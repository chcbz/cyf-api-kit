package cn.jia.point.common;

import cn.jia.core.exception.EsErrorConstants;

/**
 * 错误常量
 * @author chc
 * @date 2017年12月8日 下午2:47:56
 */
public class ErrorConstants extends EsErrorConstants {
	
	/** 礼品不存在 */
	public static final String GIFT_NOT_EXISTS = "EPOINT001";
	/** 礼品数量不足 */
	public static final String GIFT_NO_ENOUGH = "EPOINT002";
	/** 还没到签到时间 */
	public static final String SIGN_NO_THE_TIME = "EPOINT003";
	/** 用户已经被推荐 */
	public static final String REFERRAL_EXISTS = "EPOINT004";
	
	/** 用户不存在 */
	public static final String USER_NOT_EXIST = "EUSER002";
}
