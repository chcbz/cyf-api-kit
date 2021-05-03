package cn.jia.kefu.service;

import cn.jia.kefu.entity.KefuMsgTypeCode;
import cn.jia.test.BaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertTrue;

class KefuServiceTest extends BaseTest {
    @Autowired
    private KefuService kefuService;

//    public static class Mock {
//        @MockMethod(targetClass = WxMpTemplateMsgService.class)
//        private String sendTemplateMsg(WxMpTemplateMessage templateMessage) throws WxErrorException {
//            return "";
//        }
//    }

    @Test
    void sendMessage() throws Exception {
        String clientId = "";
        assertTrue(kefuService.sendMessage(KefuMsgTypeCode.GIFT_USAGE, clientId));
    }
}