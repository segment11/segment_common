package com.segment.common.job.chain.sample.model

import com.segment.common.job.chain.sample.model.json.ExtendParams
import groovy.transform.CompileStatic

@CompileStatic
class AppJobDTO extends BaseRecord<AppJobDTO> {
    Integer id

    Integer appId

    Integer type

    Integer status

    Integer failedNum

    String message

    String creator

    ExtendParams params

    Integer costMs

    Date createdDate

    Date updatedDate

}
