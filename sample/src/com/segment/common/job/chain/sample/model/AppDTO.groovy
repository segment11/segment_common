package com.segment.common.job.chain.sample.model

import groovy.transform.CompileStatic

@CompileStatic
class AppDTO extends BaseRecord<AppDTO> {
    Integer id

    String name

    Integer status

    String message

    void updateStatus(AppStatus status, String message = null) {
        assert id
        new AppDTO(id: id, status: status.value, message: message).update()
    }
}
