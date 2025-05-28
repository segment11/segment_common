package com.segment.common.job.chain

import groovy.transform.CompileStatic

@CompileStatic
enum JobStatus {
    created(0), processing(1), failed(-1), done(10)

    int value

    JobStatus(int value) {
        this.value = value
    }

    static JobStatus from(int value) {
        for (one in values()) {
            if (one.value == value) {
                return one
            }
        }
        null
    }
}
