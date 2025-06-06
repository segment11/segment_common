package com.segment.common.job.chain

import groovy.transform.CompileStatic

@CompileStatic
enum JobStatus {
    created, processing, failed, done
}
