package cn.jia.task.schedule;

import cn.jia.core.exception.EsRuntimeException;
import cn.jia.core.service.DictService;
import cn.jia.core.util.DateUtil;
import cn.jia.core.util.JSONUtil;
import cn.jia.core.util.StringUtils;
import cn.jia.kefu.entity.KefuMsgType;
import cn.jia.kefu.entity.KefuMsgTypeCode;
import cn.jia.kefu.service.KefuService;
import cn.jia.task.common.TaskConstants;
import cn.jia.task.entity.TaskItemVO;
import cn.jia.task.entity.TaskItemVOExample;
import cn.jia.task.service.TaskService;
import cn.jia.user.entity.Msg;
import cn.jia.user.entity.User;
import cn.jia.user.entity.UserExample;
import cn.jia.user.service.MsgService;
import cn.jia.user.service.UserService;
import cn.jia.wx.entity.MpInfo;
import cn.jia.wx.entity.MpInfoExample;
import cn.jia.wx.service.MpInfoService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

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
    private MpInfoService mpInfoService;
    @Autowired
    private MsgService msgService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private KefuService kefuService;

    /**
     * 任务到期通知
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void taskAlert() {
        List<String> openidList = new ArrayList<>();
        String url = dictService.getValue(TaskConstants.DICT_TYPE_TASK_CONFIG, TaskConstants.TASK_CONFIG_NOTIFY_URL);

        TaskItemVOExample example = new TaskItemVOExample();
        example.setRemind(TaskConstants.TASK_REMIND_YES);
        example.setStatus(TaskConstants.COMMON_ENABLE);
        long now = DateUtil.genTime(new Date());
        example.setTimeStart(now);
        example.setTimeEnd(now + 10 * 60 - 1);
        List<TaskItemVO> taskList = taskService.findItems(example, 1, Integer.MAX_VALUE);
        List<String> jiacns = taskList.stream().map(TaskItemVO::getJiacn).distinct().collect(Collectors.toList());
        UserExample userExample = new UserExample();
        userExample.setJiacnList(jiacns);
        List<User> userList = userService.list(userExample);
        Map<String, User> userJiacnMap = userList.stream().collect(Collectors.toMap(User::getJiacn, ee -> ee));
        MpInfoExample mpInfoExample = new MpInfoExample();
        List<String> clientIds = taskList.stream().map(TaskItemVO::getClientId).distinct().collect(Collectors.toList());
        mpInfoExample.setClientIdList(clientIds);
        List<MpInfo> mpInfoList = mpInfoService.list(mpInfoExample);
        Map<String, String> appIdMap = mpInfoList.stream().collect(Collectors.toMap(MpInfo::getClientId, MpInfo::getAppid));
        List<KefuMsgType> kefuMsgTypeList = kefuService.listMsgType();

        for (TaskItemVO vo : taskList) {
            User user = userJiacnMap.get(vo.getJiacn());
            if (user != null) {
                String msgType = user.getMsgType();
                String openid = user.getOpenid();
                if (containMsgType(msgType, TaskConstants.MESSAGE_TYPE_WX) && StringUtils.isNotEmpty(openid)
                        && StringUtils.isNotEmpty(user.getSubscribe())
                        && Arrays.asList(user.getSubscribe().split(",")).contains("wxAppId")) {
                    openidList.add(openid);
                }

                String content = dictService.getValue(TaskConstants.DICT_TYPE_TASK_CONFIG, TaskConstants.SMS_CONTENT_NOTIFY_MSG, "zh_CN");
                content = content.replace("{0}", user.getNickname());

                if (openidList.size() > 0) {
                    List<WxMpTemplateData> data = new ArrayList<>();
                    WxMpTemplateData keyword0 = new WxMpTemplateData();
                    keyword0.setName("first");
                    keyword0.setValue(content);
                    keyword0.setColor("#173177");
                    data.add(keyword0);
                    String taskType = dictService.getValue(TaskConstants.DICT_TYPE_TASK_TYPE, String.valueOf(vo.getType()));
                    WxMpTemplateData keyword1 = new WxMpTemplateData();
                    keyword1.setName("keyword1");
                    keyword1.setValue(taskType);
                    keyword1.setColor("#173177");
                    data.add(keyword1);
                    WxMpTemplateData keyword2 = new WxMpTemplateData();
                    keyword2.setName("keyword2");
                    keyword2.setValue(vo.getName());
                    keyword2.setColor("#173177");
                    data.add(keyword2);
                    WxMpTemplateData remark = new WxMpTemplateData();
                    remark.setName("remark");
                    remark.setValue(vo.getDescription());
                    remark.setColor("#173177");
                    data.add(remark);

                    try {
                        WxMpTemplateMessage message = new WxMpTemplateMessage();
                        message.setToUser(openid);
                        Optional<KefuMsgType> kefuMsgType = kefuMsgTypeList.stream().filter(item ->
                                vo.getClientId().equals(item.getClientId()) && KefuMsgTypeCode.TASK.getCode().equals(item.getTypeCode())).findFirst();
                        String templateId = kefuMsgType.orElseThrow(() -> new EsRuntimeException("找不到模板")).getWxTemplateId();
                        message.setTemplateId(templateId);
                        message.setData(data);
                        message.setUrl(url);
                        log.info(JSONUtil.toJson(message));

                        mpInfoService.findWxMpService(appIdMap.get(vo.getClientId())).getTemplateMsgService().sendTemplateMsg(message);

                        Msg msg = new Msg();
                        msg.setType(TaskConstants.MESSAGE_TYPE_WX);
                        msg.setUpdateTime(now);
                        msg.setCreateTime(now);
                        msg.setUserId(user.getId());
                        msg.setTitle(content);
                        msg.setContent(JSONUtil.toJson(message));
                        msg.setUrl(url);
                        msgService.create(msg);
                    } catch (Exception e) {
                        log.error("TaskSchedule.taskAlert", e);
                    }
                }
            }
        }
    }

    private boolean containMsgType(String typeList, String type) {
        if (StringUtils.isEmpty(typeList)) {
            return false;
        }
        return Arrays.asList(typeList.split(",")).contains(type);
    }
}
