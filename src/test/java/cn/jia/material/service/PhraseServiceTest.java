package cn.jia.material.service;

import cn.jia.core.util.DateUtil;
import cn.jia.core.util.HttpUtil;
import cn.jia.core.util.StreamUtil;
import cn.jia.material.entity.Phrase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PhraseServiceTest {

    @Autowired
    private PhraseService phraseService;

    @Test
    public void create() throws Exception {
        File file = new File("C:\\Users\\Think\\Desktop\\毒鸡汤.txt");
        FileInputStream is = new FileInputStream(file);
        String txt = StreamUtil.readText(is);
        txt = txt.replaceAll("([ABCDEFGH]|\\d+)[.．]", "$1、")
                .replaceAll("[ 　]+\r\n", "\r\n")
                .replaceAll("、[ 　]+", "、")
                .replaceAll("\r\n\r\n", "\r\n");
        long now = DateUtil.genTime(new Date());

        int i = 0;
        while (txt.contains((++i) + "、")) {
            String seq = i + "、";
            txt = txt.substring(txt.indexOf(seq));
            Phrase phrase = new Phrase();
            phrase.setClientId("jia_client");
            phrase.setCreateTime(now);
            phrase.setUpdateTime(now);
            phrase.setTag("毒鸡汤");
            phrase.setContent(txt.substring(seq.length(), !txt.contains("\n") ? txt.length() : txt.indexOf("\n")));
            txt = txt.substring(!txt.contains("\n") ? 0 : txt.indexOf("\n") + 1);
            phraseService.create(phrase);
        }
    }

    @Test
    public void th() throws Exception {
        String baseUrl = "https://8zt.cc";
        String link = "/soup/a1dd6.html";
        long now = DateUtil.genTime(new Date());
        while (true) {
            String html = HttpUtil.sendGet(baseUrl + link);
            String key = "<span id=\"sentence\" style=\"font-size: 2rem;\">";
            html = html.substring(html.indexOf(key) + key.length());
            String content = html.substring(0, html.indexOf("</span>")).trim();
            String nextLinkKey = "<a class=\"btn btn-success btn-filled btn-xs\" href=\"";
            html = html.substring(html.indexOf(nextLinkKey) + nextLinkKey.length());
            link = html.substring(0, html.indexOf("\""));
            System.out.println(content + " " + link);

            Phrase phrase = new Phrase();
            phrase.setClientId("jia_client");
            phrase.setCreateTime(now);
            phrase.setUpdateTime(now);
            phrase.setTag("毒鸡汤");
            phrase.setContent(content);
            phraseService.create(phrase);

            Thread.sleep(200);
        }
    }
}