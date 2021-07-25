package me.chanjar.weixin.mp.api.impl;

import com.alibaba.testable.core.annotation.MockMethod;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;

public class WxMpKefuServiceImplMock {
    @MockMethod(targetClass = WxMpService.class)
    public String post(String url, String postData) throws WxErrorException {
        return "{\"msgid\":\"msgid\",\"errcode\":\"0\"}";
    }
}
