package com.segment.common.job.chain

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@CompileStatic
@Slf4j
abstract class JobTask {
    @CompileStatic
    static abstract class TaskLog {
        Integer id

        Integer jobId

        // tips: not JobStep
        String step

        JobResult jobResult

        Integer costMs

        Date createdDate

        Date updatedDate

        abstract int add()

        abstract void update()
    }

    Job job

    JobStep step

    // need overwrite this method if there are more than one task with same job step name
    JobParams params

    protected int taskLogId
    protected TaskLog taskLog

    boolean isJobProcessBeforeRestart = false

    String stepAsUuid() {
        assert step
        step.name
    }

    @Override
    String toString() {
        step ? step.name : "to be set task"
    }

    abstract TaskLog load(Integer jobId, String stepAsUuid)

    abstract TaskLog newLog()

    abstract JobResult doTask()

    JobResult run() {
        assert job && step

        def one = load(job.id, stepAsUuid())
        if (!one) {
            def newOne = newLog()
            newOne.jobId = job.id
            newOne.step = stepAsUuid()
            newOne.jobResult = JobResult.fail()
            newOne.createdDate = new Date()
            newOne.updatedDate = new Date()
            taskLog = newOne
            taskLogId = newOne.add()
            newOne.id = taskLogId

            // job is start before server restart
            // but this job log (sub job task) is not restart
            isJobProcessBeforeRestart = false
        } else {
            // if already done
            if (one.jobResult.isOk) {
                log.warn 'job task log already done ok, job task log id: {}', one.id
                return JobResult.ok('already done, skip')
            }
            taskLog = one
            taskLogId = one.id
        }
        log.info 'ready to do job task log id: {}', taskLogId

        long beginT = System.currentTimeMillis()
        try {
            def result = doTask()
            long costMs = System.currentTimeMillis() - beginT

            def newLog = newLog()
            newLog.id = taskLogId
            newLog.jobResult = result
            newLog.costMs = costMs.intValue()
            newLog.updatedDate = new Date()
            newLog.update()

            return result
        } catch (Exception e) {
            long costMs = System.currentTimeMillis() - beginT
            log.error('job task error, jobId: ' + job.id, ' step: ' + stepAsUuid(), e)

            def newLog = newLog()
            newLog.id = taskLogId
            newLog.jobResult = JobResult.fail(e.message)
            newLog.costMs = costMs.intValue()
            newLog.updatedDate = new Date()
            newLog.update()

            return JobResult.fail('step: ' + stepAsUuid() + ' ex: ' + e.message)
        }
    }
}
