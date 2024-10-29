package com.wilkom.cronproject.batch;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.support.transaction.TransactionAwareProxyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wilkom.cronproject.model.Account;
import com.wilkom.cronproject.repository.AccountRepository;

@Component
public class MyWriter implements ItemWriter<Account> {
    private static Logger logger = LoggerFactory.getLogger(MyWriter.class);

    List<Account> output = TransactionAwareProxyFactory.createTransactionalList();
    @Autowired
    private AccountRepository accountRepository;

    public void write(Chunk<? extends Account> chunk) throws Exception {
        logger.info("****************** Saving ******************");
        chunk.getItems().stream().forEach(
                a -> {
                    accountRepository.save(a);
                });
    }

}
