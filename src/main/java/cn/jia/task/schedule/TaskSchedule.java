package cn.jia.task.schedule;

import cn.jia.core.service.DictService;
import cn.jia.core.util.DateUtil;
import cn.jia.kefu.entity.KefuMsgSubscribe;
import cn.jia.kefu.entity.KefuMsgType;
import cn.jia.kefu.entity.KefuMsgTypeCode;
import cn.jia.kefu.service.KefuService;
import cn.jia.task.common.TaskConstants;
import cn.jia.task.entity.TaskItemVO;
import cn.jia.task.entity.TaskItemVOExample;
import cn.jia.task.service.TaskService;
import cn.jia.user.entity.User;
import cn.jia.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class TaskSchedule {

    @Autowired
    private TaskService taskService;
    @Autowired
    private DictService dictService;
    @Autowired
    private UserService userService;
    @Autowired
    private KefuService kefuService;

    /**
     * 任务到期通知
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void taskAlert() {
        TaskItemVOExample example = new TaskItemVOExample();
        example.setRemind(TaskConstants.TASK_REMIND_YES);
        example.setStatus(TaskConstants.COMMON_ENABLE);
        long now = DateUtil.genTime(new Date());
        example.setTimeStart(now);
        example.setTimeEnd(now + 10 * 60 - 1);
        List<TaskItemVO> taskList = taskService.findItems(example, 1, Integer.MAX_VALUE);
        for (TaskItemVO vo : taskList) {
            KefuMsgType kefuMsgType= kefuService.findMsgType(vo.getClientId(), KefuMsgTypeCode.TASK.getCode());
            KefuMsgSubscribe kefuMsgSubscribe = kefuService.findMsgSubscribe(vo.getClientId(), KefuMsgTypeCode.TASK.getCode(), vo.getJiacn());
            User user = userService.findByJiacn(vo.getJiacn());
            if (user != null && kefuMsgType != null && kefuMsgSubscribe != null) {
                String taskType = dictService.getValue(TaskConstants.DICT_TYPE_TASK_TYPE, String.valueOf(vo.getType()));
                try {
                    kefuService.sendTemplate(kefuMsgType, kefuMsgSubscribe, user.getNickname(), taskType, vo.getName(), vo.getDescription());
                } catch (Exception e) {
                    log.error("TaskSchedule.taskAlert", e);
                }
            }
        }
    }
}
