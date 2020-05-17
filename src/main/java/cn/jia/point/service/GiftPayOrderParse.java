package cn.jia.point.service;

import cn.jia.core.util.DataUtil;
import cn.jia.core.util.DateUtil;
import cn.jia.core.util.StringUtils;
import cn.jia.point.dao.GiftMapper;
import cn.jia.point.dao.GiftUsageMapper;
import cn.jia.point.entity.Gift;
import cn.jia.point.entity.GiftUsage;
import cn.jia.sms.common.Constants;
import cn.jia.user.dao.UserMapper;
import cn.jia.user.entity.User;
import cn.jia.wx.entity.PayOrder;
import cn.jia.wx.service.PayOrderParse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class GiftPayOrderParse extends PayOrderParse {

    @Autowired
    private GiftMapper giftMapper;
    @Autowired
    private GiftUsageMapper giftUsageMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public PayOrder scanPayNotifyResult() {
        PayOrder payOrder = new PayOrder();
        payOrder.setBody("礼品兑换");
        payOrder.setDetail("礼品兑换");
        if(StringUtils.isNotEmpty(this.outTradeNo)) {
            Integer giftUsageId = Integer.parseInt(this.outTradeNo.substring(3));
            GiftUsage giftUsage = giftUsageMapper.selectByPrimaryKey(giftUsageId);
            payOrder.setTotalFee(giftUsage.getPrice());
            payOrder.setProductId(genProductId(giftUsage.getGiftId()));
            payOrder.setOutTradeNo(this.outTradeNo);
            User user = userMapper.selectByJiacn(giftUsage.getJiacn());
            payOrder.setOpenid(user.getOpenid());
        } else {
            Integer giftId = Integer.parseInt(this.productId.substring(3));
            Gift gift = giftMapper.selectByPrimaryKey(giftId);
            payOrder.setTotalFee(gift.getPrice());
            payOrder.setProductId(this.productId);

            GiftUsage giftUsage = new GiftUsage();
            giftUsage.setGiftId(giftId);
            giftUsage.setPrice(gift.getPrice());
            giftUsage.setTime(DateUtil.genTime(new Date()));
            giftUsage.setQuantity(1);
            giftUsage.setJiacn(this.userId);
            giftUsageMapper.insertSelective(giftUsage);

            User user = userMapper.selectByJiacn(this.userId);
            payOrder.setOpenid(user.getOpenid());
            payOrder.setOutTradeNo(genOutTradeNo(giftUsage.getId()));
        }
        return payOrder;
    }

    @Override
    public void orderNotifyResult() {
        Integer giftUsageId = Integer.parseInt(this.outTradeNo.substring(3));
        GiftUsage giftUsage = giftUsageMapper.selectByPrimaryKey(giftUsageId);
        giftUsage.setTime(DateUtil.genTime(new Date()));
        giftUsage.setStatus(Constants.COMMON_ENABLE);
        giftUsageMapper.updateByPrimaryKeySelective(giftUsage);
    }

    private static String genProductId(Integer id){
        return "GIF"+ DataUtil.frontCompWithZore(id, 7);
    }

    private static String genOutTradeNo(Integer id){
        return "GIF"+ DataUtil.frontCompWithZore(id, 7);
    }
}
