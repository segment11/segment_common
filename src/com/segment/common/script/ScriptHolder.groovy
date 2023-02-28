package com.segment.common.script

import com.alibaba.fastjson.JSON
import com.segment.common.http.Invoker
import com.segment.common.job.IntervalJob
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@CompileStatic
@Singleton
@Slf4j
class ScriptHolder extends IntervalJob {
    private Map<String, OneScript> scripts = [:]

    String getContentByName(String name) {
        scripts[name]?.content
    }

    Invoker invoker

    String serverContext

    String uri = '/api/agent/script/pull'

    @Override
    String name() {
        'script holder'
    }

    @Override
    void doJob() {
        Map<String, Long> params = [:]
        scripts.each { k, v ->
            params[k] = v.updatedDate.time
        }

        def serverUri = serverContext ? serverContext + uri : uri
        def body = invoker.request(serverUri, params, String)
        def arr = JSON.parseArray(body, OneScript)
        for (one in arr) {
            def exist = scripts[one.name]
            if (exist) {
                exist.content = one.content
                exist.updatedDate = one.updatedDate
            } else {
                scripts[one.name] = one
            }
            log.info 'script updated: {}', one.name
        }
    }
}
