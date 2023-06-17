package com.segment.common.job.chain.sample.model

import com.segment.common.job.chain.JobType
import groovy.transform.CompileStatic

@CompileStatic
enum JobTypeEnum {
    create(1), terminate(2)

    int value

    JobTypeEnum(int value) {
        this.value = value
    }

    static JobTypeEnum from(int value) {
        for (one in JobTypeEnum.values()) {
            if (one.value == value) {
                return one
            }
        }
        null
    }

    JobType one() {
        new JobType(this.name(), this.value)
    }
}