package cn.jia.wx.service;

import cn.jia.core.configuration.SpringContextHolder;
import cn.jia.wx.common.Constants;
import cn.jia.wx.entity.PayOrder;

public abstract class PayOrderParse {

    protected String productId;
    protected String outTradeNo;
    protected String userId;

    public abstract PayOrder scanPayNotifyResult();

    public abstract void orderNotifyResult();

    public static PayOrderParse instance(String productId, String userId) throws Exception{
        String prefix = productId.substring(0, 3);
        Class<?> cls = Class.forName(Constants.WX_PAY_ORDER_PARSE.get(prefix)); // 取得Class对象
        Object obj = SpringContextHolder.getBean(cls);
        PayOrderParse payOrderParse = (PayOrderParse)obj;
        payOrderParse.productId = productId;
        payOrderParse.userId = userId;
        return payOrderParse;
    }

    public static PayOrderParse instance(String outTradeNo) throws Exception{
        String prefix = outTradeNo.substring(0, 3);
        Class<?> cls = Class.forName(Constants.WX_PAY_ORDER_PARSE.get(prefix)); // 取得Class对象
        Object obj = SpringContextHolder.getBean(cls);
        PayOrderParse payOrderParse = (PayOrderParse)obj;
        payOrderParse.outTradeNo = outTradeNo;
        return payOrderParse;
    }
}