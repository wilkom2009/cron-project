package com.wilkom.cronproject.batch;

import org.springframework.batch.core.SkipListener;
import org.springframework.lang.NonNull;

import com.wilkom.cronproject.exception.SkippableException;
import com.wilkom.cronproject.model.Account;
import com.wilkom.cronproject.model.RawData;

import java.util.ArrayList;
import java.util.List;

public class SkipListenerImpl implements SkipListener<RawData, Account> {
    private List<RawData> skippedItems = new ArrayList<>();

    @Override
    public void onSkipInProcess(@NonNull RawData item, @NonNull Throwable t) {
        if (t instanceof SkippableException) {
            skippedItems.add(item);
        }
    }

    public List<RawData> getSkippedItems() {
        return skippedItems;
    }
}
