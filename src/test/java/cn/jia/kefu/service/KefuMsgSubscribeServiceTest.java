package cn.jia.kefu.service;

import cn.jia.kefu.entity.KefuMsgSubscribeEntity;
import cn.jia.kefu.entity.KefuMsgTypeCode;
import cn.jia.test.BaseDbUnitTest;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertFalse;

@Slf4j
class KefuMsgSubscribeServiceTest extends BaseDbUnitTest {
    @Autowired
    private KefuMsgSubscribeService kefuMsgSubscribeService;

    @Test
    @DatabaseSetup(value = "classpath:testObject/kefu/kefu_msg_subscribe_init.xml", type = DatabaseOperation.CLEAN_INSERT)
    void listMsgSubscribe() {
        KefuMsgSubscribeEntity example = new KefuMsgSubscribeEntity();
        example.setClientId("jia_client");
        example.setTypeCode(KefuMsgTypeCode.GIFT_USAGE.getCode());
        assertFalse(kefuMsgSubscribeService.findList(example).isEmpty());
    }
}