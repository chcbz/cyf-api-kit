package cn.jia.kefu.service;

import cn.jia.kefu.entity.KefuFaq;
import cn.jia.kefu.entity.KefuMessage;
import cn.jia.kefu.entity.KefuMsgType;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
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
}
