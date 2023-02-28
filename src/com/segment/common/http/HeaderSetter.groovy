package com.segment.common.http

import com.github.kevinsawicki.http.HttpRequest
import groovy.transform.CompileStatic

@CompileStatic
interface HeaderSetter {
    void set(HttpRequest req)
}
