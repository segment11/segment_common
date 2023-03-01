package com.segment.common

import groovy.transform.CompileStatic

@CompileStatic
@Singleton
class Conf {

    static boolean isWindows() {
        System.getProperty('os.name').toLowerCase().contains('windows')
    }

    private String workDir

    Conf resetWorkDir() {
        workDir = new File('.').absolutePath.replaceAll("\\\\", '/').
                replaceAll(/\/src\/.*/, '')
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
