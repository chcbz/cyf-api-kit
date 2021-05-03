package cn.jia.wx.schedule;

import cn.jia.base.entity.DelayObj;
import cn.jia.core.exception.EsRuntimeException;
import cn.jia.core.service.DictService;
import cn.jia.core.util.DateUtil;
import cn.jia.core.util.JSONUtil;
import cn.jia.core.util.thread.ThreadRequest;
import cn.jia.core.util.thread.ThreadRequestContent;
import cn.jia.kefu.entity.KefuMsgTypeCode;
import cn.jia.kefu.entity.KefuMsgType;
import cn.jia.kefu.service.KefuService;
import cn.jia.material.entity.Phrase;
import cn.jia.material.entity.VoteItem;
import cn.jia.material.entity.VoteQuestion;
import cn.jia.material.service.PhraseService;
import cn.jia.material.service.VoteService;
import cn.jia.task.common.TaskConstants;
import cn.jia.user.common.UserConstants;
import cn.jia.wx.entity.MpUser;
import cn.jia.wx.service.MpInfoService;
import cn.jia.wx.service.MpTemplateService;
import cn.jia.wx.service.MpUserService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.mp.bean.kefu.WxMpKefuMessage;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class WxSchedule {

	@Autowired
	private MpUserService mpUserService;
	@Autowired
	private VoteService voteService;
	@Autowired
	private MpInfoService mpInfoService;
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;
	@Autowired
	private DictService dictService;
	@Autowired
	private PhraseService phraseService;
	@Autowired
	private MpTemplateService mpTemplateService;
	@Autowired
	private KefuService kefuService;

	/**
	 * 每天7:30发送提问信息，同时生成每日一句处理队列
	 */
	@Scheduled(cron = "0 30 7 * * ?")
	public void sendVote() {
		List<MpUser> phraseUserList = new ArrayList<>();
		List<MpUser> userList = mpUserService.list();
		List<KefuMsgType> kefuMsgTypeList = kefuService.listMsgType();
		long twoDay = DateUtil.genTime(new Date()) - 2 * 24 * 60 * 60;
		for(MpUser user : userList) {
			if(user.getSubscribeItems() != null && Arrays.asList(user.getSubscribeItems().split(",")).contains(UserConstants.SUBSCRIBE_VOTE)) {
				VoteQuestion question = voteService.findOneQuestion(user.getJiacn());

				if (user.getUpdateTime() > twoDay) {
					try {
						WxMpKefuMessage kfmessage = new WxMpKefuMessage();
						kfmessage.setToUser(user.getOpenId());
						kfmessage.setMsgType(WxConsts.KefuMsgType.TEXT);
						StringBuilder content = new StringBuilder();
						content.append("每天答题时间(两小时有效)").append("\n");
						content.append(question.getTitle()).append("\n\n");
						for (VoteItem item : question.getItems()) {
							content.append(item.getOpt()).append(". ").append(item.getContent()).append("\n");
						}
						content.append("\n").append("请回复正确答案，退订回复TD");
						kfmessage.setContent(content.toString());
						mpInfoService.findWxMpService(user.getAppid()).getKefuService().sendKefuMessage(kfmessage);
						redisTemplate.opsForValue().set("vote_" + user.getOpenId(), question.getId(), 2, TimeUnit.HOURS);
						phraseUserList.add(user);
						continue;
					} catch (Exception e) {
						log.warn("sendVote.WxMpKefuMessage " + e.getMessage());
					}
				}
				try {
					List<WxMpTemplateData> data = new ArrayList<>();
					WxMpTemplateData keyword0 = new WxMpTemplateData();
					keyword0.setName("first");
					StringBuilder content = new StringBuilder("每天答题时间(两小时有效)\r\n\r\n").append(question.getTitle()).append("\r\n");
					for (VoteItem item : question.getItems()) {
						content.append("\r\n").append(item.getOpt()).append(". ").append(item.getContent());
					}
					keyword0.setValue(content.toString());
					keyword0.setColor("#173177");
					data.add(keyword0);
					WxMpTemplateData keyword1 = new WxMpTemplateData();
					keyword1.setName("keyword1");
					keyword1.setValue("知识普及");
					keyword1.setColor("#173177");
					data.add(keyword1);
					WxMpTemplateData keyword2 = new WxMpTemplateData();
					keyword2.setName("keyword2");
					keyword2.setValue("请回复正确答案，退订回复TD");
					keyword2.setColor("#173177");
					data.add(keyword2);
					WxMpTemplateData remark = new WxMpTemplateData();
					remark.setName("remark");
					remark.setValue("");
					remark.setColor("#173177");
					data.add(remark);

					WxMpTemplateMessage message = new WxMpTemplateMessage();
					message.setToUser(user.getOpenId());
					Optional<KefuMsgType> kefuMsgType = kefuMsgTypeList.stream().filter(item ->
							user.getClientId().equals(item.getClientId()) &&
									KefuMsgTypeCode.VOTE.getCode().equals(item.getTypeCode())).findFirst();
					message.setTemplateId(kefuMsgType.orElseThrow(() ->
							new EsRuntimeException("找不到模板")).getWxTemplateId());
					message.setData(data);
					String baseUrl = dictService.getValue(TaskConstants.DICT_TYPE_TASK_CONFIG, TaskConstants.TASK_CONFIG_NOTIFY_URL);
					message.setUrl(baseUrl + "/vote");
					mpInfoService.findWxMpService(user.getAppid()).getTemplateMsgService().sendTemplateMsg(message);
					redisTemplate.opsForValue().set("vote_" + user.getOpenId(), question.getId(), 2, TimeUnit.HOURS);
				} catch (Exception e) {
					log.error("sendVote.WxMpTemplateMessage", e);
				}
			}
		}

		//随机发送每日一句
		DelayQueue<DelayObj> delayQueue = new DelayQueue<>();
		for(MpUser user : phraseUserList) {
			int max = (int)(DateUtil.todayEnd().getTime() / 1000);
			int min = (int)(new Date().getTime() / 1000);
			Random random = new Random();
			long i = random.nextInt(max) % (max - min + 1) * 1000;
			delayQueue.offer(new DelayObj(i, JSONUtil.toJson(user)));
		}
		final int size = phraseUserList.size();
		new ThreadRequest(new ThreadRequestContent() {
			public void doSomeThing() {
				for(int i=0; i<size; i++) {
					try {
						DelayObj delayObj = delayQueue.take();
						Phrase phrase = phraseService.findRandom(null);
						MpUser user = JSONUtil.fromJson(delayObj.getData(), MpUser.class);
						WxMpKefuMessage kfmessage = new WxMpKefuMessage();
						kfmessage.setToUser(user.getOpenId());
						kfmessage.setMsgType(WxConsts.KefuMsgType.TEXT);
						kfmessage.setContent(phrase.getContent());
						mpInfoService.findWxMpService(user.getAppid()).getKefuService().sendKefuMessage(kfmessage);
					} catch (Exception e) {
						log.error("WxSchedule.sendPhrase", e);
					}
				}
			}
		}).start();
	}
}
