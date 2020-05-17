package cn.jia.material.service.impl;

import cn.jia.core.common.EsSecurityHandler;
import cn.jia.core.exception.EsRuntimeException;
import cn.jia.core.util.DateUtil;
import cn.jia.core.util.StringUtils;
import cn.jia.material.common.ErrorConstants;
import cn.jia.material.dao.PhraseMapper;
import cn.jia.material.dao.PhraseVoteMapper;
import cn.jia.material.entity.Phrase;
import cn.jia.material.entity.PhraseVote;
import cn.jia.material.service.PhraseService;
import cn.jia.point.common.Constants;
import cn.jia.point.service.PointService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
public class PhraseServiceImpl implements PhraseService {
	
	@Autowired
	private PhraseMapper phraseMapper;
	@Autowired
	private PhraseVoteMapper phraseVoteMapper;
	@Autowired
	private PointService pointService;
	@Autowired
	private RestHighLevelClient restHighLevelClient;

	private static String INDEX_NAME = "phrase";
	
	@Override
	public Phrase create(Phrase phrase) throws Exception {
		//判断是否有相似的短语
		MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("content", phrase.getContent());
		SearchSourceBuilder builder = SearchSourceBuilder.searchSource();
		builder.query(matchQuery);
		SearchRequest request = new SearchRequest(INDEX_NAME);
		request.source(builder);

		SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
		SearchHits hits = response.getHits();
		if (hits.getMaxScore() > 40) {
			throw new EsRuntimeException(ErrorConstants.PHRASE_HAS_EXIST);
		}

		Long now = DateUtil.genTime(new Date());
		phrase.setCreateTime(now);
		phrase.setUpdateTime(now);
		phrase.setClientId(EsSecurityHandler.clientId());
		phraseMapper.insertSelective(phrase);
		return phrase;
	}

	@Override
	public Phrase find(Integer id) {
		return phraseMapper.selectByPrimaryKey(id);
	}

	@Override
	public Phrase update(Phrase phrase) {
		Long now = DateUtil.genTime(new Date());
		phrase.setUpdateTime(now);
		phraseMapper.updateByPrimaryKeySelective(phrase);
		return phrase;
	}

	@Override
	public void delete(Integer id) {
		phraseMapper.deleteByPrimaryKey(id);

		//删除索引
		DeleteRequest request = new DeleteRequest(INDEX_NAME);
		request.id(String.valueOf(id));
		try {
			restHighLevelClient.delete(request, RequestOptions.DEFAULT);
		} catch (Exception e) {
			log.error("delete phrase index error", e);
		}
	}

	@Override
	public Phrase findRandom(Phrase example) {
		return phraseMapper.selectRandom(example);
	}

	@Override
	public void vote(PhraseVote vote) throws Exception {
		Phrase phrase = phraseMapper.selectByPrimaryKey(vote.getPhraseId());
		if(phrase == null) {
			throw new EsRuntimeException(ErrorConstants.DATA_NOT_FOUND);
		}
		Phrase upPhrase = new Phrase();
		upPhrase.setId(phrase.getId());
		if(vote.getVote().equals(1)) {
			upPhrase.setUp(phrase.getUp() + 1);
			//每被点赞10次，增加1积分
			if(StringUtils.isNotEmpty(phrase.getJiacn()) && upPhrase.getUp() % 10 == 0) {
				pointService.add(phrase.getJiacn(), 1, Constants.POINT_TYPE_PHRASE);
			}
		} else {
			upPhrase.setDown(phrase.getDown() + 1);
		}
		Long now = DateUtil.genTime(new Date());
		upPhrase.setUpdateTime(now);
		phraseMapper.updateByPrimaryKeySelective(upPhrase);

		vote.setTime(now);
		phraseVoteMapper.insertSelective(vote);
	}

	@Override
	public void read(Integer id) throws Exception {
		Phrase phrase = phraseMapper.selectByPrimaryKey(id);
		if(phrase == null) {
			throw new EsRuntimeException(ErrorConstants.DATA_NOT_FOUND);
		}
		Phrase upPhrase = new Phrase();
		upPhrase.setId(phrase.getId());
		upPhrase.setPv(phrase.getPv() + 1);
		Long now = DateUtil.genTime(new Date());
		upPhrase.setUpdateTime(now);
		phraseMapper.updateByPrimaryKeySelective(upPhrase);
	}
}
