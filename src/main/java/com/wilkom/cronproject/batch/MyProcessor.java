package com.wilkom.cronproject.batch;

import java.time.LocalDateTime;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.wilkom.cronproject.model.Account;
import com.wilkom.cronproject.model.RawData;

@Component
public class MyProcessor implements ItemProcessor<RawData, Account> {

    @Override
    public Account process(final RawData account) throws Exception {

        return new Account(account.getId(), account.getName().toUpperCase(),
                account.getAmount(), LocalDateTime.now());
    }
}
