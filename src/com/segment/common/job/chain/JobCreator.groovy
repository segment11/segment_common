package com.segment.common.job.chain

import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.transform.TupleConstructor

@CompileStatic
@TupleConstructor
@ToString(includeNames = true)
class JobCreator {
    String name
}
