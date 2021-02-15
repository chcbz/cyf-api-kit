package cn.jia.material.service;

import cn.jia.core.util.StreamUtil;
import cn.jia.test.BaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

public class VoteServiceTest extends BaseTest {

    @Autowired
    private VoteService voteService;
    @Value("classpath:testObject/material/question.txt")
    private Resource questionResource;
    @Value("classpath:testObject/material/answer.txt")
    private Resource answerResource;

    @Test
    void batchImport() throws Exception {
        String question = StreamUtil.readText(questionResource.getInputStream());
        String answer = StreamUtil.readText(answerResource.getInputStream());
        voteService.batchImport("题目", question, answer);
    }
}