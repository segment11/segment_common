package com.segment.common.job.chain.process

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.ThreadPoolExecutor

@CompileStatic
@Slf4j
abstract class Processor {
    abstract String name()

    private ThreadPoolExecutor executor

    synchronized void init() {
        if (executor == null) {
            executor = new OneThreadExecutor(name().toLowerCase().replaceAll(' ', '_'))
            log.info 'create job processor - {}', name()
        }
    }

    synchronized void shutdown() {
        if (executor) {
            log.info 'shutdown job processor - {}', name()
            executor.shutdown()
        }
    }

    void submit(Runnable runnable) {
        try {
            executor.submit runnable
        } catch (RejectedExecutionException e) {
            log.warn 'there is a processor is running for {}, try next time', name()
        }
    }
}
