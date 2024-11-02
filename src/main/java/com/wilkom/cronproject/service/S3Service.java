package com.wilkom.cronproject.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.wilkom.cronproject.exception.S3ObjectNotFoundException;
import com.wilkom.cronproject.model.RawData;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.List;

@Service
public class S3Service {
    private static Logger logger = LoggerFactory.getLogger(S3Service.class);

    @Autowired
    private AmazonS3 amazonS3Client;

    public void saveSkippedItemsToS3(String bucketName, String key, List<RawData> skippedItems) throws IOException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (CSVPrinter csvPrinter = new CSVPrinter(new OutputStreamWriter(outputStream), CSVFormat.DEFAULT)) {
            // Write header
            // No header

            // Write data
            for (RawData item : skippedItems) {
                csvPrinter.printRecord(item.getId(), item.getName(), item.getAmount());
            }
        }

        byte[] contentBytes = outputStream.toByteArray();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(contentBytes);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(contentBytes.length);
        metadata.setContentType("text/csv");

        amazonS3Client.putObject(bucketName, key, inputStream, metadata);
    }

    public InputStream getS3Object(String bucketName, String key) throws S3ObjectNotFoundException {
        try {
            S3Object s3Object = amazonS3Client.getObject(bucketName, key);
            return s3Object.getObjectContent();
        } catch (AmazonS3Exception e) {
            if (e.getErrorCode().equals("NoSuchKey")) {
                throw new S3ObjectNotFoundException("Object not found in S3: " + bucketName + "/" + key, e);
            }
            throw e;
        }
    }

    public void deleteS3Object(String bucketName, String key) {
        try {
            amazonS3Client.deleteObject(new DeleteObjectRequest(bucketName, key));
            logger.info("Object deleted successfully: s3://{}/{}", bucketName, key);
        } catch (Exception e) {
            logger.error("Error deleting object: {}", e.getMessage());
        }
    }
}