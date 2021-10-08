package cn.jia.material.service;

import cn.jia.core.util.DataUtil;
import cn.jia.core.util.DateUtil;
import cn.jia.core.util.StringUtils;
import cn.jia.material.dao.TipMapper;
import cn.jia.material.entity.Tip;
import cn.jia.sms.common.Constants;
import cn.jia.user.dao.UserMapper;
import cn.jia.user.entity.User;
import cn.jia.wx.entity.PayOrder;
import cn.jia.wx.service.AbstractPayOrderParse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class TipPayOrderParse extends AbstractPayOrderParse {

    @Autowired
    private TipMapper tipMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public PayOrder scanPayNotifyResult() {
        PayOrder payOrder = new PayOrder();
        payOrder.setBody("打赏");
        payOrder.setDetail("打赏");
        if(StringUtils.isNotEmpty(this.outTradeNo)) {
            Integer tipId = Integer.parseInt(this.outTradeNo.substring(3));
            Tip tip = tipMapper.selectByPrimaryKey(tipId);
            payOrder.setTotalFee(tip.getPrice());
            payOrder.setProductId(genProductId(tip.getEntityId()));
            payOrder.setOutTradeNo(this.outTradeNo);
            User user = userMapper.selectByJiacn(tip.getJiacn());
            payOrder.setOpenid(user.getOpenid());
        } else {
            Integer entityId = Integer.parseInt(this.productId.substring(3));
            Integer price = 100;
            payOrder.setTotalFee(price);
            payOrder.setProductId(this.productId);

            Tip tip = new Tip();
            tip.setEntityId(entityId);
            tip.setPrice(price);
            tip.setTime(DateUtil.genTime(new Date()));
            tip.setJiacn(this.userId);
            tipMapper.insertSelective(tip);

            User user = userMapper.selectByJiacn(this.userId);
            payOrder.setOpenid(user.getOpenid());
            payOrder.setOutTradeNo(genOutTradeNo(tip.getId()));
        }
        return payOrder;
    }

    @Override
    public void orderNotifyResult() {
        Integer tipId = Integer.parseInt(this.outTradeNo.substring(3));
        Tip tip = tipMapper.selectByPrimaryKey(tipId);
        tip.setTime(DateUtil.genTime(new Date()));
        tip.setStatus(Constants.COMMON_ENABLE);
        tipMapper.updateByPrimaryKeySelective(tip);
    }

    private static String genProductId(Integer id){
        return "TIP"+ DataUtil.frontCompWithZore(id, 7);
    }

    private static String genOutTradeNo(Integer id){
        return "TIP"+ DataUtil.frontCompWithZore(id, 7);
    }
}
