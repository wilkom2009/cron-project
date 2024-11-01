package com.wilkom.cronproject.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;

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

    @Autowired
    public JobCompletionNotificationListener(SkipListenerImpl skipListener, S3Service s3Service) {
        this.skipListener = skipListener;
        this.s3Service = s3Service;
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        List<RawData> skippedItems = skipListener.getSkippedItems();
        if (!skippedItems.isEmpty()) {
            logger.info("Job completed. Saving skipped items to S3:");
            try {
                String bucketName = "ws-cron-job-bucket/failed";
                String key = "skipped-items-" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        + ".csv";
                s3Service.saveSkippedItemsToS3(bucketName, key, skippedItems);
                logger.info("Skipped {} items saved to S3: s3://{}/{}", skippedItems.size(), bucketName, key);
            } catch (IOException e) {
                logger.error("Error saving skipped items to S3: {}", e.getMessage());
            }
        }
    }
}
