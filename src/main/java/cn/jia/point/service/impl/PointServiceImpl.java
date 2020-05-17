package cn.jia.point.service.impl;

import cn.jia.core.exception.EsRuntimeException;
import cn.jia.core.util.DataUtil;
import cn.jia.core.util.DateUtil;
import cn.jia.point.common.Constants;
import cn.jia.point.common.ErrorConstants;
import cn.jia.point.dao.RecordMapper;
import cn.jia.point.dao.ReferralMapper;
import cn.jia.point.dao.SignMapper;
import cn.jia.point.entity.Record;
import cn.jia.point.entity.Referral;
import cn.jia.point.entity.Sign;
import cn.jia.point.service.PointService;
import cn.jia.user.entity.User;
import cn.jia.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class PointServiceImpl implements PointService {
	
	@Autowired
	private SignMapper signMapper;
	@Autowired
	private ReferralMapper referralMapper;
	@Autowired
	private RecordMapper recordMapper;
	@Autowired
	private UserService userService;

	/**
	 * 用户签到
	 */
	@Override
	public Record sign(Sign sign) throws Exception {
		// 检查用户是否存在
		User userResult = userService.findByJiacn(sign.getJiacn());
		if (userResult == null) {
			throw new EsRuntimeException(ErrorConstants.USER_NOT_EXIST);
		}
		int userPoint = userResult.getPoint(); //用户当前积分
		Long now = DateUtil.genTime(new Date());
		// 查询最后一次签到时间
		Sign lastSign = signMapper.selectLast(sign.getJiacn());
		Long todayStart = DateUtil.genTime(DateUtil.todayStart());
		// 判断是否可以签到
		if (lastSign != null && lastSign.getTime() > todayStart) {
			throw new EsRuntimeException(ErrorConstants.SIGN_NO_THE_TIME);
		}
		// 增加用户积分
		userService.changePoint(sign.getJiacn(), Constants.POINT_SCORE_SIGN);
		//更新用户最新位置
		User params = new User();
		params.setId(userResult.getId());
		params.setLatitude(sign.getLatitude());
		params.setLongitude(sign.getLongitude());
		userService.update(params);
		//记录积分情况
		Record record = new Record();
		record.setJiacn(sign.getJiacn());
		userPoint = userPoint + Constants.POINT_SCORE_SIGN;
		record.setType(Constants.POINT_TYPE_SIGN);
		record.setTime(now);
		record.setChange(Constants.POINT_SCORE_SIGN);
		record.setRemain(userPoint);
		recordMapper.insertSelective(record);
		// 添加签到记录
		sign.setPoint(Constants.POINT_SCORE_SIGN);
		sign.setTime(now);
		signMapper.insertSelective(sign);
		return record;
	}

	/**
	 * 用户推荐
	 */
	@Override
	public Record referral(Referral referral) throws Exception {
		// 检查用户是否存在
		User userResult = userService.findByJiacn(referral.getReferrer());
		if (userResult == null) {
			throw new EsRuntimeException(ErrorConstants.USER_NOT_EXIST);
		}
		User referralResult = userService.findByJiacn(referral.getReferral());
		if (referralResult == null) {
			throw new EsRuntimeException(ErrorConstants.USER_NOT_EXIST);
		}
		
		int userPoint = userResult.getPoint(); //用户当前积分
		//查找是否已经被推荐过
		Referral ral = referralMapper.selectByReferral(referral.getReferral());
		if(ral != null) {
			throw new EsRuntimeException(ErrorConstants.REFERRAL_EXISTS);
		}
		// 增加用户积分
		userService.changePoint(referral.getReferrer(), Constants.POINT_SCORE_REFERRAL);
		//更新用户推荐人信息
		User params = new User();
		params.setId(referralResult.getId());
		params.setReferrer(referral.getReferrer());
		userService.update(params);

		Long now = DateUtil.genTime(new Date());
		// 增加推荐记录
		referral.setTime(now);
		referralMapper.insertSelective(referral);
		//记录积分情况
		Record record = new Record();
		record.setJiacn(referral.getReferrer());
		userPoint = userPoint + Constants.POINT_SCORE_REFERRAL;
		record.setType(Constants.POINT_TYPE_REFERRAL);
		record.setTime(now);
		record.setChange(Constants.POINT_SCORE_REFERRAL);
		record.setRemain(userPoint);
		recordMapper.insertSelective(record);
		return record;
	}

	/**
	 * 积分概率变化
	 */
	@Override
	public Record luck(Record record) throws Exception {
		// 检查用户是否存在
		User userResult = userService.findByJiacn(record.getJiacn());
		if (userResult == null) {
			throw new EsRuntimeException(ErrorConstants.USER_NOT_EXIST);
		}
		int userPoint = userResult.getPoint(); //用户当前积分
		int recordPoint = record.getChange(); //所使用积分
		Long now = DateUtil.genTime(new Date());
		// 扣除用户积分
		int pointChange = recordPoint - recordPoint * 2;
		userService.changePoint(record.getJiacn(), pointChange);
		//记录积分情况
		userPoint = userPoint + pointChange;
		record.setType(Constants.POINT_TYPE_LUCK);
		record.setTime(now);
		record.setChange(pointChange);
		record.setRemain(userPoint);
		recordMapper.insertSelective(record);
		//积分变化规则
		if (DataUtil.getRandom(true, 4).equals("0519")) {
			record.setChange(recordPoint * 100);
		} else if (DataUtil.getRandom(true, 2).equals("11")) {
			record.setChange(recordPoint * 10);
		} else if (Integer.parseInt(DataUtil.getRandom(true, 1)) % 4 == 0) {
			record.setChange(recordPoint * 2);
		} else {
			record.setChange(0);
		}
		// 增加用户积分
		userService.changePoint(record.getJiacn(), record.getChange());
		//记录积分情况
		userPoint = userPoint + record.getChange();
		record.setType(Constants.POINT_TYPE_LUCK);
		record.setTime(now);
		record.setChange(record.getChange());
		record.setRemain(userPoint);
		recordMapper.insertSelective(record);
		return record;
	}

	@Override
	public Record add(String jiacn, int point, int type) throws Exception {
		// 检查用户是否存在
		User userResult = userService.findByJiacn(jiacn);
		if (userResult == null) {
			throw new EsRuntimeException(ErrorConstants.USER_NOT_EXIST);
		}
		int userPoint = userResult.getPoint(); //用户当前积分
		Long now = DateUtil.genTime(new Date());
		// 增加用户积分
		userService.changePoint(jiacn, point);
		//记录积分情况
		userPoint = userPoint + point;
		Record record = new Record();
		record.setJiacn(jiacn);
		record.setType(type);
		record.setTime(now);
		record.setChange(point);
		record.setRemain(userPoint);
		recordMapper.insertSelective(record);
		return record;
	}

	/**
	 * 新用户初始化积分
	 */
	@Override
	public Record init(Record record) throws Exception {
		// 检查用户是否存在
		User userResult = userService.findByJiacn(record.getJiacn());
		if (userResult == null) {
			throw new EsRuntimeException(ErrorConstants.USER_NOT_EXIST);
		}
		int userPoint = userResult.getPoint(); // 用户当前积分
		Long now = DateUtil.genTime(new Date());
		
		// 增加用户积分
		userService.changePoint(record.getJiacn(), Constants.POINT_SCORE_INIT);
		// 记录积分情况
		userPoint = userPoint + Constants.POINT_SCORE_INIT;
		record.setType(Constants.POINT_TYPE_LUCK);
		record.setTime(now);
		record.setChange(Constants.POINT_SCORE_INIT);
		record.setRemain(userPoint);
		recordMapper.insertSelective(record);
		return record;
	}
}
