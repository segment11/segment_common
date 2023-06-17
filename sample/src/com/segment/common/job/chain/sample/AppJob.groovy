package com.segment.common.job.chain.sample

import com.segment.common.Conf
import com.segment.common.job.chain.*
import com.segment.common.job.chain.sample.model.*
import com.segment.common.job.lock.LocalOneLock
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@CompileStatic
@Slf4j
class AppJob extends Job {
    AppDTO app
    AppJobDTO dto

    AppJob(AppDTO app, AppJobDTO dto) {
        this.app = app
        this.dto = dto

        this.id = dto.id
        this.creator = new JobCreator(dto.creator)
        this.type = JobTypeEnum.from(dto.type).one()
        this.status = JobStatus.from(dto.status)
        this.params = new JobParams(dto.params.params)
        this.result = dto.failedNum > 0 ? JobResult.fail(dto.message) : JobResult.ok(dto.message)
        this.failedNum = dto.failedNum
        this.costMs = dto.costMs
        this.createdDate = dto.createdDate
        this.updatedDate = dto.updatedDate
    }

    @Override
    int appId() {
        app.id
    }

    @Override
    void save() {
        dto.creator = creator.name
        dto.type = type.value
        dto.status = status.value
        dto.params.params.clear()
        dto.params.params.putAll(params.params)

        dto.failedNum = failedNum
        dto.message = result?.message
        dto.costMs = costMs
        dto.updatedDate = updatedDate

        dto.update()
    }

    @Override
    boolean isJobProcessBeforeRestart() {
        dto.createdDate < AppProcessor.serverStartDate
    }

    @Override
    boolean isStopped() {
        AppProcessor.isStopped
    }

    @Override
    int maxFailNum() {
        Conf.instance.getInt('job.maxFailNum', 3)
    }

    @Override
    void updateFailedNum(Integer failedNum) {
        new AppJobDTO(id: dto.id, failedNum: failedNum, updatedDate: new Date()).update()
    }

    @Override
    void lockExecute(Closure<Void> cl) {
        def lock = new LocalOneLock()
        lock.lockKey = 'app_operate_' + app.id
        def isDone = lock.exe(cl)
        if (!isDone) {
            log.warn 'get lock fail and try next time, app id: {}', app.id
        }
    }

    @Override
    void allDone() {
        log.warn 'all done'
        // done other jobs of this app
        new AppJobDTO().useD().exeUpdate('update app_job set status = ? where app_id = ? and status != ?',
                [JobStatus.done.value, app.id, JobStatus.done.value])

        // u can change here, update different status by job type
        if (type == JobTypeEnum.create.one()) {
            app.updateStatus(AppStatus.running)
        } else if (type == JobTypeEnum.terminate.one()) {
            app.updateStatus(AppStatus.terminated)
        }
    }

    @Override
    void fail(JobStep failedStep) {
        // done other jobs of this app except this
        new AppJobDTO().useD().exeUpdate('update app_job set status = ? where app_id = ? and status != ? and id != ?',
                [JobStatus.done.value, app.id, JobStatus.done.value, id])

        // u can change here, update different status by job type and failed job step
        if (failedStep == JobStepEnum.second.one()) {
            if (type == JobTypeEnum.create.one()) {
                app.updateStatus(AppStatus.created)
            } else if (type == JobTypeEnum.terminate.one()) {
                app.updateStatus(AppStatus.running)
            }
        }
    }

    @Override
    void updateStatus(JobStatus status, JobResult result, Integer costMs) {
        new AppJobDTO(id: id, status: status.value, message: result.message,
                costMs: costMs, updatedDate: new Date()).update()
    }
}
