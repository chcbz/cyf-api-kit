package cn.jia.point.service;

import cn.jia.common.entity.BaseEntity;
import cn.jia.core.exception.EsRuntimeException;
import cn.jia.core.util.DateUtil;
import cn.jia.core.util.StringUtils;
import cn.jia.point.common.Constants;
import cn.jia.point.common.ErrorConstants;
import cn.jia.point.entity.GiftExample;
import cn.jia.point.entity.PointGift;
import cn.jia.point.entity.PointGiftUsage;
import cn.jia.point.entity.PointRecord;
import cn.jia.user.entity.User;
import cn.jia.user.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class GiftService {
	
	@Autowired
	private IPointGiftService pointGiftService;
	@Autowired
	private IPointGiftUsageService pointGiftUsageService;
	@Autowired
	private UserService userService;
	@Autowired
	private IPointRecordService pointRecordService;

	public PointGift create(PointGift gift) {
		Long now = DateUtil.genTime(new Date());
		gift.setCreateTime(now);
		gift.setUpdateTime(now);
		pointGiftService.save(gift);
		return gift;
	}

	public PointGift find(Integer id) throws Exception {
		PointGift gift = pointGiftService.getById(id);
		if(gift == null) {
			throw new EsRuntimeException(ErrorConstants.GIFT_NOT_EXISTS);
		}
		return gift;
	}

	public PointGift update(PointGift gift) {
		Long now = DateUtil.genTime(new Date());
		gift.setUpdateTime(now);
		pointGiftService.updateById(gift);
		return gift;
	}

	public void delete(Integer id) {
		pointGiftService.removeById(id);
	}

	public PageInfo<PointGift> list(int pageNo, int pageSize, GiftExample example) {
		example = example == null ? new GiftExample() : example;
		PageHelper.startPage(pageNo, pageSize);
		LambdaQueryWrapper<PointGift> queryWrapper = Wrappers.lambdaQuery(new PointGift());
		if (example.getCreateTimeStart() != null) {
			queryWrapper.gt(BaseEntity::getCreateTime, example.getCreateTimeStart());
		}
		if (example.getCreateTimeEnd() != null) {
			queryWrapper.lt(BaseEntity::getCreateTime, example.getCreateTimeEnd());
		}
		if (example.getClientStrictFlag() != null && example.getClientStrictFlag().equals(1)) {
			if (StringUtils.isNotEmpty(example.getClientId())) {
				queryWrapper.eq(PointGift::getClientId, example.getClientId());
			} else {
				queryWrapper.isNull(PointGift::getClientId);
			}
		}
		if (StringUtils.isNotEmpty(example.getName())) {
			queryWrapper.like(PointGift::getName, example.getName());
		}
		if (StringUtils.isNotEmpty(example.getDescription())) {
			queryWrapper.like(PointGift::getDescription, example.getDescription());
		}
		List<PointGift> list = pointGiftService.list(queryWrapper);
		return PageInfo.of(list);
	}

	/**
	 * 礼品兑换
	 * @throws Exception 异常信息
	 */
	@Transactional(rollbackFor = Exception.class)
	public void usage(PointGiftUsage record) throws Exception {
		PointGift gift = pointGiftService.getById(record.getGiftId());
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
		pointGiftService.updateById(gift);
		//如果非现金交易，默认扣除积分
		if(record.getPrice() == null || record.getPrice().equals(0)) {
			// 更新用户积分
			int totalPoint = record.getQuantity() * gift.getPoint();
			int point = totalPoint - totalPoint * 2;
			userService.changePoint(record.getJiacn(), point);
			//记录积分情况
			PointRecord pointRecord = new PointRecord();
			pointRecord.setJiacn(user.getJiacn());
			pointRecord.setType(Constants.POINT_TYPE_REDEEM);
			pointRecord.setTime(now);
			pointRecord.setChg(point);
			pointRecord.setRemain(user.getPoint() + point);
			pointRecordService.save(pointRecord);
			// 更新兑换记录
			record.setPoint(totalPoint);
		}
		record.setTime(now);
		record.setName(gift.getName());
		record.setDescription(gift.getDescription());
		record.setPicUrl(gift.getPicUrl());
		pointGiftUsageService.save(record);
	}

	@Transactional(rollbackFor = Exception.class)
	public void usageCancel(Integer giftUsageId) throws Exception {
		PointGiftUsage giftUsage = pointGiftUsageService.getById(giftUsageId);
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
		PointGiftUsage upUsage = new PointGiftUsage();
		upUsage.setId(giftUsage.getId());
		upUsage.setStatus(Constants.GIFT_USAGE_STATUS_CANCEL);
		pointGiftUsageService.updateById(upUsage);
	}

	public void usageDelete(Integer giftUsageId) throws Exception {
		PointGiftUsage giftUsage = pointGiftUsageService.getById(giftUsageId);
		if (giftUsage == null) {
			throw new EsRuntimeException(ErrorConstants.DATA_NOT_FOUND);
		}
		if (!Constants.GIFT_USAGE_STATUS_DRAFT.equals(giftUsage.getStatus()) &&
				!Constants.GIFT_USAGE_STATUS_CANCEL.equals(giftUsage.getStatus())) {
			throw new EsRuntimeException(ErrorConstants.GIFT_CANNOT_CANCEL);
		}
		pointGiftUsageService.removeById(giftUsageId);
	}

	/**
	 * 获取礼品的兑换情况
	 */
	public PageInfo<PointGiftUsage> usageListByGift(int pageNum, int pageSize, Integer giftId) {
		PageHelper.startPage(pageNum, pageSize);
		LambdaQueryWrapper<PointGiftUsage> queryWrapper = Wrappers.lambdaQuery(new PointGiftUsage())
				.eq(PointGiftUsage::getGiftId, giftId);
		List<PointGiftUsage> list = pointGiftUsageService.list(queryWrapper);
		return PageInfo.of(list);
	}

	/**
	 * 获取用户的礼品兑换情况
	 */
	public PageInfo<PointGiftUsage> usageListByUser(int pageNum, int pageSize, String jiacn) {
		PageHelper.startPage(pageNum, pageSize);
		LambdaQueryWrapper<PointGiftUsage> queryWrapper = Wrappers.lambdaQuery(new PointGiftUsage())
				.eq(PointGiftUsage::getJiacn, jiacn);
		List<PointGiftUsage> list = pointGiftUsageService.list(queryWrapper);
		return PageInfo.of(list);
	}
}
