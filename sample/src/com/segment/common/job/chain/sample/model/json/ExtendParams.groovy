package com.segment.common.job.chain.sample.model.json

import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.transform.TupleConstructor
import org.segment.d.json.JSONFiled

@CompileStatic
@TupleConstructor
@ToString(includeNames = true)
class ExtendParams implements JSONFiled {
    Map<String, String> params = [:]

    String get(String key) {
        if (!params) {
            return null
        }
        params[key]
    }

    void put(String key, String value) {
        params[key] = value
    }

    boolean asBoolean() {
        params != null && params.size() > 0
    }
}
