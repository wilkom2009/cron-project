package com.wilkom.cronproject.batch;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
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

import com.wilkom.cronproject.exception.S3ObjectNotFoundException;
import com.wilkom.cronproject.exception.SkippableException;
import com.wilkom.cronproject.model.Account;
import com.wilkom.cronproject.model.RawData;
import com.wilkom.cronproject.repository.AccountRepository;
import com.wilkom.cronproject.service.S3Service;

import jakarta.annotation.Nonnull;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    private static Logger logger = LoggerFactory.getLogger(BatchConfig.class);

    @Value("${s3.bucket.name}")
    private String bucketName;

    @Value("${s3.data.key}")
    private String rawDataKey;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private AccountRepository accountRepository;

    private InputStream getS3InputStream(String fileName) {
        try {
            return s3Service.getS3Object(bucketName, fileName);
        } catch (S3ObjectNotFoundException e) {
            logger.error("S3 object not found: {}", fileName);
            return new ByteArrayInputStream(new byte[0]);
        }
    }

    private String renameInputFile() {
        String fileName = rawDataKey + ".csv";
        String renamedFileName = "processing_" + fileName;
        try {
            s3Service.renameS3Object(bucketName, fileName, renamedFileName);
        } catch (S3ObjectNotFoundException e) {
            return fileName;
        }
        return renamedFileName;
    }

    @Bean
    @JobScope
    public JobExecutionContext jobExecutionContext() {
        return new JobExecutionContext();
    }

    @Bean
    public SkipListenerImpl skipListener() {
        return new SkipListenerImpl();
    }

    @Bean
    public JobCompletionNotificationListener jobCompletionListener(JobExecutionContext jobExecutionContext) {
        return new JobCompletionNotificationListener(skipListener(), s3Service, jobExecutionContext);
    }

    @Bean
    @StepScope // create a new instance for each step execution
    @Nonnull
    public FlatFileItemReader<RawData> reader() {

        String renamedFileName = renameInputFile();

        FlatFileItemReader<RawData> reader = new FlatFileItemReader<>();
        reader.setResource(new InputStreamResource(getS3InputStream(renamedFileName)));
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
    public Step myStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("myStep" + System
                .currentTimeMillis(), jobRepository)
                .<RawData, Account>chunk(50, transactionManager)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .faultTolerant()
                .skip(S3ObjectNotFoundException.class)
                .skip(SkippableException.class)
                .skipLimit(10000) // Adjust this value as needed
                .listener(skipListener())
                .build();
    }

    @Bean
    @Nonnull
    public Job myJob(JobRepository jobRepository, Step step) {
        return new JobBuilder("myJob" + System
                .currentTimeMillis(), jobRepository)
                .start(step)
                .listener(jobCompletionListener(jobExecutionContext()))
                .build();
    }

    @Bean
    public MyProcessor processor() {
        return new MyProcessor(jobExecutionContext());
    }

    @Bean
    public MyWriter writer() {
        return new MyWriter(accountRepository, jobExecutionContext());
    }
}
