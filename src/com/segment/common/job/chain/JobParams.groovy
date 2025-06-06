package com.segment.common.job.chain

import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.transform.TupleConstructor

@CompileStatic
@TupleConstructor
@ToString(includeNames = true)
class JobParams {
    Map<String, String> params = [:]

    String get(String key) {
        params[key]
    }

    String getString(String key, String defaultValue) {
        def val = get(key)
        return val == null ? defaultValue : val
    }

    int getInt(String key, int defaultValue) {
        def val = get(key)
        return val == null ? defaultValue : val as int
    }

    double getDouble(String key, double defaultValue) {
        def val = get(key)
        return val == null ? defaultValue : val as double
    }

    void put(String key, String value) {
        params[key] = value
    }

    boolean asBoolean() {
        params.size() > 0
    }
}
