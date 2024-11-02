package com.wilkom.cronproject.batch;

import java.util.List;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.support.transaction.TransactionAwareProxyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.wilkom.cronproject.model.Account;
import com.wilkom.cronproject.repository.AccountRepository;

@Component
public class MyWriter implements ItemWriter<Account> {

    List<Account> output = TransactionAwareProxyFactory.createTransactionalList();

    private final AccountRepository accountRepository;
    private final JobExecutionContext jobExecutionContext;

    @Autowired
    public MyWriter(AccountRepository accountRepository, JobExecutionContext jobExecutionContext) {
        this.jobExecutionContext = jobExecutionContext;
        this.accountRepository = accountRepository;
    }

    public void write(@NonNull Chunk<? extends Account> chunk) throws Exception {
        chunk.getItems().stream().forEach(
                a -> {
                    accountRepository.save(a);
                    jobExecutionContext.incrementProcessedDataSize();
                });
    }

}
