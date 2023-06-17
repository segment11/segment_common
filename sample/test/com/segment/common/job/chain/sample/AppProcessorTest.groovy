package com.segment.common.job.chain.sample

import com.segment.common.job.chain.JobStatus
import com.segment.common.job.chain.sample.model.*
import com.segment.common.job.chain.sample.model.json.ExtendParams
import com.segment.common.job.chain.sample.task.RedisCreateTask
import com.segment.common.job.chain.sample.task.RedisSlotSetTask
import org.segment.d.D
import org.segment.d.Ds
import org.segment.d.dialect.MySQLDialect
import spock.lang.Specification

class AppProcessorTest extends Specification {
    static Ds ds

    def setupSpec() {
        ds = Ds.h2mem('test').cacheAs('test_ds')
        def d = new D(ds, new MySQLDialect())
        d.exe('''
create table app (
    id int auto_increment primary key,
    name varchar(50),
    status int,
    message varchar(1000)
);

create table app_job (
    id int auto_increment primary key,
    app_id int,
    type int,
    status int,
    failed_num int,
    message text,
    creator varchar(20),
    params varchar(1000),
    cost_ms int,
    created_date timestamp,
    updated_date timestamp default current_timestamp
);
create index idx_app_job_app_id on app_job(app_id);

create table app_job_log (
    id int auto_increment primary key,
    job_id int,
    step varchar(100),
    is_ok bit,
    message text,
    params varchar(2000),
    cost_ms int,
    created_date timestamp,
    updated_date timestamp default current_timestamp
);
create index idx_app_job_log_job_id on app_job_log(job_id);
create index idx_app_job_log_created_date on app_job_log(created_date);
''')

        println 'done create tables'
    }

    def cleanupSpec() {
        println 'clean up...'
        AppProcessor.stopRunning()

        println 'clean database rows'
        println 'delete app rows: ' + new AppDTO().where('id <= 2').deleteAll()
        println 'delete app job rows: ' + new AppJobDTO().where('id <= 2').deleteAll()
        println 'delete app job log rows: ' + new AppJobLogDTO().where('id <= 4').deleteAll()

        ds.closeConnect()
    }

    void "All"() {
        given:
        def app1 = new AppDTO(name: 'redis-singleton-0')
        app1.id = app1.add()
        def app2 = new AppDTO(name: 'redis-cluster-0')
        app2.id = app2.add()

        def job11 = new AppJobDTO(appId: app1.id,
                type: JobTypeEnum.create.value,
                status: JobStatus.created.value,
                failedNum: 0,
                creator: 'tester',
                params: new ExtendParams(),
                costMs: -1,
                createdDate: new Date(),
                updatedDate: new Date())
        job11.id = job11.add()

        def job21 = new AppJobDTO(appId: app2.id,
                type: JobTypeEnum.create.value,
                status: JobStatus.created.value,
                failedNum: 0,
                creator: 'tester',
                params: new ExtendParams(),
                costMs: -1,
                createdDate: new Date(),
                updatedDate: new Date())
        job21.id = job21.add()

        and:
        def appJob1 = new AppJob(app1, job11)
        appJob1.taskList << new RedisCreateTask(job: appJob1, step: JobStepEnum.first.one())

        def appJob2 = new AppJob(app2, job21)
        appJob2.taskList << new RedisCreateTask(job: appJob2, step: JobStepEnum.first.one())
        appJob2.taskList << new RedisSlotSetTask(job: appJob2, step: JobStepEnum.second.one(), subStep: 'set all')
        appJob2.taskList << new RedisSlotSetTask(job: appJob2, step: JobStepEnum.second.one(), subStep: 'migrate some')

        and:
        AppProcessor.run(appJob1)
        AppProcessor.run(appJob1)
        AppProcessor.run(appJob2)
        AppProcessor.run(appJob2)

        println 'wait 1s...'
        Thread.sleep(2000)

        def job11LogList = new AppJobLogDTO(jobId: job11.id).list()
        def job21LogList = new AppJobLogDTO(jobId: job21.id).list()
        expect:

        job11LogList.size() == 1
        job21LogList.size() == 3

        job11LogList[0].step == JobStepEnum.first.name()
        job11LogList[0].isOk

        job21LogList[0].step == JobStepEnum.first.name()
        job21LogList[0].isOk

        job21LogList[1].step.contains JobStepEnum.second.name()
        job21LogList[1].isOk

        job21LogList[2].step.contains JobStepEnum.second.name()
        job21LogList[2].isOk
    }

}
