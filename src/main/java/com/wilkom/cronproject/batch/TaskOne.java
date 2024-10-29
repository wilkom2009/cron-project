package com.wilkom.cronproject.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class TaskOne implements Tasklet, StepExecutionListener {
    private static Logger LOG = LoggerFactory.getLogger(TaskOne.class);

    @Override
    @Nullable
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        LOG.info("Executing!");

        return RepeatStatus.FINISHED;
    }

    @Override
    public ExitStatus afterStep(StepExecution ex) {
        LOG.info("Finished!");

        return ExitStatus.COMPLETED;
    }
}
