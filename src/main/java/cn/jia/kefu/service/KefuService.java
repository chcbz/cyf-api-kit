package cn.jia.kefu.service;

import cn.jia.core.common.EsConstants;
import cn.jia.core.util.JSONUtil;
import cn.jia.core.util.StringUtils;
import cn.jia.kefu.entity.*;
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
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KefuService {
	
	@Autowired
	private IKefuFaqService kefuFaqService;
	@Autowired
	private IKefuMessageService kefuMessageService;
	@Autowired
	private IKefuMsgTypeService kefuMsgTypeService;
	@Autowired
	private IKefuMsgSubscribeService kefuMsgSubscribeService;
	@Autowired
	private MpInfoService mpInfoService;
	@Autowired
	private MpTemplateService mpTemplateService;
	@Autowired
	private MpUserService mpUserService;

	public PageInfo<KefuFaq> listFAQ(KefuFaq example, int pageNo, int pageSize) {
		PageHelper.startPage(pageNo, pageSize);
		List<KefuFaq> kefuFaqList = kefuFaqService.listByEntity(example);
		return PageInfo.of(kefuFaqList);
	}
	public KefuFaq createFAQ(KefuFaq record) {
		kefuFaqService.save(record);
		return record;
	}
	public KefuFaq findFAQ(Integer id) {
		return kefuFaqService.getById(id);
	}
	public KefuFaq updateFAQ(KefuFaq record) {
		kefuFaqService.updateById(record);
		return record;
	}
	public void deleteFAQ(Integer id) {
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

	public Boolean sendMessage(KefuMsgTypeCode msgType, String clientId, String... attr) throws Exception {
		LambdaQueryWrapper<KefuMsgType> queryWrapper = Wrappers.lambdaQuery(new KefuMsgType())
				.eq(KefuMsgType::getTypeCode, msgType).eq(KefuMsgType::getClientId, clientId);
		KefuMsgType kefuMsgType = kefuMsgTypeService.getOne(queryWrapper);
		if (kefuMsgType == null) {
			return false;
		}

		// 通知管理员
		KefuMsgSubscribe example = new KefuMsgSubscribe();
		example.setClientId(clientId);
		example.setTypeCode(msgType.getCode());
		List<KefuMsgSubscribe> kefuMsgSubscribeList = listMsgSubscribe(example);
		for (KefuMsgSubscribe item : kefuMsgSubscribeList) {
			if (EsConstants.COMMON_YES.equals(item.getWxRxFlag())
					&& StringUtils.isNotEmpty(kefuMsgType.getWxTemplateId())) {
				String msgContent = kefuMsgType.getWxTemplate();
				for (int i = 0; i< attr.length; i++) {
					msgContent = msgContent.replace("#" + i + "#", attr[i]);
				}
				List<WxMpTemplateData> data = JSONUtil.jsonToList(msgContent, WxMpTemplateData.class);
				MpTemplate mpTemplate = mpTemplateService.find(kefuMsgType.getWxTemplateId());
				MpUser mpUser = mpUserService.findByJiacn(item.getJiacn());
				if (mpTemplate != null && mpUser != null) {
					WxMpTemplateMessage message = new WxMpTemplateMessage();
					message.setToUser(mpUser.getOpenId());
					message.setTemplateId(mpTemplate.getTemplateId());
					message.setData(data);
					message.setUrl(kefuMsgType.getUrl());
					mpInfoService.findWxMpService(mpTemplate.getAppid()).getTemplateMsgService().sendTemplateMsg(message);
				}
			}
		}
		return true;
	}
}
