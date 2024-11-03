package com.wilkom.cronproject.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.wilkom.cronproject.exception.S3ObjectNotFoundException;
import com.wilkom.cronproject.model.RawData;
import com.wilkom.cronproject.service.EmailService;
import com.wilkom.cronproject.service.S3Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class JobCompletionNotificationListener implements JobExecutionListener {
    private static Logger logger = LoggerFactory.getLogger(JobCompletionNotificationListener.class);

    private final SkipListenerImpl skipListener;
    private final S3Service s3Service;
    private final JobExecutionContext jobExecutionContext;
    private final EmailService emailService;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @Value("${aws.s3.bucket.failed.name}")
    private String failedBucketName;

    @Value("${aws.s3.data.key}")
    private String rawDataKey;

    @Value("${aws.ses.to-email}")
    private String toEmail;

    @Autowired
    public JobCompletionNotificationListener(SkipListenerImpl skipListener, S3Service s3Service,
            JobExecutionContext jobExecutionContext, EmailService emailService) {
        this.skipListener = skipListener;
        this.s3Service = s3Service;
        this.jobExecutionContext = jobExecutionContext;
        this.emailService = emailService;
    }

    @Override
    public void afterJob(@NonNull JobExecution jobExecution) {
        if (jobExecution.getExitStatus().getExitCode().equals("COMPLETED")) {
            logger.info("Job completed successfully!");

            // Check for skipped items due to S3ObjectNotFoundException
            long s3ObjectNotFoundCount = jobExecution.getStepExecutions().stream()
                    .flatMap(stepExecution -> stepExecution.getFailureExceptions().stream())
                    .filter(throwable -> throwable.getCause() instanceof S3ObjectNotFoundException)
                    .count();

            if (s3ObjectNotFoundCount > 0) {
                logger.warn("Job completed, but {} S3 objects were not found", s3ObjectNotFoundCount);
            }

            performRawDataFileCleanupAction(jobExecution);
            performSaveToS3SkippedItemsAction();
        } else {
            performProcessingDataRevertRenaming();
            sendSuccessEmailNotification(jobExecution, false);
            logger.error("Job failed!");
        }
    }

    private void performProcessingDataRevertRenaming() {
        String fileName = rawDataKey + ".csv";
        String renamedFileName = "processing_" + fileName;

        try {
            s3Service.renameS3Object(bucketName, renamedFileName, fileName);
        } catch (S3ObjectNotFoundException e) {
            logger.error("Failed to rename S3 object: {}", renamedFileName);
        }
    }

    /**
     * Perform the processed data clean up
     */
    private void performRawDataFileCleanupAction(JobExecution jobExecution) {

        if (jobExecutionContext.getProcessedDataSize() != 0
                && jobExecutionContext.getProcessedDataSize() <= jobExecutionContext.getRawDataSize()) {
            logger.info("{} items have been processed, cleaning up origin raw data file ...",
                    jobExecutionContext.getProcessedDataSize());

            String fileName = rawDataKey + ".csv";
            String renamedFileName = "processing_" + fileName;

            s3Service.deleteS3Object(bucketName, renamedFileName);

            logger.info("Cleaning up successfully done!");
            sendSuccessEmailNotification(jobExecution, true);
        }
    }

    private void performSaveToS3SkippedItemsAction() {
        List<RawData> skippedItems = skipListener.getSkippedItems();

        if (!skippedItems.isEmpty()) {
            logger.info("Job completed. Saving skipped items to S3:");
            try {
                String fkey = rawDataKey + "-" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        + ".csv";
                s3Service.saveSkippedItemsToS3(failedBucketName, fkey, skippedItems);
                logger.info("Skipped {} items saved to S3: s3://{}/{}", skippedItems.size(), failedBucketName, fkey);
            } catch (IOException e) {
                logger.error("Error saving skipped items to S3: {}", e.getMessage());
            }
        }
    }

    private void sendSuccessEmailNotification(JobExecution jobExecution, boolean isSuccessful) {
        String rootMsg = isSuccessful ? "completed successfully" : "failed";
        // Send email notification
        String emailBody = String.format(
                "Job %s %s  at %s.<br><br>" +
                        "Start time: %s<br>" +
                        "End time: %s<br>" +
                        "Status: %s<br>" +
                        "Exit status: %s<br>",
                jobExecution.getJobInstance().getJobName(),
                rootMsg,
                jobExecution.getEndTime(),
                jobExecution.getStartTime(),
                jobExecution.getEndTime(),
                jobExecution.getStatus(),
                jobExecution.getExitStatus().getExitDescription());

        emailService.sendEmail(
                toEmail,
                "Batch Job Completed: " + jobExecution.getJobInstance().getJobName(),
                emailBody);
    }
}
