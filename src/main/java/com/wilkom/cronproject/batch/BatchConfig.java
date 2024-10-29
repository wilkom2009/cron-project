package com.wilkom.cronproject.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import com.wilkom.cronproject.model.Account;
import com.wilkom.cronproject.model.RawData;

import jakarta.annotation.Nonnull;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    @Value("${file.input}")
    private String fileInput;

    @Bean
    public Tasklet myTasklet() {
        return new TaskOne();
    }

    @Bean
    public FlatFileItemReader<RawData> reader() {
        return new FlatFileItemReaderBuilder<RawData>().name("rawItemReader")
                .resource(new ClassPathResource(fileInput))
                .delimited()
                .names("id", "name", "amount")
                .targetType(
                        RawData.class)
                .build();
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

}
