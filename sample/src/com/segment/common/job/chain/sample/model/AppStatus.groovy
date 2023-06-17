package com.segment.common.job.chain.sample.model

import groovy.transform.CompileStatic

@CompileStatic
enum AppStatus {
    created(0), running(1), stopped(10), terminated(-1),
    switchShardMaster(100), changeReplica(200), changeShard(300),
    unhealthy(1000), inMaintenance(10000)

    int value

    AppStatus(int value) {
        this.value = value
    }

    static AppStatus from(int status) {
        for (one in AppStatus.values()) {
            if (one.value == status) {
                return one
            }
        }
        null
    }
}