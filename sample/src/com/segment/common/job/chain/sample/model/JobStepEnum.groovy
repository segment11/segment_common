package com.segment.common.job.chain.sample.model

import com.segment.common.job.chain.JobStep
import groovy.transform.CompileStatic

@CompileStatic
enum JobStepEnum {
    first, second

    int index() {
        def steps = JobStepEnum.values()
        int i = 0
        for (step in steps) {
            if (step == this) {
                return i
            }
            i++
        }
    }

    JobStep one() {
        new JobStep(this.name(), this.index())
    }
}