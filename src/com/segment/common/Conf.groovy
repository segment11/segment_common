package com.segment.common

import groovy.transform.CompileStatic

import java.util.regex.Pattern

@CompileStatic
@Singleton
class Conf {

    static String platform

    static String arch

    static String platformArch

    static {
        def osName = System.properties['os.name'].toString().replaceAll(' ', '').toLowerCase()
        Map<String, Pattern> osMap = [:]
        osMap['windows'] = ~/.*win.*/
        osMap['linux'] = ~/.*linux.*/
        osMap['macosx'] = ~/.*darwin.*/

        platform = osMap.find { osName ==~ it.value }?.key
        arch = System.properties['os.arch'] ==~ /.*64.*/ ? 'x86_64' : 'i386'
        platformArch = "${platform}-${arch}".toString()
    }

    static boolean isWindows() {
        platform == 'windows'
    }

    private String workDir

    Conf resetWorkDir(boolean isTestDir = false) {
        workDir = new File('.').absolutePath.replaceAll("\\\\", '/').
                replaceAll(/\/src\/.*/, '')
        if (isTestDir) {
            workDir = workDir.replaceAll(/\/test\/.*/, '')
        }

        if (workDir[-1] == '.') {
            workDir = workDir[0..-2]
        }
        if (workDir[-1] == '/') {
            workDir = workDir[0..-2]
        }
        put('workDir', workDir)
        this
    }

    String projectPath(String relativePath = '') {
        workDir + relativePath
    }

    Map<String, String> params = [:]

    Map<String, String> filter(String prefix) {
        Map<String, String> x = [:]
        params.findAll { it.key.startsWith(prefix) }.each { k, v ->
            x[k[prefix.length()..-1]] = v
        }
        x
    }

    Conf load() {
        def confFile = new File(projectPath('/conf.properties'))
        def confFileDev = new File(projectPath('/src/conf_dev.properties'))
        def isLoad = loadFromFile(confFile)
        if (!isLoad) {
            def isLoadDev = loadFromFile(confFileDev)
            if (!isLoadDev) {
                def resource = this.class.classLoader.getResource('conf.properties')
                if (resource) {
                    loadFromFile(new File(resource.file))
                } else {
                    throw new RuntimeException('conf.properties not found')
                }
            }
        }
        this
    }

    Conf loadArgs(String[] args) {
        if (!args) {
            return this
        }
        for (it in args) {
            def arr = it.split('=')
            if (arr.length >= 2) {
                params[arr[0]] = arr[1..-1].join('=')
            }
        }
        this
    }

    private boolean loadFromFile(File confFile) {
        if (!confFile.exists()) {
            return false
        }
        confFile.readLines().findAll { it.trim() && !it.startsWith('#') }.each {
            def arr = it.split('=')
            if (arr.length >= 2) {
                params[arr[0]] = arr[1..-1].join('=')
            }
        }
        true
    }

    boolean isDev() {
        'dev' == get('env')
    }

    String get(String key) {
        params[key]
    }

    String getString(String key, String defaultValue) {
        get(key) ?: defaultValue
    }

    int getInt(String key, int defaultValue) {
        def s = get(key)
        s ? s as int : defaultValue
    }

    boolean isOn(String key) {
        '1' == get(key)
    }

    Conf put(String key, Object value) {
        params[key] = value.toString()
        this
    }

    Conf on(String key) {
        put(key, 1)
    }

    Conf off(String key) {
        put(key, 0)
    }

    @Override
    String toString() {
        params.toString()
    }
}
