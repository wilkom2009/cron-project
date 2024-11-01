package com.wilkom.cronproject.batch;

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

import com.wilkom.cronproject.exception.SkippableException;
import com.wilkom.cronproject.model.Account;
import com.wilkom.cronproject.model.RawData;
import com.wilkom.cronproject.service.S3Service;

import jakarta.annotation.Nonnull;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    private static final String bucketName = "ws-cron-job-bucket";
    private static final String key = "data-list.csv";

    @Value("${file.input}")
    private String fileInput;

    @Autowired
    private S3Service s3Service;

    @Bean
    public SkipListenerImpl skipListener() {
        return new SkipListenerImpl();
    }

    @Bean
    public JobCompletionNotificationListener jobCompletionListener() {
        return new JobCompletionNotificationListener(skipListener(), s3Service);
    }

    @Bean
    public Tasklet myTasklet() {
        return new TaskOne();
    }

    @Bean
    @StepScope // create a new instance for each step execution
    @Nonnull
    public FlatFileItemReader<RawData> reader() {
        FlatFileItemReader<RawData> reader = new FlatFileItemReader<>();
        reader.setResource(new InputStreamResource(s3Service.getS3ObjectAsInputStream(bucketName, key)));
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
                .faultTolerant()
                .skip(SkippableException.class)
                .skipLimit(10000) // Adjust this value as needed
                .listener(skipListener())
                .build();
    }

    @Bean
    @Nonnull
    public Job myJob(JobRepository jobRepository, Step step) {
        return new JobBuilder("myJob", jobRepository)
                .start(step)
                .listener(jobCompletionListener())
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
}
