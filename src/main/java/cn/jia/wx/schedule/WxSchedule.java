package cn.jia.wx.schedule;

import cn.jia.base.entity.DelayObj;
import cn.jia.core.service.DictService;
import cn.jia.core.util.DateUtil;
import cn.jia.core.util.StringUtils;
import cn.jia.core.util.thread.ThreadRequest;
import cn.jia.core.util.thread.ThreadRequestContent;
import cn.jia.material.entity.Phrase;
import cn.jia.material.entity.VoteItem;
import cn.jia.material.entity.VoteQuestion;
import cn.jia.material.service.PhraseService;
import cn.jia.material.service.VoteService;
import cn.jia.user.common.Constants;
import cn.jia.user.entity.User;
import cn.jia.user.service.UserService;
import cn.jia.wx.service.MpInfoService;
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
	private UserService userService;
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

	/**
	 * 每天7:30发送提问信息，同时生成每日一句处理队列
	 */
	@Scheduled(cron = "0 30 7 * * ?")
	public void sendVote() {
		List<User> phraseUserList = new ArrayList<>();
		List<User> userList = userService.list(null, 1, Integer.MAX_VALUE);
		long twoDay = DateUtil.genTime(new Date()) - 2 * 24 * 60 * 60;
		String wxAppId = dictService.selectByDictTypeAndDictValue(cn.jia.task.common.Constants.DICT_TYPE_TASK_CONFIG, cn.jia.task.common.Constants.TASK_CONFIG_WX_APP_ID).getName();
		for(User user : userList) {
			if(StringUtils.isNotEmpty(user.getSubscribe()) && StringUtils.isNotEmpty(user.getOpenid())
					&& Arrays.asList(user.getSubscribe().split(",")).contains(Constants.SUBSCRIBE_VOTE)
					&& Arrays.asList(user.getSubscribe().split(",")).contains(wxAppId)) {
				VoteQuestion question = voteService.findOneQuestion(user.getJiacn());

				if (user.getUpdateTime() > twoDay) {
					try {
						WxMpKefuMessage kfmessage = new WxMpKefuMessage();
						kfmessage.setToUser(user.getOpenid());
						kfmessage.setMsgType(WxConsts.KefuMsgType.TEXT);
						StringBuilder content = new StringBuilder();
						content.append("每天答题时间(两小时有效)").append("\n");
						content.append(question.getTitle()).append("\n\n");
						for (VoteItem item : question.getItems()) {
							content.append(item.getOpt()).append(". ").append(item.getContent()).append("\n");
						}
						content.append("\n").append("请回复正确答案，退订回复TD");
						kfmessage.setContent(content.toString());
						mpInfoService.findWxMpService(wxAppId).getKefuService().sendKefuMessage(kfmessage);
						redisTemplate.opsForValue().set("vote_" + user.getOpenid(), question.getId(), 2, TimeUnit.HOURS);
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
					keyword1.setValue("保险知识普及");
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
					message.setToUser(user.getOpenid());
					String templateId = dictService.selectByDictTypeAndDictValue(cn.jia.task.common.Constants.DICT_TYPE_TASK_CONFIG, cn.jia.task.common.Constants.TASK_CONFIG_WX_MSG_TEMPLATE_ID).getName();
					message.setTemplateId(templateId);
					message.setData(data);
					String baseUrl = dictService.selectByDictTypeAndDictValue(cn.jia.task.common.Constants.DICT_TYPE_TASK_CONFIG, cn.jia.task.common.Constants.TASK_CONFIG_NOTIFY_URL).getName();
					message.setUrl(baseUrl + "/vote");
					mpInfoService.findWxMpService(wxAppId).getTemplateMsgService().sendTemplateMsg(message);
					redisTemplate.opsForValue().set("vote_" + user.getOpenid(), question.getId(), 2, TimeUnit.HOURS);
				} catch (Exception e) {
					log.error("sendVote.WxMpTemplateMessage", e);
				}
			}
		}

		//随机发送每日一句
		DelayQueue<DelayObj> delayQueue = new DelayQueue<>();
		for(User user : phraseUserList) {
			int max = (int)(DateUtil.todayEnd().getTime() / 1000);
			int min = (int)(new Date().getTime() / 1000);
			Random random = new Random();
			long i = random.nextInt(max) % (max - min + 1) * 1000;
			delayQueue.offer(new DelayObj(i, user.getOpenid()));
		}
		final int size = phraseUserList.size();
		new ThreadRequest(new ThreadRequestContent() {
			public void doSomeThing() {
				for(int i=0; i<size; i++) {
					try {
						DelayObj delayObj = delayQueue.take();
						Phrase phrase = phraseService.findRandom(null);
						WxMpKefuMessage kfmessage = new WxMpKefuMessage();
						kfmessage.setToUser(delayObj.getData());
						kfmessage.setMsgType(WxConsts.KefuMsgType.TEXT);
						kfmessage.setContent(phrase.getContent());
						mpInfoService.findWxMpService(wxAppId).getKefuService().sendKefuMessage(kfmessage);
					} catch (Exception e) {
						log.error("WxSchedule.sendPhrase", e);
					}
				}
			}
			public void onSuccess() {}
		}).start();
	}
}
