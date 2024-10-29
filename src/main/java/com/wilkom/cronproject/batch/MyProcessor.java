package com.wilkom.cronproject.batch;

import java.time.LocalDateTime;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.wilkom.cronproject.model.Account;
import com.wilkom.cronproject.model.RawData;

@Component
public class MyProcessor implements ItemProcessor<RawData, Account> {

    @Override
    public Account process(final RawData data) throws Exception {

        // Filter out accounts with names longer than 10 characters
        if (data.getName().length() > 10) {
            return null;
        }

        return new Account(data.getId(), data.getName().toUpperCase(),
                data.getAmount(), LocalDateTime.now());
    }
}
