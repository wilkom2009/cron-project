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

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private BatchConfig batchConfig;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private Step myStep1;

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
