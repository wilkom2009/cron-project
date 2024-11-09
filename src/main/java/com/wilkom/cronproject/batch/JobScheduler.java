package com.wilkom.cronproject.batch;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class JobScheduler {

    private final JobLauncher jobLauncher;

    private final BatchConfig batchConfig;

    private final JobRepository jobRepository;

    private final Step myStep1;

    @Autowired
    public JobScheduler(JobLauncher jobLauncher, BatchConfig batchConfig, JobRepository jobRepository, Step myStep1) {
        this.jobLauncher = jobLauncher;
        this.batchConfig = batchConfig;
        this.jobRepository = jobRepository;
        this.myStep1 = myStep1;
    }

    @Scheduled(fixedRate = 120000) // Runs every 2 minutes
    public void runJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(batchConfig.myJob(jobRepository, myStep1), jobParameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
