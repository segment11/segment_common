package com.segment.common.job.chain.sample.task

import com.segment.common.job.chain.JobTask
import com.segment.common.job.chain.sample.AppJobLog
import com.segment.common.job.chain.sample.model.AppJobLogDTO
import groovy.transform.CompileStatic

@CompileStatic
abstract class BaseTask extends JobTask {
    @Override
    JobTask.TaskLog load(Integer jobId, String stepAsUuid) {
        def dto = new AppJobLogDTO(jobId: jobId, step: stepAsUuid).one()
        if (!dto) {
            return null
        }
        new AppJobLog(dto)
    }

    @Override
    JobTask.TaskLog newLog() {
        new AppJobLog()
    }
}
