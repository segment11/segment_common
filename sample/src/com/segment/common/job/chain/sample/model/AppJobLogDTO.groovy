package com.segment.common.job.chain.sample.model

import com.segment.common.job.chain.sample.model.json.ExtendParams
import groovy.transform.CompileStatic

@CompileStatic
class AppJobLogDTO extends BaseRecord<AppJobLogDTO> {
    Integer id

    Integer jobId

    String step

    Boolean isOk

    String message

    ExtendParams params

    Integer costMs

    Date createdDate

    Date updatedDate

    String param(String key) {
        params?.get(key)
    }

    AppJobLogDTO addParam(String key, String value) {
        if (params == null) {
            params = new ExtendParams()
        }
        params.put(key, value)
        this
    }

}
