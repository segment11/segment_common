package com.segment.common.job

import spock.lang.Specification

class IntervalJobTest extends Specification {
    static class Inner extends IntervalJob {
        int i

        @Override
        String name() {
            'test'
        }

        @Override
        void doJob() {
            println i++
        }
    }

    def 'test all'() {
        given:
        def job = new Inner()
        job.interval = 2
        job.isDelayRelative = true
        job.start()
        and:
        Thread.sleep(1000 * 10)
        job.stop()
        expect:
        job.i >= 5
    }
}
