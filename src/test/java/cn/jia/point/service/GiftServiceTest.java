package cn.jia.point.service;

import cn.jia.BaseTest;
import cn.jia.DbUnitHelper;
import cn.jia.point.entity.Gift;
import com.github.pagehelper.Page;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class GiftServiceTest extends BaseTest {
    @Autowired
    private GiftService giftService;
    @Autowired
    private DataSource dataSource;
    @Value("classpath:testObject/point/point_gift_init.json")
    private Resource resource;

    @Test
    void create() {
        Gift gift = DbUnitHelper.readJsonEntity(resource, Gift.class);
        gift = giftService.create(gift);
        assertNotNull(gift);
    }

    @Test
    @DatabaseSetup(value = "classpath:testObject/point/point_gift_init.xml", type = DatabaseOperation.CLEAN_INSERT)
    void find() throws Exception {
        Gift gift = giftService.find(1);
        assertNotNull(gift);
    }

    @Test
    void update() {
        Gift gift = new Gift();
        gift.setId(1);
        gift.setName("testName");
        giftService.update(gift);
        assertTrue(true);
    }

    @Test
    void delete() {
        giftService.delete(1);
        assertTrue(true);
    }

    @Test
    @DatabaseSetup(value = "classpath:testObject/point/point_gift_init.xml", type = DatabaseOperation.CLEAN_INSERT)
    void list() {
        Page<Gift> giftPage = giftService.list(1, 10, null);
        assertNotEquals(giftPage.size(), 0);
    }

    @Test
    void usage() {
    }

    @Test
    void usageCancel() {
    }

    @Test
    void usageDelete() {
    }

    @Test
    void usageListByGift() {
    }

    @Test
    void usageListByUser() {
    }

    @Test
    void printDataSet() {
        log.info(DbUnitHelper.printDataSet(dataSource, "point_gift", "select * from point_gift where id = 1"));
    }
}