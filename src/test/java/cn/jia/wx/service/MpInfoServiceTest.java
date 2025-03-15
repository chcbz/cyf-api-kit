package cn.jia.wx.service;

import cn.jia.test.BaseDbUnitTest;
import cn.jia.test.DbUnitHelper;
import cn.jia.wx.entity.MpInfoEntity;
import cn.jia.wx.entity.MpInfoVO;
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
class MpInfoServiceTest extends BaseDbUnitTest {
    @Autowired
    private MpInfoService mpInfoService;
    @Autowired
    private DataSource dataSource;
    @Value("classpath:testObject/wx/mp_info_init.json")
    private Resource mpInfoResource;

    @Test
    @DatabaseSetup(value = "classpath:testObject/wx/mp_info_init.xml", type = DatabaseOperation.CLEAN_INSERT)
    void findWxMpService() {
        WxMpService wxMpService = mpInfoService.findWxMpService("wxd59557202ddff2d5");
        assertNotNull(wxMpService);
    }

    @Test
    void create() {
        MpInfoEntity mpInfo = DbUnitHelper.readJsonEntity(mpInfoResource, MpInfoEntity.class);
        assertNotNull(mpInfoService.create(Objects.requireNonNull(mpInfo)));
    }

    @Test
    @DatabaseSetup(value = "classpath:testObject/wx/mp_info_init.xml", type = DatabaseOperation.CLEAN_INSERT)
    void find() {
        assertNotNull(mpInfoService.get(1L));
    }

    @Test
    @DatabaseSetup(value = "classpath:testObject/wx/mp_info_init.xml", type = DatabaseOperation.CLEAN_INSERT)
    void findByKey() {
        assertNotNull(mpInfoService.findByKey("wxd59557202ddff2d5"));
    }

    @Test
    @DatabaseSetup(value = "classpath:testObject/wx/mp_info_init.xml", type = DatabaseOperation.CLEAN_INSERT)
    void list() {
        MpInfoVO mpInfoExample = new MpInfoVO();
        mpInfoExample.setOriginal("gh_336235a5d843");
        mpInfoExample.setClientIdList(Arrays.asList("jia_client", "234"));
        assertEquals(mpInfoService.findList(mpInfoExample).size(), 1);
    }

    @Test
    @DatabaseSetup(value = "classpath:testObject/wx/mp_info_init.xml", type = DatabaseOperation.CLEAN_INSERT)
    void testList() {
        MpInfoVO mpInfoExample = new MpInfoVO();
        mpInfoExample.setOriginal("gh_336235a5d843");
        assertEquals(mpInfoService.findPage(mpInfoExample, 1, 10).getSize(), 1);
    }

    @Test
    @DatabaseSetup(value = "classpath:testObject/wx/mp_info_init.xml", type = DatabaseOperation.CLEAN_INSERT)
    void update() {
        MpInfoEntity mpInfo = new MpInfoEntity();
        mpInfo.setAcid(1L);
        mpInfo.setCity("dg");
        assertNotNull(mpInfoService.update(mpInfo));
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
        assertEquals(mpInfoService.findList(new MpInfoVO()).size(), 1);
    }

    @Test
    @Disabled
    void printDataSet() {
        log.info(DbUnitHelper.printDataSet(dataSource, "wx_mp_info", "select * from wx_mp_info where acid = 1"));
    }
}