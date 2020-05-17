package cn.jia.kefu.service.impl;

import cn.jia.core.util.DateUtil;
import cn.jia.kefu.dao.KefuFAQMapper;
import cn.jia.kefu.dao.KefuMessageMapper;
import cn.jia.kefu.entity.KefuFAQ;
import cn.jia.kefu.entity.KefuMessage;
import cn.jia.kefu.service.KefuService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class KefuServiceImpl implements KefuService {
	
	@Autowired
	private KefuFAQMapper kefuFAQMapper;
	@Autowired
	private KefuMessageMapper kefuMessageMapper;

	@Override
	public Page<KefuFAQ> listFAQ(KefuFAQ example, int pageNo, int pageSize) {
		PageHelper.startPage(pageNo, pageSize);
		return kefuFAQMapper.selectByExample(example);
	}
	@Override
	public KefuFAQ createFAQ(KefuFAQ record) {
		long now = DateUtil.genTime(new Date());
		record.setCreateTime(now);
		record.setUpdateTime(now);
		kefuFAQMapper.insertSelective(record);
		return record;
	}
	@Override
	public KefuFAQ findFAQ(Integer id) {
		return kefuFAQMapper.selectByPrimaryKey(id);
	}
	@Override
	public KefuFAQ updateFAQ(KefuFAQ record) {
		long now = DateUtil.genTime(new Date());
		record.setUpdateTime(now);
		kefuFAQMapper.updateByPrimaryKeySelective(record);
		return record;
	}
	@Override
	public void deleteFAQ(Integer id) {
		kefuFAQMapper.deleteByPrimaryKey(id);
	}
	
	@Override
	public Page<KefuMessage> listMessage(KefuMessage example, int pageNo, int pageSize) {
		PageHelper.startPage(pageNo, pageSize);
		return kefuMessageMapper.selectByExample(example);
	}
	@Override
	public KefuMessage createMessage(KefuMessage record) {
		long now = DateUtil.genTime(new Date());
		record.setCreateTime(now);
		record.setUpdateTime(now);
		kefuMessageMapper.insertSelective(record);
		return record;
	}
	@Override
	public KefuMessage findMessage(Integer id) {
		return kefuMessageMapper.selectByPrimaryKey(id);
	}
	@Override
	public KefuMessage updateMessage(KefuMessage record) {
		long now = DateUtil.genTime(new Date());
		record.setUpdateTime(now);
		kefuMessageMapper.updateByPrimaryKeySelective(record);
		return record;
	}
	@Override
	public void deleteMessage(Integer id) {
		kefuMessageMapper.deleteByPrimaryKey(id);
	}
}
