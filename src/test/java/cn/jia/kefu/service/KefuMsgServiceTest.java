package cn.jia.kefu.service;

import cn.jia.kefu.entity.KefuMsgTypeCode;
import cn.jia.test.BaseDbUnitTest;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseSetups;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class KefuMsgServiceTest extends BaseDbUnitTest {
    @Autowired
    private KefuService kefuService;

    @Test
    @DatabaseSetups({
            @DatabaseSetup(value = "classpath:testObject/kefu/kefu_msg_type_init.xml", type = DatabaseOperation.CLEAN_INSERT),
            @DatabaseSetup(value = "classpath:testObject/wx/mp_info_init.xml", type = DatabaseOperation.CLEAN_INSERT),
            @DatabaseSetup(value = "classpath:testObject/wx/mp_template_init.xml", type = DatabaseOperation.CLEAN_INSERT),
            @DatabaseSetup(value = "classpath:testObject/wx/mp_user_init.xml", type = DatabaseOperation.CLEAN_INSERT),
            @DatabaseSetup(value = "classpath:testObject/kefu/kefu_msg_subscribe_init.xml", type = DatabaseOperation.CLEAN_INSERT)
    })
    void sendMessage() throws Exception {
        String clientId = "jia_client";
        assertTrue(kefuService.sendMessage(KefuMsgTypeCode.VOTE, clientId, ""));
    }
}