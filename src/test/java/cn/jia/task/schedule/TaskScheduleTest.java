package cn.jia.task.schedule;

import cn.jia.core.util.DateUtil;
import cn.jia.task.entity.TaskPlanEntity;
import cn.jia.task.service.TaskService;
import cn.jia.test.BaseDbUnitTest;
import cn.jia.test.DbUnitHelper;
import org.springframework.test.context.jdbc.Sql;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;

@Slf4j
class TaskScheduleTest extends BaseDbUnitTest {
    @Autowired
    private TaskSchedule taskSchedule;
    @Autowired
    private TaskService taskService;
    @Autowired
    private DataSource dataSource;
    @Value("classpath:testObject/task/task_plan_init.json")
    private Resource resource;

    @Test
    @Sql(scripts = {
            "classpath:testObject/kefu/kefu_msg_type_init.sql",
            "classpath:testObject/wx/mp_info_init.sql",
            "classpath:testObject/wx/mp_template_init.sql",
            "classpath:testObject/wx/mp_user_init.sql",
            "classpath:testObject/kefu/kefu_msg_subscribe_init.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void taskAlert() {
        TaskPlanEntity taskPlan = DbUnitHelper.readJsonEntity(resource, TaskPlanEntity.class);
        taskPlan.setStartTime(DateUtil.nowTime() + 50);
        taskPlan.setEndTime(DateUtil.nowTime() + 100);
        taskPlan.setClientId("jia_client");
        taskService.create(taskPlan);
        taskSchedule.taskAlert();
    }

    @Test
    void printDataSet() {
        log.info(DbUnitHelper.printDataSet(dataSource, "user_info", "select * from user_info where id=5"));
    }
}