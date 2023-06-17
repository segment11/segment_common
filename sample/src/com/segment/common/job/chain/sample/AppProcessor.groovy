package com.segment.common.job.chain.sample

import com.segment.common.job.chain.process.Processor
import groovy.transform.CompileStatic

import java.util.concurrent.ConcurrentHashMap

@CompileStatic
class AppProcessor extends Processor {
    private static ConcurrentHashMap<Integer, AppProcessor> appProcessorByAppId = new ConcurrentHashMap<>()

    static void run(AppJob appJob) {
        def oneProcessor = new AppProcessor()
        oneProcessor.appJob = appJob

        def old = appProcessorByAppId.putIfAbsent(appJob.app.id, oneProcessor)
        if (old == null) {
            oneProcessor.init()
            oneProcessor.submit {
                appJob.run()
            }
        } else {
            old.appJob = appJob
            old.init()
            old.submit {
                appJob.run()
            }
        }
    }

    static volatile boolean isStopped = false

    static void stopRunning() {
        isStopped = true

        for (entry in appProcessorByAppId) {
            if (entry.value) {
                entry.value.shutdown()
            }
        }
    }

    static Date serverStartDate = new Date()

    AppJob appJob

    @Override
    String name() {
        appJob.app.name
    }
}
