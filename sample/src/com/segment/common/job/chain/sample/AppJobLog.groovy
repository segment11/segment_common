package com.segment.common.job.chain.sample

import com.segment.common.job.chain.JobParams
import com.segment.common.job.chain.JobResult
import com.segment.common.job.chain.JobTask
import com.segment.common.job.chain.sample.model.AppJobLogDTO
import com.segment.common.job.chain.sample.model.json.ExtendParams
import groovy.transform.CompileStatic

@CompileStatic
class AppJobLog extends JobTask.TaskLog {

    AppJobLog() {}

    AppJobLog(AppJobLogDTO r) {
        id = r.id
        jobId = r.jobId
        step = r.step
        jobResult = r.isOk ? JobResult.ok(r.message) : JobResult.fail(r.message)
        if (r.params) {
            jobResult.params = new JobParams(r.params.params)
        }
        costMs = r.costMs
        createdDate = r.createdDate
        updatedDate = r.updatedDate
    }

    @Override
    int add() {
        def r = new AppJobLogDTO()
        r.jobId = jobId
        r.step = step
        r.isOk = jobResult.isOk
        r.message = jobResult.message
        r.params = new ExtendParams(jobResult.params.params)
        r.costMs = costMs
        r.createdDate = createdDate
        r.updatedDate = updatedDate
        r.add()
    }

    @Override
    void update() {
        def r = new AppJobLogDTO()
        r.id = id
        r.jobId = jobId
        r.step = step
        r.isOk = jobResult.isOk
        r.message = jobResult.message
        r.params = new ExtendParams(jobResult.params.params)
        r.costMs = costMs
        r.createdDate = createdDate
        r.updatedDate = updatedDate
        r.update()
    }
}
