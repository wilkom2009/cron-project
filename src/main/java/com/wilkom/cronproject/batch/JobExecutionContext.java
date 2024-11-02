package com.wilkom.cronproject.batch;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobExecutionContext {
    private Integer rawDataSize = 0;
    private Integer processedDataSize = 0;

    public void incrementProcessedDataSize() {
        processedDataSize++;
    }

    public void incrementRawDataSize() {
        rawDataSize++;
    }

}
