package cn.jia.wx.service;

import cn.jia.core.configuration.SpringContextHolder;
import cn.jia.wx.common.Constants;
import cn.jia.wx.entity.PayOrder;

/**
 * @author chc
 */
public abstract class AbstractPayOrderParse {

    protected String productId;
    protected String outTradeNo;
    protected String userId;

    /**
     * 扫码支付结果
     *
     * @return 支付结果
     */
    public abstract PayOrder scanPayNotifyResult();

    /**
     * 支付结果处理
     */
    public abstract void orderNotifyResult();

    public static AbstractPayOrderParse instance(String productId, String userId) throws Exception{
        String prefix = productId.substring(0, 3);
        // 取得Class对象
        Class<?> cls = Class.forName(Constants.WX_PAY_ORDER_PARSE.get(prefix));
        Object obj = SpringContextHolder.getBean(cls);
        AbstractPayOrderParse payOrderParse = (AbstractPayOrderParse)obj;
        payOrderParse.productId = productId;
        payOrderParse.userId = userId;
        return payOrderParse;
    }

    public static AbstractPayOrderParse instance(String outTradeNo) throws Exception{
        String prefix = outTradeNo.substring(0, 3);
        // 取得Class对象
        Class<?> cls = Class.forName(Constants.WX_PAY_ORDER_PARSE.get(prefix));
        Object obj = SpringContextHolder.getBean(cls);
        AbstractPayOrderParse payOrderParse = (AbstractPayOrderParse)obj;
        payOrderParse.outTradeNo = outTradeNo;
        return payOrderParse;
    }
}