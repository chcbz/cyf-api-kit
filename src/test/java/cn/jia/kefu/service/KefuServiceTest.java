package cn.jia.kefu.service;

import cn.jia.kefu.entity.KefuMsgSubscribe;
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
        String clientId = "jia_client";
        assertTrue(kefuService.sendMessage(KefuMsgTypeCode.GIFT_USAGE, clientId));
    }

    @Test
    void listMsgSubscribe() {
        KefuMsgSubscribe example = new KefuMsgSubscribe();
        example.setClientId("jia_client");
        example.setTypeCode(KefuMsgTypeCode.GIFT_USAGE.getCode());
        assertTrue(kefuService.listMsgSubscribe(example).size() > 0);
    }
}