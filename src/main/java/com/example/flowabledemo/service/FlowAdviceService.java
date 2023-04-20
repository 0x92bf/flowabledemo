package com.example.flowabledemo.service;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

@Slf4j
public class FlowAdviceService implements JavaDelegate {

    @Override
    public void execute(DelegateExecution delegateExecution) {
        log.info("===========flowable serviceTask=========");
        //delegateExecution.getExecutions();
    }
}
