package cn.jia.base.service;

import cn.jia.BaseTest;
import cn.jia.core.util.JSONUtil;
import cn.jia.material.model.PhraseModel;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.IOException;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ElasticsearchServiceTest extends BaseTest {

    @InjectMocks
    private ElasticsearchService elasticsearchService;
    @Mock
    private RestHighLevelClient restHighLevelClient;

    @Test
    void search() throws IOException {
        SearchHit searchHit = mock(SearchHit.class);
        when(searchHit.getSourceAsString()).thenReturn(JSONUtil.toJson(new PhraseModel()));
        SearchHit[] hits = new SearchHit[1];
        hits[0] = searchHit;
        SearchHits searchHits = mock(SearchHits.class);
        when(searchHits.getHits()).thenReturn(hits);
        SearchResponse response = mock(SearchResponse.class);
        when(response.getHits()).thenReturn(searchHits);
        when(restHighLevelClient.search(any(), any())).thenReturn(response);

        MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("content", "小时候的我们很快乐。因为小时候穷和丑没有现在那么明显");
        SearchSourceBuilder builder = SearchSourceBuilder.searchSource();
        builder.query(matchQuery);
        List<PhraseModel> list = elasticsearchService.search("phrase", builder, PhraseModel.class);
        System.out.println(JSONUtil.toJson(list));
    }
}