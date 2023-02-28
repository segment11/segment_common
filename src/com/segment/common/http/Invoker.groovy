package com.segment.common.http

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.github.kevinsawicki.http.HttpRequest
import com.segment.common.Conf
import com.segment.common.Utils
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@CompileStatic
@Slf4j
class Invoker {
    String serverAddr

    String serverScheme = 'http://'

    String serverHost

    int serverPort = 80

    int connectTimeout = 500

    int readTimeout = 2000

    void init(String prefix = '') {
        def c = Conf.instance
        serverScheme = c.getString(prefix + 'serverScheme', 'http://')
        serverHost = c.get(prefix + 'serverHost')
        serverPort = c.getInt(prefix + 'serverPort', 5010)
        connectTimeout = c.getInt(prefix + 'server.connectTimeout.ms', 500)
        readTimeout = c.getInt(prefix + 'server.readTimeout.ms', 2000)
    }

    HeaderSetter headerSetter

    String json(Object obj) {
        def mapper = new ObjectMapper()
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        mapper.serializationInclusion = JsonInclude.Include.NON_NULL
        mapper.writeValueAsString(obj)
    }

    private <T> T readJson(String string, Class<T> clz) {
        new ObjectMapper().readValue(string, clz)
    }

    public <T> T request(String uri, Map params = null, Class<T> clz = String,
                         Closure<Void> failCallback = null, boolean isPost = false) {
        def addr = serverAddr ?: (serverScheme + serverHost + ':' + serverPort)

        try {
            def req = isPost ? HttpRequest.post(addr + uri) :
                    HttpRequest.get(addr + uri, params ?: [:], true)

            int readTimeoutFinal
            if (params?.readTimeout) {
                readTimeoutFinal = params.readTimeout as int
            } else {
                readTimeoutFinal = readTimeout
            }
            req.connectTimeout(connectTimeout).readTimeout(readTimeoutFinal)

            if (headerSetter) {
                headerSetter.set(req)
            }

            if (isPost) {
                def sendBody = json(params ?: [:])
                req.send(sendBody)
            }
            def body = req.body()
            if (req.code() != 200) {
                if (failCallback) {
                    log.warn('http request fail, uri: {}, params: {}, body: {}', uri, params, body)
                    failCallback.call(body)
                    body
                } else {
                    throw new HttpInvokeException('http request fail - ' + uri + ' - ' + params + ' - ' + body)
                }
            }
            if (clz == null || clz == String) {
                return body as T
            }
            readJson(body, clz)
        } catch (Exception e) {
            if (failCallback) {
                failCallback.call(Utils.getStackTraceString(e))
                null
            } else {
                throw e
            }
        }
    }
}
