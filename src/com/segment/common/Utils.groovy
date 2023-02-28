package com.segment.common

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.commons.net.telnet.TelnetClient

@CompileStatic
@Slf4j
class Utils {
    static void stopWhenConsoleQuit(Closure<Void> closure, InputStream is = null) {
        boolean isStopped = false
        Runtime.addShutdownHook {
            if (!isStopped) {
                closure.call()
            }
        }

        if (Conf.isWindows()) {
            Thread.start {
                def br = new BufferedReader(new InputStreamReader(is ?: System.in))
                while (true) {
                    if (br.readLine() == 'quit') {
                        println 'quit from console...'
                        closure.call()
                        isStopped = true
                        break
                    }
                }
            }
        }
    }

    static String getStackTraceString(Throwable t) {
        if (!t) {
            return ''
        }

        def writer = new StringWriter()
        def pw = new PrintWriter(writer)
        t.printStackTrace(pw)
        pw.flush()
        writer.flush()
        pw.close()
        writer.close()
        writer.toString()
    }

    static String localIpCached = null

    static String localIp(String pre = null) {
        if (localIpCached != null) {
            return localIpCached
        }

        String preFinal
        if (pre == null) {
            preFinal = Conf.instance.getString('localIpFilterPre', '192.')
        } else {
            preFinal = pre
        }
        for (it in NetworkInterface.getNetworkInterfaces()) {
            def name = it.name
            if (!name.contains('docker') && !name.contains('lo')) {
                for (address in it.getInetAddresses()) {
                    if (!address.isLoopbackAddress()) {
                        String ipAddr = address.hostAddress
                        if (!ipAddr.contains("::") && !ipAddr.contains("0:0:") && !ipAddr.contains("fe80")) {
                            if (ipAddr.startsWith(preFinal)) {
                                localIpCached = ipAddr
                                return localIpCached
                            }
                        }
                    }
                }
            }
        }
        localIpCached = InetAddress.localHost.hostAddress
        localIpCached
    }

    static String uuid(String pre = '', int len = 8) {
        def rand = new Random()
        List az = 0..9
        int size = az.size()
        def sb = new StringBuilder(pre)
        for (int i = 0; i < len; i++) {
            sb << az[rand.nextInt(size)]
        }
        sb.toString()
    }

    static boolean isPortListenAvailable(int port, String host = '127.0.0.1') {
        def tc = new TelnetClient(connectTimeout: 500)
        try {
            tc.connect(host, port)
            return false
        } catch (Exception e) {
            return true
        } finally {
            tc.disconnect()
        }
    }
}
