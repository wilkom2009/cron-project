package com.wilkom.cronproject.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.wilkom.cronproject.model.Account;

@Component
public class MyProcessor implements ItemProcessor<Account, Account> {

    private static final Logger logger = LoggerFactory.getLogger(MyProcessor.class);

    @Override
    public Account process(final Account account) throws Exception {

        Account transformedCoffee = new Account(account.getId(), account.getHolder().toUpperCase(),
                account.getBalance());

        return transformedCoffee;
    }
}
