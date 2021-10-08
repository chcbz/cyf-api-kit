package cn.jia.wx.service;

import cn.jia.core.common.EsSecurityHandler;
import cn.jia.core.exception.EsRuntimeException;
import cn.jia.core.util.BeanUtil;
import cn.jia.core.util.DateUtil;
import cn.jia.core.util.StringUtils;
import cn.jia.wx.common.ErrorConstants;
import cn.jia.wx.entity.PayInfo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.BeanUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



@Service
public class PayInfoService {

    @Autowired
    private IPayInfoService payInfoService;
    private Map<String, WxPayService> wxPayServiceMap;

    @PostConstruct
    public void init() {
        wxPayServiceMap = new HashMap<>(16);
        List<PayInfo> payInfoList = payInfoService.list();
        for(PayInfo pay : payInfoList) {
            WxPayService wxPayService = new WxPayServiceImpl();
            WxPayConfig config = new WxPayConfig();
            BeanUtil.copyPropertiesIgnoreNull(pay, config);
            wxPayService.setConfig(config);
            wxPayServiceMap.put(pay.getAppId(), wxPayService);
        }
    }

    public WxPayService findWxPayService(HttpServletRequest request) throws Exception {
        String appid = request.getParameter("appid");
        if(StringUtils.isEmpty(appid)) {
            throw new EsRuntimeException(ErrorConstants.APPID_NOT_NULL);
        }
        return findWxPayService(appid);
    }

    public WxPayService findWxPayService(String key) throws Exception {
        if(StringUtils.isEmpty(key)) {
            throw new EsRuntimeException(ErrorConstants.APPID_NOT_NULL);
        }
        WxPayService wxPayService = wxPayServiceMap.get(key);
        if(wxPayService == null) {
            PayInfo info = findByKey(key);
            if(info != null) {
                wxPayService = new WxPayServiceImpl();
                WxPayConfig config = new WxPayConfig();
                BeanUtil.copyPropertiesIgnoreNull(info, config);
                wxPayService.setConfig(config);
                wxPayServiceMap.put(info.getAppId(), wxPayService);
            }
        }
        if(wxPayService == null) {
            throw new EsRuntimeException(ErrorConstants.WXMP_NOT_EXIST);
        }
        return wxPayService;
    }

    public PayInfo create(PayInfo payInfo) {
        //保存公众号信息
        Long now = DateUtil.genTime(new Date());
        payInfo.setClientId(EsSecurityHandler.clientId());
        payInfo.setCreateTime(now);
        payInfo.setUpdateTime(now);
        payInfoService.save(payInfo);
        return payInfo;
    }

    public PayInfo find(Integer id) {
        return payInfoService.getById(id);
    }

    public PayInfo findByKey(String key) {
        LambdaQueryWrapper<PayInfo> queryWrapper = Wrappers.lambdaQuery(new PayInfo()).eq(PayInfo::getAppId, key);
        return payInfoService.getOne(queryWrapper);
    }

    public List<PayInfo> list(PayInfo example) {
        QueryWrapper<PayInfo> queryWrapper = new QueryWrapper<>();
        return payInfoService.list(queryWrapper.allEq(BeanUtils.beanToMap(example), false));
    }

    public PageInfo<PayInfo> list(PayInfo example, int pageNo, int pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<PayInfo> payOrderList = list(example);
        return new PageInfo<>(payOrderList);
    }

    public PayInfo update(PayInfo payInfo) {
        //保存公众号信息
        Long now = DateUtil.genTime(new Date());
        payInfo.setUpdateTime(now);
        payInfoService.updateById(payInfo);
        return payInfo;
    }

    public void delete(Integer id) {
        payInfoService.removeById(id);
    }

    public List<PayInfo> selectAll() {
        return payInfoService.list();
    }

}