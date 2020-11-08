package cn.jia.point.service.impl;

import cn.jia.core.exception.EsRuntimeException;
import cn.jia.core.util.DateUtil;
import cn.jia.point.common.Constants;
import cn.jia.point.common.ErrorConstants;
import cn.jia.point.dao.GiftMapper;
import cn.jia.point.dao.GiftUsageMapper;
import cn.jia.point.dao.RecordMapper;
import cn.jia.point.entity.Gift;
import cn.jia.point.entity.GiftExample;
import cn.jia.point.entity.GiftUsage;
import cn.jia.point.entity.Record;
import cn.jia.point.service.GiftService;
import cn.jia.user.entity.User;
import cn.jia.user.service.UserService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class GiftServiceImpl implements GiftService {
	
	@Autowired
	private GiftMapper giftMapper;
	@Autowired
	private GiftUsageMapper giftUsageMapper;
	@Autowired
	private UserService userService;
	@Autowired
	private RecordMapper recordMapper;

	@Override
	public Gift create(Gift gift) {
		Long now = DateUtil.genTime(new Date());
		gift.setCreateTime(now);
		gift.setUpdateTime(now);
		giftMapper.insert(gift);
		return gift;
	}

	@Override
	public Gift find(Integer id) throws Exception {
		Gift gift = giftMapper.selectByPrimaryKey(id);
		if(gift == null) {
			throw new EsRuntimeException(ErrorConstants.GIFT_NOT_EXISTS);
		}
		return gift;
	}

	@Override
	public Gift update(Gift gift) {
		Long now = DateUtil.genTime(new Date());
		gift.setUpdateTime(now);
		giftMapper.updateByPrimaryKeySelective(gift);
		return gift;
	}

	@Override
	public void delete(Integer id) {
		giftMapper.deleteByPrimaryKey(id);
	}

	@Override
	public Page<Gift> list(int pageNo, int pageSize, GiftExample example) {
		PageHelper.startPage(pageNo, pageSize);
		return giftMapper.selectByExample(example);
	}

	/**
	 * 礼品兑换
	 * @throws Exception 异常信息
	 */
	@Override
	@Transactional
	public void usage(GiftUsage record) throws Exception {
		Gift gift = giftMapper.selectByPrimaryKey(record.getGiftId());
		if (gift == null) {
			throw new EsRuntimeException(ErrorConstants.GIFT_NOT_EXISTS);
		}
		if (gift.getQuantity() < record.getQuantity()) {
			throw new EsRuntimeException(ErrorConstants.GIFT_NO_ENOUGH);
		}
		
		User user = userService.findByJiacn(record.getJiacn());
		if (user == null) {
			throw new EsRuntimeException(ErrorConstants.USER_NOT_EXIST);
		}
		Long now = DateUtil.genTime(new Date());
		// 更新礼品数量
		gift.setQuantity(gift.getQuantity() - record.getQuantity());
		gift.setUpdateTime(now);
		giftMapper.updateByPrimaryKeySelective(gift);
		//如果非现金交易，默认扣除积分
		if(record.getPrice() == null || record.getPrice().equals(0)) {
			// 更新用户积分
			int totalPoint = record.getQuantity() * gift.getPoint();
			int point = totalPoint - totalPoint * 2;
			userService.changePoint(record.getJiacn(), point);
			//记录积分情况
			Record pointRecord = new Record();
			pointRecord.setJiacn(user.getJiacn());
			pointRecord.setType(Constants.POINT_TYPE_REDEEM);
			pointRecord.setTime(now);
			pointRecord.setChange(point);
			pointRecord.setRemain(user.getPoint() + point);
			recordMapper.insertSelective(pointRecord);
			// 更新兑换记录
			record.setPoint(totalPoint);
		}
		record.setTime(now);
		record.setName(gift.getName());
		record.setDescription(gift.getDescription());
		record.setPicUrl(gift.getPicUrl());
		giftUsageMapper.insertSelective(record);
	}

	@Override
	@Transactional
	public void usageCancel(Integer giftUsageId) throws Exception {
		GiftUsage giftUsage = giftUsageMapper.selectByPrimaryKey(giftUsageId);
		if (giftUsage == null) {
			throw new EsRuntimeException(ErrorConstants.DATA_NOT_FOUND);
		}
		if (!Constants.GIFT_USAGE_STATUS_PAYED.equals(giftUsage.getStatus())) {
			throw new EsRuntimeException(ErrorConstants.GIFT_CANNOT_CANCEL);
		}
		// 退回积分
		if (giftUsage.getPoint() != null && giftUsage.getPoint() != 0) {
			User user = userService.findByJiacn(giftUsage.getJiacn());
			if (user == null) {
				throw new EsRuntimeException(ErrorConstants.USER_NOT_EXIST);
			}
			User upUser = new User();
			upUser.setId(user.getId());
			upUser.setPoint(user.getPoint() + giftUsage.getPoint());
			userService.update(upUser);
		}
		// 修改状态
		GiftUsage upUsage = new GiftUsage();
		upUsage.setId(giftUsage.getId());
		upUsage.setStatus(Constants.GIFT_USAGE_STATUS_CANCEL);
		giftUsageMapper.updateByPrimaryKeySelective(upUsage);
	}

	@Override
	public void usageDelete(Integer giftUsageId) throws Exception {
		GiftUsage giftUsage = giftUsageMapper.selectByPrimaryKey(giftUsageId);
		if (giftUsage == null) {
			throw new EsRuntimeException(ErrorConstants.DATA_NOT_FOUND);
		}
		if (!Constants.GIFT_USAGE_STATUS_DRAFT.equals(giftUsage.getStatus()) &&
				!Constants.GIFT_USAGE_STATUS_CANCEL.equals(giftUsage.getStatus())) {
			throw new EsRuntimeException(ErrorConstants.GIFT_CANNOT_CANCEL);
		}
		giftUsageMapper.deleteByPrimaryKey(giftUsageId);
	}

	/**
	 * 获取礼品的兑换情况
	 */
	@Override
	public Page<GiftUsage> usageListByGift(int pageNum, int pageSize, Integer giftId) {
		PageHelper.startPage(pageNum, pageSize);
		return giftUsageMapper.selectByGiftId(giftId);
	}

	/**
	 * 获取用户的礼品兑换情况
	 */
	@Override
	public Page<GiftUsage> usageListByUser(int pageNum, int pageSize, String jiacn) {
		PageHelper.startPage(pageNum, pageSize);
		return giftUsageMapper.selectByUser(jiacn);
	}
}
