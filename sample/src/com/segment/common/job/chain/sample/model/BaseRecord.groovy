package com.segment.common.job.chain.sample.model


import groovy.transform.CompileStatic
import org.segment.d.D
import org.segment.d.Ds
import org.segment.d.Record
import org.segment.d.dialect.MySQLDialect

@CompileStatic
class BaseRecord<V extends BaseRecord> extends Record<V> {
    @Override
    String pk() {
        'id'
    }

    @Override
    D useD() {
        new D(Ds.one('test_ds'), new MySQLDialect())
    }
}
