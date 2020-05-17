package cn.jia.task.schedule;

import cn.jia.base.config.security.SecurityConfiguration;
import cn.jia.core.entity.JSONResult;
import cn.jia.core.service.DictService;
import cn.jia.core.util.DateUtil;
import cn.jia.core.util.JSONUtil;
import cn.jia.core.util.StringUtils;
import cn.jia.task.common.Constants;
import cn.jia.task.entity.TaskItemVO;
import cn.jia.task.entity.TaskItemVOExample;
import cn.jia.task.service.TaskService;
import cn.jia.user.entity.Msg;
import cn.jia.user.entity.User;
import cn.jia.user.service.MsgService;
import cn.jia.user.service.UserService;
import cn.jia.wx.service.MpInfoService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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

	/**
	 * 任务到期通知
	 */
	@Scheduled(cron = "0 0/10 * * * ?")
	public void taskAlert() {
		List<String> phoneList = new ArrayList<>();
//		List<String> emailList = new ArrayList<>();
		List<String> openidList = new ArrayList<>();
		String wxAppId = dictService.selectByDictTypeAndDictValue(Constants.DICT_TYPE_TASK_CONFIG, Constants.TASK_CONFIG_WX_APP_ID).getName();
        String url = dictService.selectByDictTypeAndDictValue(Constants.DICT_TYPE_TASK_CONFIG, Constants.TASK_CONFIG_NOTIFY_URL).getName();

		TaskItemVOExample example = new TaskItemVOExample();
		example.setRemind(Constants.TASK_REMIND_YES);
		example.setStatus(Constants.COMMON_ENABLE);
		long now = DateUtil.genTime(new Date());
		example.setTimeStart(now);
		example.setTimeEnd(now + 10 * 60 - 1);
		List<TaskItemVO> taskList = taskService.findItems(example, 1, Integer.MAX_VALUE);
		for(TaskItemVO vo : taskList) {
			User user = userService.findByJiacn(vo.getJiacn());
			if(user != null) {
				String msgType = user.getMsgType();
				String phone = user.getPhone();
//				String email = user.getEmail();
				String openid = user.getOpenid();
				if(containMsgType(msgType, Constants.MESSAGE_TYPE_SMS) && StringUtils.isNotEmpty(phone)) {
					phoneList.add(phone);
				}
//				if(containMsgType(msgType, Constants.MESSAGE_TYPE_EMAIL) && StringUtils.isNotEmpty(email)) {
//					emailList.add(email);
//				}
				if(containMsgType(msgType, Constants.MESSAGE_TYPE_WX) && StringUtils.isNotEmpty(openid)
						&& StringUtils.isNotEmpty(user.getSubscribe())
						&& Arrays.asList(user.getSubscribe().split(",")).contains(wxAppId)) {
					openidList.add(openid);
				}
				
				String content = dictService.selectByDictTypeAndDictValue(Constants.DICT_TYPE_TASK_CONFIG, Constants.SMS_CONTENT_NOTIFY_MSG, "zh_CN").getName();
				content = content.replace("{0}", user.getNickname());
				
				if(phoneList.size() > 0) {
					phoneList = phoneList.stream().distinct().collect(Collectors.toList()); //去重
					HttpHeaders headers = new HttpHeaders();
					headers.set("Authorization", "Bearer " + SecurityConfiguration.jiaToken());
					MultiValueMap<String, String> variables = new LinkedMultiValueMap<>();
					variables.add("mobile", org.apache.commons.lang3.StringUtils.join(phoneList, ","));
					variables.add("content", content);
					HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(variables, headers);
					String jiaUrl = dictService.selectByDictTypeAndDictValue(cn.jia.user.common.Constants.DICT_TYPE_USER_CONFIG, cn.jia.user.common.Constants.USER_CONFIG_JIA_SERVER_URL).getName();
					restTemplate.postForObject(jiaUrl + "/sms/sendBatch", requestEntity, JSONResult.class);

                    Msg msg = new Msg();
                    msg.setType(Constants.MESSAGE_TYPE_WX);
                    msg.setUpdateTime(now);
                    msg.setCreateTime(now);
                    msg.setUserId(user.getId());
                    msg.setTitle("");
                    msg.setContent(content);
                    msg.setUrl(url);
                    msgService.create(msg);
				}
				
				/*if(emailList.size() > 0) {
					emailList = emailList.stream().distinct().collect(Collectors.toList()); //去重
					Map<String, Object> msg = new HashMap<>();
					msg.put("sender", "hy-api-oa");
					msg.put("receiver", String.join(";", emailList));
					msg.put("content", content);
					String url = dictService.selectByDictTypeAndDictValue(Constants.DICT_TYPE_TASK_CONFIG, Constants.TASK_CONFIG_NOTIFY_URL).getName();
					msg.put("url", url);
					msg.put("msgType", Constants.MESSAGE_TYPE_EMAIL);
					String sendMsg = JSONUtil.toJson(msg);
					log.info("ReleaseSchedule -> Send Email Message: " + sendMsg);
					rabbitTemplate.convertAndSend("jia.sms", sendMsg);
				}*/
				
				if(openidList.size() > 0) {
					List<WxMpTemplateData> data = new ArrayList<>();
					WxMpTemplateData keyword0 = new WxMpTemplateData();
					keyword0.setName("first");
					keyword0.setValue(content);
					keyword0.setColor("#173177");
					data.add(keyword0);
					String taskType = dictService.selectByDictTypeAndDictValue(Constants.DICT_TYPE_TASK_TYPE, String.valueOf(vo.getType())).getName();
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

					WxMpTemplateMessage message = new WxMpTemplateMessage();
					message.setToUser(openid);
					String templateId = dictService.selectByDictTypeAndDictValue(Constants.DICT_TYPE_TASK_CONFIG, Constants.TASK_CONFIG_WX_MSG_TEMPLATE_ID).getName();
					message.setTemplateId(templateId);
					message.setData(data);
					message.setUrl(url);
					log.info(JSONUtil.toJson(message));

                    try {
                        mpInfoService.findWxMpService(wxAppId).getTemplateMsgService().sendTemplateMsg(message);
                    } catch (Exception e) {
                        log.error("TaskSchedule.taskAlert", e);
                    }

                    Msg msg = new Msg();
					msg.setType(Constants.MESSAGE_TYPE_WX);
					msg.setUpdateTime(now);
					msg.setCreateTime(now);
					msg.setUserId(user.getId());
					msg.setTitle(content);
					msg.setContent(JSONUtil.toJson(message));
					msg.setUrl(url);
					msgService.create(msg);
				}

				if(StringUtils.isNotEmpty(vo.getRemindPhone())) {
					HttpHeaders headers = new HttpHeaders();
					headers.set("Authorization", "Bearer " + SecurityConfiguration.jiaToken());
					MultiValueMap<String, String> variables = new LinkedMultiValueMap<>();
                    variables.add("mobile", vo.getRemindPhone());
                    variables.add("content", vo.getRemindMsg());
					HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(variables, headers);
                    String jiaUrl = dictService.selectByDictTypeAndDictValue(cn.jia.user.common.Constants.DICT_TYPE_USER_CONFIG, cn.jia.user.common.Constants.USER_CONFIG_JIA_SERVER_URL).getName();
                    restTemplate.postForObject(jiaUrl + "/sms/sendBatch", requestEntity, JSONResult.class);
                }
			}
		}
	}
	
	private boolean containMsgType(String typeList, String type) {
		if(StringUtils.isEmpty(typeList)) {
			return false;
		}
		return Arrays.asList(typeList.split(",")).contains(type);
	}
}
