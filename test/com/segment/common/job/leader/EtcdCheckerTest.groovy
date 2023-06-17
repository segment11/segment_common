package com.segment.common.job.leader

import spock.lang.Specification

class EtcdCheckerTest extends Specification {
    String etcdAddr = '192.168.111.112:2379'

    def "Init"() {
        given:
        def etcdChecker = new EtcdChecker('test', etcdAddr, 30)
        etcdChecker.init()
        println etcdChecker.etcdAddr
        expect:
        etcdChecker.etcdAddr.contains(',')
    }

    def "IsLeader"() {
        given:
        def etcdChecker = new EtcdChecker('test', etcdAddr, 5)
        etcdChecker.init()
        println etcdChecker.etcdAddr
        expect:
        etcdChecker.isLeader()
        !etcdChecker.isLeader()
        when:
        Thread.sleep(5000)
        then:
        etcdChecker.isLeader()
    }
}
