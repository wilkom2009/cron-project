package com.wilkom.cronproject.batch;

import java.io.InputStream;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.InputStreamResource;
import org.springframework.transaction.PlatformTransactionManager;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.wilkom.cronproject.model.Account;
import com.wilkom.cronproject.model.RawData;

import jakarta.annotation.Nonnull;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    private static final String bucketName = "ws-cron-job-bucket";
    private static final String key = "data-list.csv";

    @Value("${file.input}")
    private String fileInput;

    @Autowired
    private AmazonS3 amazonS3Client;

    @Bean
    public Tasklet myTasklet() {
        return new TaskOne();
    }

    @Bean
    @StepScope // create a new instance for each step execution
    @Nonnull
    public FlatFileItemReader<RawData> reader() {
        FlatFileItemReader<RawData> reader = new FlatFileItemReader<>();
        reader.setResource(new InputStreamResource(getInputStream(bucketName, key)));
        reader.setLinesToSkip(1); // Skip header row if present

        DefaultLineMapper<RawData> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("id", "name", "amount");

        BeanWrapperFieldSetMapper<RawData> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(RawData.class);

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        reader.setLineMapper(lineMapper);

        return reader;
    }

    @Bean
    @Nonnull
    public Step myStep(JobRepository jobRepository, Tasklet myTasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("myStep", jobRepository)
                .<RawData, Account>chunk(50, transactionManager)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }

    @Bean
    @Nonnull
    public Job myJob(JobRepository jobRepository, Step step) {
        return new JobBuilder("myJob", jobRepository)
                .start(step)
                .build();
    }

    @Bean
    public MyProcessor processor() {
        return new MyProcessor();
    }

    @Bean
    public MyWriter writer() {
        return new MyWriter();
    }

    public InputStream getInputStream(String bucketName, String key) {
        S3Object s3Object = amazonS3Client.getObject(new GetObjectRequest(bucketName, key));
        return s3Object.getObjectContent();
    }
}
