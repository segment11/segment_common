package com.segment.common.job.leader

import spock.lang.Specification

class EtcdCheckerTest extends Specification {
    def "Init"() {
        given:
        def etcdChecker = new EtcdChecker('test', '192.168.99.101:2379', 30)
        etcdChecker.init()
        println etcdChecker.etcdAddr
        expect:
        etcdChecker.etcdAddr.contains(',')
    }
}
