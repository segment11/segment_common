package com.segment.common.job.chain

import groovy.transform.CompileStatic
import io.prometheus.client.Summary

@CompileStatic
@Singleton
class JobStatsHolder {
    Summary jobTaskProcessTimeSummary = Summary.build().name('job_task_process_time').
            help('job task process time cost in millis').
            labelNames('app_id', 'job_type', 'step').
            quantile(0.5.doubleValue(), 0.05.doubleValue()).
            quantile(0.9.doubleValue(), 0.01.doubleValue()).register()
}
