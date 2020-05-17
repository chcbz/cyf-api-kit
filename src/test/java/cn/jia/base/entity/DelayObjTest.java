package cn.jia.base.entity;

import cn.jia.core.util.DateUtil;
import cn.jia.core.util.thread.ThreadRequest;
import cn.jia.core.util.thread.ThreadRequestContent;
import cn.jia.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.DelayQueue;

@Slf4j
public class DelayObjTest {

    public static void main(String[] args) {
        DelayQueue<DelayObj> delayQueue = new DelayQueue<>();
        List<User> phraseUserList = new ArrayList<>();
        User u = new User();
        u.setOpenid("23423423423432");
        phraseUserList.add(u);
        User u2 = new User();
        u2.setOpenid("2342343253525");
        phraseUserList.add(u2);
        for(User user : phraseUserList) {
            int max = (int)(DateUtil.todayEnd().getTime() / 1000);
            int min = (int)(new Date().getTime() / 1000);
            Random random = new Random();
            long i = random.nextInt(max) % (max - min + 1);
            delayQueue.offer(new DelayObj(i, user.getOpenid()));
        }
        final int size = phraseUserList.size();
        new ThreadRequest(new ThreadRequestContent() {
            public void doSomeThing() {
                for(int i=0; i<size; i++) {
                    try {
                        DelayObj delayObj = delayQueue.take();
                        System.out.println(delayObj.getData());
                    } catch (Exception e) {
                        log.error("WxSchedule.sendPhrase", e);
                    }
                }
            }
            public void onSuccess() {}
        }).start();
    }
}