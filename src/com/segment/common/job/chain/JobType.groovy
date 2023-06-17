package com.segment.common.job.chain

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TupleConstructor

@CompileStatic
@TupleConstructor
@EqualsAndHashCode
@ToString(includeNames = true)
class JobType {
    String name

    // save to database
    int value

}
