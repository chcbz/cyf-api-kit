package cn.jia.kefu.common;

import cn.jia.core.exception.EsErrorConstants;

/**
 * 客服模块异常常量
 * @author chc
 * @since 2021/2/1
 */
public class KefuErrorConstants extends EsErrorConstants {

    /** 礼品不存在 */
    public static final String GIFT_NOT_EXISTS = "EPOINT001";
    /** 礼品数量不足 */
    public static final String GIFT_NO_ENOUGH = "EPOINT002";
    /** 还没到签到时间 */
    public static final String SIGN_NO_THE_TIME = "EPOINT003";
    /** 用户已经被推荐 */
    public static final String REFERRAL_EXISTS = "EPOINT004";
    /** 已支付状态才能取消 */
    public static final String GIFT_CANNOT_CANCEL = "EPOINT005";

    /** 用户不存在 */
    public static final String USER_NOT_EXIST = "EUSER002";
}
