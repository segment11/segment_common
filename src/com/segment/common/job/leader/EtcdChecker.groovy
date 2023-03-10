package com.segment.common.job.leader

import com.alibaba.fastjson.JSONObject
import com.github.kevinsawicki.http.HttpRequest
import com.segment.common.Conf
import com.segment.common.Utils
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@CompileStatic
@Slf4j
class EtcdChecker {
    private String key = 'your_leader_key'

    private String etcdAddr

    private long ttl

    private String leaderAddr

    int connectTimeout = 200

    int readTimeout = 1000

    EtcdChecker(String key, String etcdAddr, long ttl) {
        this.key = key
        this.etcdAddr = etcdAddr
        this.ttl = ttl
    }

    String getEtcdAddr() {
        return etcdAddr
    }

    void init() {
        def c = Conf.instance
        connectTimeout = c.getInt('leader.check.connectTimeout.ms', 200)
        readTimeout = c.getInt('leader.check.readTimeout.ms', 1000)

        if (!this.etcdAddr.contains(',') || !this.etcdAddr.startsWith('http://')) {
            // dns lookup better?

            def tmpAddr = this.etcdAddr.startsWith('http://') ? this.etcdAddr : 'http://' + this.etcdAddr
            def req = HttpRequest.get(tmpAddr + '/v2/members')
            setTimeout(req)
            def body = req.body()
            log.info body

            def r = JSONObject.parseObject(body)
            def members = r.getJSONArray('members')
            List<String> urls = []
            members.each { m ->
                def jo = m as JSONObject
                def peerUrls = jo.getJSONArray('clientURLs')
                peerUrls.each { u ->
                    if (u.toString().contains('127.')) {
                        return
                    }
                    urls << u.toString()
                }
            }
            this.etcdAddr = urls.join(',')
        }
    }

    private void setTimeout(HttpRequest req) {
        req.connectTimeout(connectTimeout).readTimeout(readTimeout)
    }

    private boolean isAddrLeader(String addr) {
        try {
            def req = HttpRequest.get(addr + '/v2/stats/self')
            setTimeout(req)
            def body = req.body()

            def r = JSONObject.parseObject(body)
            return 'StateLeader' == r.getString('state')
        } catch (Exception e) {
            log.error('access etcd error', e)
            return false
        }
    }

    private boolean updateKeyValue() {
        if (!leaderAddr) {
            return false
        }

        try {
            Map params = [:]
            params.ttl = ttl
            params.value = Utils.localIp()
            def reqPut = HttpRequest.put(leaderAddr + '/v2/keys/' + key + '?prevExist=false', params, true)
            setTimeout(reqPut)
            def body = reqPut.body()

            /*
{
"action": "create",
"node": {
"key": "/name",
"value": "kerry",
"expiration": "2022-11-27T11:35:06.665439053Z",
"ttl": 10,
"modifiedIndex": 46,
"createdIndex": 46
}
}
       */
            log.debug body

            def r = JSONObject.parseObject(body)

            Integer errorCode = r.getInteger('errorCode')
            // 105 -> Key already exists
            return 105 != errorCode
        } catch (Exception e) {
            log.error('access etcd error', e)
            return false
        }
    }

    boolean isLeader() {
        if (!etcdAddr) {
            return false
        }
        if (leaderAddr && isAddrLeader(leaderAddr)) {
            return updateKeyValue()
        }

        for (addr in etcdAddr.split(',')) {
            if (isAddrLeader(addr)) {
                leaderAddr = addr
                break
            }
        }
        updateKeyValue()
    }

    private void updateAgain() {
        if (!leaderAddr) {
            throw new IllegalStateException('leader addr not found')
        }

        try {
            Map params = [:]
            params.ttl = ttl
            params.value = Utils.localIp()
            def reqPut = HttpRequest.put(leaderAddr + '/v2/keys/' + key, params, true)
            setTimeout(reqPut)
            def code = reqPut.code()
            if (200 != code) {
                log.warn 'continue set leader failed, body: {}', reqPut.body()
            }
        } catch (Exception e) {
            log.error('access etcd error', e)
        }
    }

    void continueLeader() {
        if (!etcdAddr) {
            return
        }

        if (leaderAddr && isAddrLeader(leaderAddr)) {
            updateAgain()
            return
        }

        for (addr in etcdAddr.split(',')) {
            if (isAddrLeader(addr)) {
                leaderAddr = addr
                break
            }
        }

        updateAgain()
    }
}
