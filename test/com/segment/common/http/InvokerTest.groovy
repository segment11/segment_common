package com.segment.common.http

import com.github.kevinsawicki.http.HttpRequest
import com.segment.common.Utils
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import spock.lang.Specification

import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class InvokerTest extends Specification {

    static class ResultMock {
        String flag
    }

    def 'test request'() {
        given:
        def server = new Server()
        def handler = new ServletContextHandler()
        handler.addServlet(new ServletHolder(new HttpServlet() {
            void end(HttpServletResponse response, String body) {
                def os = response.outputStream
                os.write(body.bytes)
                os.flush()
                os.close()
            }

            @Override
            protected void service(HttpServletRequest request, HttpServletResponse response)
                    throws ServletException, IOException {
                def uri = request.getRequestURI()
                if (uri.contains('error')) {
                    response.status = 500
                    end(response, 'error')
                } else {
                    response.status = 200
                    end(response, '{"flag":"ok"}')
                }
            }
        }), "/*")
        server.handler = handler

        def connector = new ServerConnector(server)
        connector.host = Utils.localIp()
        connector.port = 5000
        server.addConnector(connector)
        server.start()
        and:
        def invoker = new Invoker()
        invoker.serverHost = Utils.localIp()
        invoker.serverPort = 5000
        invoker.headerSetter = { HttpRequest req ->
            req.header('X-key', 'X-value')
        }
        expect:
        invoker.request('/test') == '{"flag":"ok"}'
        invoker.request('/test', [:], ResultMock).flag == 'ok'
        invoker.request('/test/error', null, null, { String body ->
            println body
        }, true) == 'error'
        cleanup:
        server.stop()
    }
}
