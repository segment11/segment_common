package com.segment.common.job.chain

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@CompileStatic
@Slf4j
abstract class Job {
    Integer id

    JobCreator creator

    JobType type

    JobStatus status

    JobParams params

    JobResult result

    List<JobTask> taskList = []

    Integer failedNum

    Integer costMs

    Date createdDate

    Date updatedDate

    protected Map<String, Object> toMap() {
        return [
                id         : id,
                creator    : creator?.name,
                type       : type.name,
                status     : status.name(),
                params     : params,
                result     : result,
                taskList   : taskList.collect { it.toMap() },
                failedNum  : failedNum,
                costMs     : costMs,
                createdDate: createdDate,
                updatedDate: updatedDate
        ]
    }

    abstract int appId()

    abstract void save()

    abstract boolean isJobProcessBeforeRestart()

    abstract boolean isStopped()

    abstract int maxFailNum()

    abstract void updateFailedNum(Integer failedNum)

    abstract void lockExecute(Closure<Void> cl)

    abstract void allDone()

    abstract void fail(JobStep failedStep)

    abstract void updateStatus(JobStatus status, JobResult result, Integer costMs = -1)

    void run() {
        boolean isJobProcessBeforeRestart = isJobProcessBeforeRestart()
        int maxFailNum = maxFailNum()

        if (status == JobStatus.processing) {
            if (!isJobProcessBeforeRestart) {
                log.warn 'why still processing? jobId: {}', id
                return
            }
        }

        if (status == JobStatus.failed) {
            if (failedNum >= maxFailNum) {
                log.warn 'max failed: {}, message: {}, jobId: {}', maxFailNum, result?.message, id
                return
            }

            updateFailedNum(failedNum + 1)
            return
        }

        lockExecute {
            handle()
        }
    }

    private void handle() {
        updateStatus(JobStatus.processing, JobResult.fail('begin process'))

        boolean isAllDoneOk = true
        JobStep failedStep

        long beginT = System.currentTimeMillis()
        for (task in taskList) {
            if (isStopped()) {
                log.warn 'stopped, skip job do task'
                break
            }
            task.isJobProcessBeforeRestart = isJobProcessBeforeRestart()

            def result = task.run()

            if (!result.isOk) {
                long costMs = System.currentTimeMillis() - beginT
                updateStatus(JobStatus.failed, JobResult.fail(result.message), costMs.intValue())
                failedStep = task.step

                log.warn 'job task fail, jobId: {}, step: {}, message: {}', id, failedStep.name, result.message
                isAllDoneOk = false
                break
            }
        }

        // all task done ok
        log.info 'all job task done. jobId: {}, isOk: {}', id, isAllDoneOk
        if (isAllDoneOk) {
            long costMs = System.currentTimeMillis() - beginT
            updateStatus(JobStatus.done, JobResult.ok('all done'), costMs.intValue())
            allDone()
        } else {
            assert failedStep != null
            fail(failedStep)
        }
    }
}
