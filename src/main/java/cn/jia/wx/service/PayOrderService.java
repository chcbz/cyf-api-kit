package cn.jia.wx.service;

import cn.jia.core.util.DateUtil;
import cn.jia.wx.entity.PayOrder;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.BeanUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class PayOrderService {

    @Autowired
    private IPayOrderService payOrderService;

    public PayOrder create(PayOrder payOrder) {
        Long now = DateUtil.genTime(new Date());
        payOrder.setCreateTime(now);
        payOrder.setUpdateTime(now);
        payOrderService.save(payOrder);
        return payOrder;
    }

    public PayOrder find(Integer id) {
        return payOrderService.getById(id);
    }

    public List<PayOrder> list(PayOrder example) {
        QueryWrapper<PayOrder> queryWrapper = new QueryWrapper<>();
        return payOrderService.list(queryWrapper.allEq(BeanUtils.beanToMap(example), false));
    }

    public PageInfo<PayOrder> list(PayOrder example, int pageNo, int pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<PayOrder> payOrderList = list(example);
        return new PageInfo<>(payOrderList);
    }

    public PayOrder update(PayOrder payOrder) {
        Long now = DateUtil.genTime(new Date());
        payOrder.setUpdateTime(now);
        payOrderService.updateById(payOrder);
        return payOrder;
    }

    public void delete(Integer id) {
        payOrderService.removeById(id);
    }

    public List<PayOrder> selectAll() {
        return payOrderService.list();
    }

}