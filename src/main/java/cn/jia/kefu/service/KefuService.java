package cn.jia.kefu.service;

import cn.jia.core.common.EsConstants;
import cn.jia.core.util.DateUtil;
import cn.jia.core.util.JSONUtil;
import cn.jia.core.util.StringUtils;
import cn.jia.kefu.entity.*;
import cn.jia.task.common.TaskConstants;
import cn.jia.wx.entity.MpTemplate;
import cn.jia.wx.entity.MpUser;
import cn.jia.wx.service.MpInfoService;
import cn.jia.wx.service.MpTemplateService;
import cn.jia.wx.service.MpUserService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.bean.kefu.WxMpKefuMessage;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class KefuService {
	private static final String ACTIVE_MP_USER = "ACTIVE_MP_USER";
	
	@Autowired
	private IKefuFaqService kefuFaqService;
	@Autowired
	private IKefuMessageService kefuMessageService;
	@Autowired
	private IKefuMsgTypeService kefuMsgTypeService;
	@Autowired
	private IKefuMsgSubscribeService kefuMsgSubscribeService;
	@Autowired
	private IKefuMsgLogService kefuMsgLogService;
	@Autowired
	private MpInfoService mpInfoService;
	@Autowired
	private MpTemplateService mpTemplateService;
	@Autowired
	private MpUserService mpUserService;
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	public PageInfo<KefuFaq> listFaq(KefuFaq example, int pageNo, int pageSize) {
		PageHelper.startPage(pageNo, pageSize);
		List<KefuFaq> kefuFaqList = kefuFaqService.listByEntity(example);
		return PageInfo.of(kefuFaqList);
	}
	public KefuFaq createFaq(KefuFaq record) {
		kefuFaqService.save(record);
		return record;
	}
	public KefuFaq findFaq(Integer id) {
		return kefuFaqService.getById(id);
	}

	public KefuFaq updateFaq(KefuFaq record) {
		kefuFaqService.updateById(record);
		return record;
	}
	public void deleteFaq(Integer id) {
		kefuFaqService.removeById(id);
	}
	
	public PageInfo<KefuMessage> listMessage(KefuMessage example, int pageNo, int pageSize) {
		PageHelper.startPage(pageNo, pageSize);
		List<KefuMessage> kefuMessageList = kefuMessageService.listByEntity(example);
		return PageInfo.of(kefuMessageList);
	}
	public KefuMessage createMessage(KefuMessage record) {
		kefuMessageService.save(record);
		return record;
	}
	public KefuMessage findMessage(Integer id) {
		return kefuMessageService.getById(id);
	}
	
	public KefuMessage updateMessage(KefuMessage record) {
		kefuMessageService.updateById(record);
		return record;
	}
	public void deleteMessage(Integer id) {
		kefuMessageService.removeById(id);
	}

	public KefuMsgType findMsgType(String clientId, String typeCode) {
		Wrapper<KefuMsgType> wrapper = Wrappers.lambdaQuery(new KefuMsgType())
				.eq(KefuMsgType::getClientId, clientId)
				.eq(KefuMsgType::getTypeCode, typeCode);
		return kefuMsgTypeService.getOne(wrapper);
	}

	public List<KefuMsgType> listMsgType() {
		return kefuMsgTypeService.list();
	}

	public List<KefuMsgSubscribe> listMsgSubscribe() {
		return kefuMsgSubscribeService.list();
	}

	public List<KefuMsgSubscribe> listMsgSubscribe(KefuMsgSubscribe example) {
		return kefuMsgSubscribeService.listByEntity(example);
	}

	public KefuMsgSubscribe findMsgSubscribe(String clientId, String typeCode, String jiacn) {
		Wrapper<KefuMsgSubscribe> wrapper = Wrappers.lambdaQuery(new KefuMsgSubscribe())
				.eq(KefuMsgSubscribe::getClientId, clientId)
				.eq(KefuMsgSubscribe::getTypeCode, typeCode)
				.eq(KefuMsgSubscribe::getJiacn, jiacn);
		return kefuMsgSubscribeService.getOne(wrapper);
	}

	/**
	 * 发送客服消息
	 *
	 * @param msgType 消息类型
	 * @param clientId 应用标识符
	 * @param attr 属性
	 * @return 发送结果
	 * @throws Exception 异常
	 */
	public Boolean sendMessage(KefuMsgTypeCode msgType, String clientId, String... attr) throws Exception {
		LambdaQueryWrapper<KefuMsgType> queryWrapper = Wrappers.lambdaQuery(new KefuMsgType())
				.eq(KefuMsgType::getTypeCode, msgType.getCode()).eq(KefuMsgType::getClientId, clientId);
		KefuMsgType kefuMsgType = kefuMsgTypeService.getOne(queryWrapper);
		if (kefuMsgType == null) {
			return false;
		}

		// 通知订阅者
		KefuMsgSubscribe example = new KefuMsgSubscribe();
		example.setClientId(clientId);
		example.setTypeCode(msgType.getCode());
		List<KefuMsgSubscribe> kefuMsgSubscribeList = listMsgSubscribe(example);
		for (KefuMsgSubscribe item : kefuMsgSubscribeList) {
			sendTemplate(kefuMsgType, item, attr);
		}
		return true;
	}

	public void sendTemplate(KefuMsgType kefuMsgType, KefuMsgSubscribe item, String... attr) throws Exception {
		if (EsConstants.COMMON_YES.equals(item.getWxRxFlag())) {
			sendWxTemplate(kefuMsgType, item, attr);
		}
	}

	public boolean sendWxTemplate(KefuMsgType kefuMsgType, KefuMsgSubscribe item, String... attr) throws Exception {
		MpUser mpUser = mpUserService.findByJiacn(item.getJiacn(), kefuMsgType.getClientId());
		if (mpUser == null) {
			return false;
		}
		String msgContent = "";
		boolean sendSuccess = false;
		Object activeMpUser = redisTemplate.opsForValue().get("active_mp_user_" + mpUser.getOpenId());
		if (StringUtils.isNotEmpty(kefuMsgType.getWxTemplateTxt()) && activeMpUser != null) {
			WxMpKefuMessage kfmessage = new WxMpKefuMessage();
			kfmessage.setToUser(mpUser.getOpenId());
			kfmessage.setMsgType(WxConsts.KefuMsgType.TEXT);
			msgContent = kefuMsgType.getWxTemplateTxt();
			for (int i = 0; i < attr.length; i++) {
				msgContent = msgContent.replace("#" + i + "#", attr[i]);
			}
			kfmessage.setContent(msgContent.replaceAll("\\\\n", "\n"));
			try {
				sendSuccess = mpInfoService.findWxMpService(mpUser.getAppid()).getKefuService().sendKefuMessage(kfmessage);
			} catch (WxErrorException e) {
				log.error("sendKefuMessage error: ", e);
			}
		}
		// 如果客服消息发送失败，尝试用模板消息
		if (!sendSuccess && StringUtils.isNotEmpty(kefuMsgType.getWxTemplateId())) {
			MpTemplate mpTemplate = mpTemplateService.find(kefuMsgType.getWxTemplateId());
			if (mpTemplate != null) {
				msgContent = kefuMsgType.getWxTemplate();
				for (int i = 0; i< attr.length; i++) {
					msgContent = msgContent.replace("#" + i + "#", attr[i]);
				}
				List<WxMpTemplateData> data = JSONUtil.jsonToList(msgContent, WxMpTemplateData.class);
				WxMpTemplateMessage message = new WxMpTemplateMessage();
				message.setToUser(mpUser.getOpenId());
				message.setTemplateId(mpTemplate.getTemplateId());
				message.setData(data);
				message.setUrl(kefuMsgType.getUrl());
				String messageId = mpInfoService.findWxMpService(mpTemplate.getAppid()).getTemplateMsgService().sendTemplateMsg(message);
				sendSuccess = StringUtils.isNotEmpty(messageId);
			}
		}
		if (sendSuccess) {
			KefuMsgLog msg = new KefuMsgLog();
			msg.setType(TaskConstants.MESSAGE_TYPE_WX);
			msg.setUpdateTime(DateUtil.genTime());
			msg.setCreateTime(DateUtil.genTime());
			msg.setJiacn(item.getJiacn());
			msg.setTitle(kefuMsgType.getWxTemplateId());
			msg.setContent(msgContent);
			msg.setUrl(kefuMsgType.getUrl());
			return kefuMsgLogService.save(msg);
		}
		return false;
	}

	public static void main(String[] args) {
		String str = "[{\"name\":\"first\",\"value\":\"有新的下单信息，请及时处理！\",\"color\":\"#173177\"},{\"name\":\"keyword1\",\"value\":\"礼品下单通知\",\"color\":\"#173177\"},{\"name\":\"keyword2\",\"value\":\"请及时处理\",\"color\":\"#173177\"},{\"name\":\"remark\",\"value\":\"\",\"color\":\"#173177\"}]";
		List<WxMpTemplateData> data = JSONUtil.jsonToList(str, WxMpTemplateData.class);
		System.out.println(data);
	}
}
