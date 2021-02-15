package cn.jia.wx.service;

import cn.jia.test.BaseTest;
import cn.jia.test.DbUnitHelper;
import cn.jia.wx.entity.MpInfo;
import cn.jia.wx.entity.MpInfoExample;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class MpInfoServiceTest extends BaseTest {
    @Autowired
    private MpInfoService mpInfoService;
    @Autowired
    private DataSource dataSource;
    @Value("classpath:testObject/wx/mp_info_init.json")
    private Resource mpInfoResource;

    @Test
    @DatabaseSetup(value = "classpath:testObject/wx/mp_info_init.xml", type = DatabaseOperation.CLEAN_INSERT)
    void findWxMpService() throws Exception {
        WxMpService wxMpService = mpInfoService.findWxMpService("wxd59557202ddff2d5");
        assertNotNull(wxMpService);
    }

    @Test
    void create() {
        MpInfo mpInfo = DbUnitHelper.readJsonEntity(mpInfoResource, MpInfo.class);
        assertTrue(mpInfoService.create(Objects.requireNonNull(mpInfo)));
    }

    @Test
    @DatabaseSetup(value = "classpath:testObject/wx/mp_info_init.xml", type = DatabaseOperation.CLEAN_INSERT)
    void find() {
        assertNotNull(mpInfoService.find(1));
    }

    @Test
    @DatabaseSetup(value = "classpath:testObject/wx/mp_info_init.xml", type = DatabaseOperation.CLEAN_INSERT)
    void findByKey() {
        assertNotNull(mpInfoService.findByKey("wxd59557202ddff2d5"));
    }

    @Test
    @DatabaseSetup(value = "classpath:testObject/wx/mp_info_init.xml", type = DatabaseOperation.CLEAN_INSERT)
    void list() {
        MpInfoExample mpInfoExample = new MpInfoExample();
        mpInfoExample.setOriginal("gh_336235a5d843");
        mpInfoExample.setClientIdList(Arrays.asList("jia_client", "234"));
        assertEquals(mpInfoService.list(mpInfoExample).size(), 1);
    }

    @Test
    @DatabaseSetup(value = "classpath:testObject/wx/mp_info_init.xml", type = DatabaseOperation.CLEAN_INSERT)
    void testList() {
        MpInfoExample mpInfoExample = new MpInfoExample();
        mpInfoExample.setOriginal("gh_336235a5d843");
        assertEquals(mpInfoService.list(mpInfoExample, 1, 10).getSize(), 1);
    }

    @Test
    @DatabaseSetup(value = "classpath:testObject/wx/mp_info_init.xml", type = DatabaseOperation.CLEAN_INSERT)
    void update() {
        MpInfo mpInfo = new MpInfo();
        mpInfo.setAcid(1);
        mpInfo.setCity("dg");
        assertTrue(mpInfoService.update(mpInfo));
    }

    @Test
    @DatabaseSetup(value = "classpath:testObject/wx/mp_info_init.xml", type = DatabaseOperation.CLEAN_INSERT)
    void delete() {
        assertTrue(mpInfoService.delete(1));
        assertFalse(mpInfoService.delete(1));
    }

    @Test
    @DatabaseSetup(value = "classpath:testObject/wx/mp_info_init.xml", type = DatabaseOperation.CLEAN_INSERT)
    void selectAll() {
        assertEquals(mpInfoService.selectAll().size(), 1);
    }

    @Test
    @Disabled
    void printDataSet() {
        log.info(DbUnitHelper.printDataSet(dataSource, "wx_mp_info", "select * from wx_mp_info where acid = 1"));
    }
}