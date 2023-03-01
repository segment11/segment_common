package com.segment.common.job.leader

import com.segment.common.Conf
import com.segment.common.Utils
import com.segment.common.job.IntervalJob
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@CompileStatic
@Singleton
@Slf4j
class LeaderFlagHolder extends IntervalJob {
    private EtcdChecker checker

    boolean isLeader = false

    void init() {
        def c = Conf.instance
        def etcdAddr = c.get('leader.etcdAddr')
        def key = c.get('leader.key')
        if (etcdAddr) {
            int times = c.getInt('leader.check.ttlLostTimes', 3)
            checker = new EtcdChecker(key, etcdAddr, interval * times)
            checker.init()
        }
    }

    @Override
    String name() {
        'leader flag holder'
    }

    @Override
    void doJob() {
        if (!checker) {
            return
        }

        if (isLeader) {
            checker.continueLeader()
            if (intervalCount % 10 == 0) {
                log.info 'continue set leader, ip: {}, interval count: {}', Utils.localIp(), intervalCount
            }
        } else {
            boolean isLeaderNew = checker.isLeader()
            if (isLeaderNew != isLeader) {
                log.info 'change leader, ip: {}, I am the leader: {}', Utils.localIp(), isLeaderNew
            }
            isLeader = isLeaderNew
        }
    }
}
