package com.segment.common.job.chain.sample.task

import com.segment.common.job.chain.JobResult
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@CompileStatic
@Slf4j
class RedisCreateTask extends BaseTask {
    @Override
    JobResult doTask() {
        log.info 'doing task {}', stepAsUuid()
        Thread.sleep(100)
        log.info 'done task {}', stepAsUuid()
        JobResult.ok('just for test')
    }
}
