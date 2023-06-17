package com.segment.common.job.chain.sample.model

import com.segment.common.job.chain.JobStep
import groovy.transform.CompileStatic

@CompileStatic
enum JobStepEnum2 {
    first(1), second(2)

    int seq

    JobStepEnum2(int seq) {
        this.seq = seq
    }

    JobStep one() {
        new JobStep(this.name(), this.seq)
    }
}