package cn.jia.wx.schedule;

import cn.jia.base.entity.DelayObj;
import cn.jia.core.util.DateUtil;
import cn.jia.core.util.JSONUtil;
import cn.jia.core.util.StringUtils;
import cn.jia.core.util.thread.ThreadRequest;
import cn.jia.core.util.thread.ThreadRequestContent;
import cn.jia.kefu.entity.KefuMsgSubscribe;
import cn.jia.kefu.entity.KefuMsgType;
import cn.jia.kefu.entity.KefuMsgTypeCode;
import cn.jia.kefu.service.KefuService;
import cn.jia.material.entity.Phrase;
import cn.jia.material.entity.VoteItem;
import cn.jia.material.entity.VoteQuestion;
import cn.jia.material.service.PhraseService;
import cn.jia.material.service.VoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class WxSchedule {
	@Autowired
	private VoteService voteService;
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;
	@Autowired
	private PhraseService phraseService;
	@Autowired
	private KefuService kefuService;

	/**
	 * 每天7:30发送提问信息，同时生成每日一句处理队列
	 */
	@Scheduled(cron = "0 30 7 * * ?")
	public void sendVote() {
		KefuMsgSubscribe subscribe = new KefuMsgSubscribe();
		subscribe.setTypeCode(KefuMsgTypeCode.VOTE.getCode());
		List<KefuMsgSubscribe> subscribeList = kefuService.listMsgSubscribe(subscribe);
		for (KefuMsgSubscribe kefuMsgSubscribe : subscribeList) {
			KefuMsgType kefuMsgType = kefuService.findMsgType(kefuMsgSubscribe.getClientId(), kefuMsgSubscribe.getTypeCode());
			VoteQuestion question = voteService.findOneQuestion(kefuMsgSubscribe.getJiacn());
			String title = question.getTitle();
			StringBuilder content = new StringBuilder();
			for (VoteItem item : question.getItems()) {
				if (StringUtils.isNotEmpty(content)) {
					content.append("\\n");
				}
				content.append(item.getOpt()).append(". ").append(item.getContent());
			}
			try {
				boolean sendSuccess = kefuService.sendWxTemplate(kefuMsgType, kefuMsgSubscribe, title, content.toString());
				if (sendSuccess) {
					redisTemplate.opsForValue().set("vote_" + kefuMsgSubscribe.getJiacn(), question.getId(), 2, TimeUnit.HOURS);
				}
			} catch (Exception e) {
				log.error("sendVote.WxMpTemplateMessage", e);
			}
		}

		//随机发送每日一句
		DelayQueue<DelayObj> delayQueue = new DelayQueue<>();
		subscribe.setTypeCode(KefuMsgTypeCode.PHRASE.getCode());
		subscribeList = kefuService.listMsgSubscribe(subscribe);
		for (KefuMsgSubscribe kefuMsgSubscribe : subscribeList) {
			int max = (int)(DateUtil.todayEnd().getTime() / 1000);
			int min = (int)(new Date().getTime() / 1000);
			Random random = new Random();
			long i = random.nextInt(max) % (max - min + 1) * 1000;
			delayQueue.offer(new DelayObj(i, JSONUtil.toJson(kefuMsgSubscribe)));
		}
		final int size = subscribeList.size();
		new ThreadRequest(new ThreadRequestContent() {
			public void doSomeThing() {
				for(int i=0; i<size; i++) {
					try {
						DelayObj delayObj = delayQueue.take();
						Phrase phrase = phraseService.findRandom(null);
						KefuMsgSubscribe kefuMsgSubscribe = JSONUtil.fromJson(delayObj.getData(), KefuMsgSubscribe.class);
						KefuMsgType kefuMsgType = kefuService.findMsgType(kefuMsgSubscribe.getClientId(), kefuMsgSubscribe.getTypeCode());
						kefuService.sendWxTemplate(kefuMsgType, kefuMsgSubscribe, phrase.getContent());
					} catch (Exception e) {
						log.error("WxSchedule.sendPhrase", e);
					}
				}
			}
		}).start();
	}
}
