package com.wilkom.cronproject.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

import com.wilkom.cronproject.exception.S3ObjectNotFoundException;
import com.wilkom.cronproject.model.RawData;
import com.wilkom.cronproject.service.S3Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class JobCompletionNotificationListener implements JobExecutionListener {
    private static Logger logger = LoggerFactory.getLogger(JobCompletionNotificationListener.class);

    private final SkipListenerImpl skipListener;
    private final S3Service s3Service;
    private final JobExecutionContext jobExecutionContext;
    private static final String bucketName = "ws-cron-job-bucket";
    private static final String failedBucketName = "ws-cron-job-bucket/failed";
    private static final String key = "data-list.csv";
    private static final String failedKey = "data-list";

    @Autowired
    public JobCompletionNotificationListener(SkipListenerImpl skipListener, S3Service s3Service,
            JobExecutionContext jobExecutionContext) {
        this.skipListener = skipListener;
        this.s3Service = s3Service;
        this.jobExecutionContext = jobExecutionContext;
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

            performRawDataFileCleanupAction();
            performSaveToS3SkippedItemsAction();
        } else {
            logger.error("Job failed!");
        }
    }

    private void performRawDataFileCleanupAction() {

        if (jobExecutionContext.getProcessedDataSize() != 0
                && jobExecutionContext.getProcessedDataSize() <= jobExecutionContext.getRawDataSize()) {
            logger.info("{} items have been processed, cleaning up origin raw data file ...",
                    jobExecutionContext.getProcessedDataSize());

            s3Service.deleteS3Object(bucketName, key);

            logger.info("Cleaning up successfully done!");
        }
    }

    private void performSaveToS3SkippedItemsAction() {
        List<RawData> skippedItems = skipListener.getSkippedItems();

        if (!skippedItems.isEmpty()) {
            logger.info("Job completed. Saving skipped items to S3:");
            try {
                String fkey = failedKey + "-" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        + ".csv";
                s3Service.saveSkippedItemsToS3(failedBucketName, fkey, skippedItems);
                logger.info("Skipped {} items saved to S3: s3://{}/{}", skippedItems.size(), failedBucketName, fkey);
            } catch (IOException e) {
                logger.error("Error saving skipped items to S3: {}", e.getMessage());
            }
        }
    }
}
