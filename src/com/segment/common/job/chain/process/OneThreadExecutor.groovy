package com.segment.common.job.chain.process

import com.segment.common.job.NamedThreadFactory
import groovy.transform.CompileStatic

import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.LinkedTransferQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

@CompileStatic
class OneThreadExecutor extends ThreadPoolExecutor {
    OneThreadExecutor(String threadName) {
        super(1, 1, 0, TimeUnit.MILLISECONDS,
                new OneSizeExecutorQueue(), new NamedThreadFactory(threadName), new AbortPolicy())
    }
}

@CompileStatic
class OneSizeExecutorQueue extends LinkedBlockingDeque<Runnable> {
    boolean offer(Runnable o) {
        this.size() == 0 ? super.offer(o) : false
    }
}