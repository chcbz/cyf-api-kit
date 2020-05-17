package cn.jia.base.service;

import cn.jia.core.util.JSONUtil;
import cn.jia.material.model.PhraseModel;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ElasticsearchServiceTest {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Test
    public void search() {
        MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("content", "小时候的我们很快乐。因为小时候穷和丑没有现在那么明显");
        SearchSourceBuilder builder = SearchSourceBuilder.searchSource();
        builder.query(matchQuery);
        List<PhraseModel> list = elasticsearchService.search("phrase", builder, PhraseModel.class);
        System.out.println(JSONUtil.toJson(list));
    }
}