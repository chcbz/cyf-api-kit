package cn.jia.kefu.service;

import cn.jia.kefu.entity.KefuMsgSubscribe;
import cn.jia.kefu.entity.KefuMsgTypeCode;
import cn.jia.test.BaseTest;
import cn.jia.test.DbUnitHelper;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseSetups;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class KefuServiceTest extends BaseTest {
    @Autowired
    private KefuService kefuService;
    @Autowired
    private DataSource dataSource;

//    public static class Mock {
//        @MockMethod(targetClass = WxMpTemplateMsgService.class)
//        private String sendTemplateMsg(WxMpTemplateMessage templateMessage) throws WxErrorException {
//            return "";
//        }
//    }

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
        assertTrue(kefuService.sendMessage(KefuMsgTypeCode.GIFT_USAGE, clientId));
    }

    @Test
    @DatabaseSetup(value = "classpath:testObject/kefu/kefu_msg_subscribe_init.xml", type = DatabaseOperation.CLEAN_INSERT)
    void listMsgSubscribe() {
        KefuMsgSubscribe example = new KefuMsgSubscribe();
        example.setClientId("jia_client");
        example.setTypeCode(KefuMsgTypeCode.GIFT_USAGE.getCode());
        assertTrue(kefuService.listMsgSubscribe(example).size() > 0);
    }

    @Test
    @Disabled
    void printDataSet() {
        log.info(DbUnitHelper.printDataSet(dataSource, "kefu_msg_type", "select * from kefu_msg_type"));
        log.info(DbUnitHelper.printDataSet(dataSource, "wx_mp_info", "select * from wx_mp_info"));
        log.info(DbUnitHelper.printDataSet(dataSource, "wx_mp_template", "select * from wx_mp_template"));
        log.info(DbUnitHelper.printDataSet(dataSource, "wx_mp_user", "select * from wx_mp_user"));
    }
}