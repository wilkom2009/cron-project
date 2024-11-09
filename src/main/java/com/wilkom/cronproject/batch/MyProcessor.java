package com.wilkom.cronproject.batch;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
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
    public Account process(@NonNull final RawData data) throws Exception {

        jobExecutionContext.incrementRawDataSize();

        // Skip processing if the amount is greater than 500
        if (shouldSkip(data)) {
            throw new SkippableException("Skipping item: " + data.toString());
        }

        return new Account(
                0, data.getName().toUpperCase(),
                data.getAmount(), LocalDateTime.now());
    }

    private boolean shouldSkip(RawData item) {
        // Skip client with less than 500$
        return item.getAmount().compareTo(BigDecimal.valueOf(500)) < 0;
    }

}
