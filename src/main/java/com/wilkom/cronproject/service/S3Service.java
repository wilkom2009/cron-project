package com.wilkom.cronproject.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.wilkom.cronproject.model.RawData;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
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

    @Autowired
    private AmazonS3 amazonS3Client;

    public void saveSkippedItemsToS3(String bucketName, String key, List<RawData> skippedItems) throws IOException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (CSVPrinter csvPrinter = new CSVPrinter(new OutputStreamWriter(outputStream), CSVFormat.DEFAULT)) {
            // Write header

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

    public InputStream getS3ObjectAsInputStream(String bucketName, String key) {
        S3Object s3Object = amazonS3Client.getObject(new GetObjectRequest(bucketName, key));
        return s3Object.getObjectContent();
    }

}