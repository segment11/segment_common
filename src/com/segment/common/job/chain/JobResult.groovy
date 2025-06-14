package com.segment.common.job.chain

import groovy.transform.CompileStatic
import groovy.transform.ToString

@CompileStatic
@ToString(includeNames = true)
class JobResult {
    boolean isOk = true
    String message
    // for next step parameter pass
    JobParams params = new JobParams()

    JobResult() {}

    private JobResult(boolean isOk, String message) {
        this.isOk = isOk
        this.message = message
    }

    static JobResult fail(String message = null) {
        new JobResult(false, message)
    }

    static JobResult ok(String message = null) {
        new JobResult(true, message)
    }
}
