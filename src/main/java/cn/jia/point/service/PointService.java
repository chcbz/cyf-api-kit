package cn.jia.point.service;

import cn.jia.core.exception.EsRuntimeException;
import cn.jia.core.util.DataUtil;
import cn.jia.core.util.DateUtil;
import cn.jia.point.common.Constants;
import cn.jia.point.common.ErrorConstants;
import cn.jia.point.entity.PointRecord;
import cn.jia.point.entity.PointReferral;
import cn.jia.point.entity.PointSign;
import cn.jia.user.entity.User;
import cn.jia.user.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class PointService {
	
	@Autowired
	private IPointSignService pointSignService;
	@Autowired
	private IPointReferralService pointReferralService;
	@Autowired
	private IPointRecordService pointRecordService;
	@Autowired
	private UserService userService;

	/**
	 * 用户签到
	 */
	public PointRecord sign(PointSign sign) throws Exception {
		// 检查用户是否存在
		User userResult = userService.findByJiacn(sign.getJiacn());
		if (userResult == null) {
			throw new EsRuntimeException(ErrorConstants.USER_NOT_EXIST);
		}
		int userPoint = userResult.getPoint(); //用户当前积分
		Long now = DateUtil.genTime(new Date());
		// 查询最后一次签到时间
		LambdaQueryWrapper<PointSign> queryWrapper = Wrappers.lambdaQuery(new PointSign());
		PointSign lastSign = pointSignService.getOne(queryWrapper.eq(PointSign::getJiacn, sign.getJiacn())
				.orderByDesc(PointSign::getTime));
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
		PointRecord record = new PointRecord();
		record.setJiacn(sign.getJiacn());
		userPoint = userPoint + Constants.POINT_SCORE_SIGN;
		record.setType(Constants.POINT_TYPE_SIGN);
		record.setTime(now);
		record.setChg(Constants.POINT_SCORE_SIGN);
		record.setRemain(userPoint);
		pointRecordService.save(record);
		// 添加签到记录
		sign.setPoint(Constants.POINT_SCORE_SIGN);
		sign.setTime(now);
		pointSignService.save(sign);
		return record;
	}

	/**
	 * 用户推荐
	 */
	public PointRecord referral(PointReferral referral) throws Exception {
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
		LambdaQueryWrapper<PointReferral> queryWrapper = Wrappers.lambdaQuery(new PointReferral());
		PointReferral ral = pointReferralService.getOne(queryWrapper.eq(PointReferral::getReferral, referral.getReferral()));
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
		pointReferralService.save(referral);
		//记录积分情况
		PointRecord record = new PointRecord();
		record.setJiacn(referral.getReferrer());
		userPoint = userPoint + Constants.POINT_SCORE_REFERRAL;
		record.setType(Constants.POINT_TYPE_REFERRAL);
		record.setTime(now);
		record.setChg(Constants.POINT_SCORE_REFERRAL);
		record.setRemain(userPoint);
		pointRecordService.save(record);
		return record;
	}

	/**
	 * 积分概率变化
	 */
	public PointRecord luck(PointRecord record) throws Exception {
		// 检查用户是否存在
		User userResult = userService.findByJiacn(record.getJiacn());
		if (userResult == null) {
			throw new EsRuntimeException(ErrorConstants.USER_NOT_EXIST);
		}
		int userPoint = userResult.getPoint(); //用户当前积分
		int recordPoint = record.getChg(); //所使用积分
		Long now = DateUtil.genTime(new Date());
		// 扣除用户积分
		int pointChange = recordPoint - recordPoint * 2;
		userService.changePoint(record.getJiacn(), pointChange);
		//记录积分情况
		userPoint = userPoint + pointChange;
		record.setType(Constants.POINT_TYPE_LUCK);
		record.setTime(now);
		record.setChg(pointChange);
		record.setRemain(userPoint);
		pointRecordService.save(record);
		//积分变化规则
		if (DataUtil.getRandom(true, 4).equals("0519")) {
			record.setChg(recordPoint * 100);
		} else if (DataUtil.getRandom(true, 2).equals("11")) {
			record.setChg(recordPoint * 10);
		} else if (Integer.parseInt(DataUtil.getRandom(true, 1)) % 4 == 0) {
			record.setChg(recordPoint * 2);
		} else {
			record.setChg(0);
		}
		// 增加用户积分
		userService.changePoint(record.getJiacn(), record.getChg());
		//记录积分情况
		userPoint = userPoint + record.getChg();
		record.setType(Constants.POINT_TYPE_LUCK);
		record.setTime(now);
		record.setChg(record.getChg());
		record.setRemain(userPoint);
		pointRecordService.save(record);
		return record;
	}

	public PointRecord add(String jiacn, int point, int type) throws Exception {
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
		PointRecord record = new PointRecord();
		record.setJiacn(jiacn);
		record.setType(type);
		record.setTime(now);
		record.setChg(point);
		record.setRemain(userPoint);
		pointRecordService.save(record);
		return record;
	}

	/**
	 * 新用户初始化积分
	 */
	public PointRecord init(PointRecord record) throws Exception {
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
		record.setChg(Constants.POINT_SCORE_INIT);
		record.setRemain(userPoint);
		pointRecordService.save(record);
		return record;
	}
}
