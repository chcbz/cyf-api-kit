package cn.jia.material.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class VoteServiceTest {

    @Autowired
    private VoteService voteService;

    @Test
    public void batchImport() throws Exception {
        voteService.batchImport("C:\\Users\\Think\\Desktop\\个人代理销售人员资格考试题库.txt", "C:\\Users\\Think\\Desktop\\个人代理销售人员资格考试题库答案.txt");
    }
}