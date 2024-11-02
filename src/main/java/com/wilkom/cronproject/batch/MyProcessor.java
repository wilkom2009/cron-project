package com.wilkom.cronproject.batch;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wilkom.cronproject.exception.SkippableException;
import com.wilkom.cronproject.model.Account;
import com.wilkom.cronproject.model.RawData;

@Component
public class MyProcessor implements ItemProcessor<RawData, Account> {
    private final JobExecutionContext jobExecutionContext;

    @Autowired
    public MyProcessor(JobExecutionContext jobExecutionContext) {
        this.jobExecutionContext = jobExecutionContext;
    }

    @Override
    public Account process(final RawData data) throws Exception {

        jobExecutionContext.incrementRawDataSize();

        int randomInt = ThreadLocalRandom.current().nextInt(1, 1001);

        // Skip processing if the amount is greater than 500
        if (shouldSkip(data)) {
            throw new SkippableException("Skipping item: " + data.getId());
        }

        return new Account(
                randomInt * data.getId(), data.getName().toUpperCase(),
                data.getAmount(), LocalDateTime.now());
    }

    private boolean shouldSkip(RawData item) {
        // Skip client with less than 500$
        return item.getAmount().compareTo(BigDecimal.valueOf(500)) < 0;
    }

}
