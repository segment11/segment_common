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

    void put(String key, String value) {
        params[key] = value
    }

    boolean asBoolean() {
        params.size() > 0
    }
}
