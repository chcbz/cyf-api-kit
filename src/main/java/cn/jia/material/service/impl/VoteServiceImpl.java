package cn.jia.material.service.impl;

import cn.jia.core.common.EsSecurityHandler;
import cn.jia.core.exception.EsRuntimeException;
import cn.jia.core.util.DateUtil;
import cn.jia.material.common.Constants;
import cn.jia.material.common.ErrorConstants;
import cn.jia.material.dao.VoteItemMapper;
import cn.jia.material.dao.VoteMapper;
import cn.jia.material.dao.VoteQuestionMapper;
import cn.jia.material.dao.VoteTickMapper;
import cn.jia.material.entity.Vote;
import cn.jia.material.entity.VoteItem;
import cn.jia.material.entity.VoteQuestion;
import cn.jia.material.entity.VoteTick;
import cn.jia.material.service.VoteService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class VoteServiceImpl implements VoteService {
	
	@Autowired
	private VoteMapper voteMapper;
	@Autowired
	private VoteQuestionMapper voteQuestionMapper;
	@Autowired
	private VoteItemMapper voteItemMapper;
	@Autowired
	private VoteTickMapper voteTickMapper;
	
	@Override
	public Vote create(Vote vote) {
		Long now = DateUtil.genTime(new Date());
		//如果保存过，则先清空原来的记录，重新生成
		if(vote.getId() != null) {
			delete(vote.getId());
		}
		List<VoteQuestion> questions = vote.getQuestions();
		vote.setId(null);
		vote.setStartTime(now);
		vote.setClientId(EsSecurityHandler.clientId());
		voteMapper.insertSelective(vote);
		for(VoteQuestion question : questions) {
			List<VoteItem> items = question.getItems();
			question.setId(null);
			question.setVoteId(vote.getId());
			voteQuestionMapper.insertSelective(question);
			for(VoteItem item : items) {
				item.setId(null);
				item.setQuestionId(question.getId());
				voteItemMapper.insertSelective(item);
			}
		}
		
		return vote;
	}

	@Override
	public Vote find(Integer id) throws Exception {
		Vote vote = voteMapper.selectByPrimaryKey(id);
		if(vote == null) {
			throw new EsRuntimeException(ErrorConstants.MEDIA_NOT_EXIST);
		}
		List<VoteQuestion> voteQuestionList = voteQuestionMapper.selectByVoteId(id);
		vote.setQuestions(voteQuestionList);
		for(VoteQuestion question : voteQuestionList) {
			List<VoteItem> voteItemList = voteItemMapper.selectByQuestionId(question.getId());
			question.setItems(voteItemList);
		}
		return vote;
	}

	@Override
	public Vote update(Vote vote) {
		voteMapper.updateByPrimaryKeySelective(vote);
		return vote;
	}

	@Override
	public void delete(Integer id) {
		voteTickMapper.deleteByVoteId(id);
		voteItemMapper.deleteByVoteId(id);
		voteQuestionMapper.deleteByVoteId(id);
		voteMapper.deleteByPrimaryKey(id);
	}

	@Override
	public Page<Vote> list(int pageNo, int pageSize, Vote example) {
		if(example == null) {
			example = new Vote();
		}
		example.setClientId(EsSecurityHandler.clientId());
		PageHelper.startPage(pageNo, pageSize);
		return voteMapper.selectByExample(example);
	}

	@Override
	public List<VoteTick> findTickByJiacn(VoteTick voteTick) {
		return voteTickMapper.selectByJiacn(voteTick);
	}

	@Override
	public boolean tick(VoteTick voteTick) {
		VoteQuestion question = voteQuestionMapper.selectByPrimaryKey(voteTick.getQuestionId());
		voteTick.setVoteId(question.getVoteId());
		long now = DateUtil.genTime(new Date());
		voteTick.setTime(now);
		char[] questionOption = question.getOpt().toCharArray();
		Arrays.sort(questionOption);
		char[] tickOption = voteTick.getOpt().toCharArray();
		Arrays.sort(tickOption);
		boolean tick = String.valueOf(questionOption).equalsIgnoreCase(String.valueOf(tickOption));
		voteTick.setTick(tick ? Constants.COMMON_YES : Constants.COMMON_NO);
		voteTickMapper.insertSelective(voteTick);
		//增加投票数
		Vote vote = voteMapper.selectByPrimaryKey(voteTick.getVoteId());
		Vote upVote = new Vote();
		upVote.setId(voteTick.getVoteId());
		upVote.setNum(vote.getNum() + 1);
		voteMapper.updateByPrimaryKeySelective(upVote);
		List<VoteItem> itemList = voteItemMapper.selectByQuestionId(voteTick.getQuestionId());
		for(VoteItem item : itemList) {
			if(item.getOpt().equalsIgnoreCase(voteTick.getOpt())) {
				VoteItem upItem = new VoteItem();
				upItem.setId(item.getId());
				upItem.setNum(item.getNum() + 1);
				voteItemMapper.updateByPrimaryKeySelective(upItem);
			}
		}

		return tick;
	}

	@Override
	public VoteQuestion findOneQuestion(String jiacn) {
		VoteQuestion question = voteQuestionMapper.selectNoTick(jiacn);
		if (question != null) {
			List<VoteItem> voteItemList = voteItemMapper.selectByQuestionId(question.getId());
			question.setItems(voteItemList);
		}
		return question;
	}

	@Override
	public VoteQuestion findQuestion(Integer id) {
		return voteQuestionMapper.selectByPrimaryKey(id);
	}

	@Override
	public void batchImport(String voteName, String txt, String answer) {
		txt = txt.replaceAll("([ABCDEFGH]|\\d+)[.．]", "$1、")
				.replaceAll("[ 　]+\r\n", "\r\n")
				.replaceAll("、[ 　]+", "、");
		long now = DateUtil.genTime(new Date());
		Vote vote = new Vote();
		vote.setClientId(EsSecurityHandler.clientId());
		vote.setStartTime(now);
		vote.setCloseTime(now + 365 * 24 * 60 * 60);
		vote.setName(voteName);
		voteMapper.insertSelective(vote);

		answer = answer.replaceAll("([ABCDEFGH]|\\d+)[.．]", "$1、")
				.replaceAll("、[ 　]+", "、");

		int i = 0;
		while (txt.contains((++i) + "、")) {
			String seq = i + "、";
			txt = txt.substring(txt.indexOf(seq));
			VoteQuestion voteQuestion = new VoteQuestion();
			voteQuestion.setVoteId(vote.getId());
			voteQuestion.setPoint(1);
			voteQuestion.setTitle(txt.substring(seq.length(), txt.indexOf("\n")));
			txt = txt.substring(txt.indexOf("\n") + 1);
			Pattern pattern = Pattern.compile("[^\\d]" + i + "、([ABCDEFGH])");
			Matcher m = pattern.matcher(answer);
			String opt = "";
			if (m.find()) {
				opt = m.group(1);
			}
			voteQuestion.setOpt(opt);
			voteQuestionMapper.insertSelective(voteQuestion);

			String[] abcd = {"A", "B", "C", "D", "E", "F", "G", "H"};
			for (String s : abcd) {
				VoteItem item = new VoteItem();
				item.setQuestionId(voteQuestion.getId());
				item.setOpt(s);
				if(s.equalsIgnoreCase(opt)) {
					item.setTick(Constants.COMMON_YES);
				}
				if(!txt.contains(s + "、") || txt.indexOf(s + "、") > Math.abs(txt.indexOf("\n"))) {
					break;
				}
				txt = txt.substring(txt.indexOf(s + "、"));
				int endIndex = Math.min(txt.indexOf(" "), txt.indexOf("\n"));
				endIndex = endIndex == -1 ? Math.max(txt.indexOf(" "), txt.indexOf("\n")) : endIndex;
				item.setContent(txt.substring(2, endIndex == -1 ? txt.length() : endIndex));
				txt = txt.substring(endIndex == -1 ? 0 : endIndex + 1);
				voteItemMapper.insertSelective(item);
			}
		}
	}
}
