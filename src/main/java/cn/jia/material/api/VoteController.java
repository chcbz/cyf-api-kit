package cn.jia.material.api;

import cn.jia.core.entity.JSONRequestPage;
import cn.jia.core.entity.JSONResult;
import cn.jia.core.entity.JSONResultPage;
import cn.jia.core.util.JSONUtil;
import cn.jia.material.entity.Vote;
import cn.jia.material.entity.VoteQuestion;
import cn.jia.material.entity.VoteTick;
import cn.jia.material.service.VoteService;
import cn.jia.point.service.PointService;
import com.github.pagehelper.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vote")
public class VoteController {
	
	@Autowired
	private VoteService voteService;
	@Autowired
	private PointService pointService;

	/**
	 * 获取投票信息
	 * @param id
	 * @return
	 */
	@GetMapping(value = "/get")
	public Object findById(@RequestParam(name = "id") Integer id) throws Exception{
		Vote vote = voteService.find(id);
//		String material_url = dictService.selectByDictTypeAndDictValue(Constants.DICT_TYPE_MODULE_URL, Constants.MODULE_URL_MATERIAL).getName();
//		for(VoteQuestion question : vote.getQuestions()) {
//			for(VoteItem item : question.getItems()) {
//				item.setPicLink(material_url + "/" + item.getPicUrl());
//			}
//		}
		return JSONResult.success(vote);
	}

	/**
	 * 创建投票
	 * @param vote 投票信息
	 * @return
	 */
	@PostMapping(value = "/create")
	public Object create(@RequestBody Vote vote) {
		voteService.create(vote);
		return JSONResult.success(vote);
	}

	/**
	 * 更新投票信息
	 * @param vote 投票信息
	 * @return
	 */
	@PostMapping(value = "/update")
	public Object update(@RequestBody Vote vote) {
		voteService.update(vote);
		return JSONResult.success();
	}

	/**
	 * 删除投票
	 * @param id
	 * @return
	 */
	@GetMapping(value = "/delete")
	public Object delete(@RequestParam(name = "id") Integer id) {
		voteService.delete(id);
		return JSONResult.success();
	}
	
	/**
	 * 获取所有投票信息
	 * @return
	 */
	@PostMapping(value = "/list")
	public Object list(@RequestBody JSONRequestPage<String> page) {
		Vote example = JSONUtil.fromJson(page.getSearch(), Vote.class);
		Page<Vote> voteList = voteService.list(page.getPageNum(), page.getPageSize(), example);
		JSONResultPage<Vote> result = new JSONResultPage<>(voteList);
		result.setPageNum(voteList.getPageNum());
		result.setTotal(voteList.getTotal());
		return result;
	}
	
	/**
	 * 获取当前用户所选项
	 * @param voteTick
	 * @return
	 */
	@PostMapping(value = "/get/ticks")
	public Object findTicks(@RequestBody VoteTick voteTick) {
		List<VoteTick> voteTickList = voteService.findTickByJiacn(voteTick);
		return JSONResult.success(voteTickList);
	}

	/**
	 * 随机获取还没做过的投票
	 * @param jiacn 用户jiacn
	 * @return 投票题目
	 */
	@GetMapping(value = "/get/random")
	public Object findRandom(@RequestParam String jiacn) {
		VoteQuestion question = voteService.findOneQuestion(jiacn);
		return JSONResult.success(question);
	}

	/**
	 * 投票
	 * @param voteTick 头票信息
	 * @return 正确与否
	 * @throws Exception 异常信息
	 */
	@PostMapping(value = "/tick")
	public Object tick(@RequestBody VoteTick voteTick) throws Exception {
		boolean tick = voteService.tick(voteTick);
//		if(tick) {
//			VoteQuestion voteQuestion = voteService.findQuestion(voteTick.getQuestionId());
//			pointService.add(voteTick.getJiacn(), voteQuestion.getPoint(), cn.jia.point.common.Constants.POINT_TYPE_VOTE);
//		}
		return JSONResult.success(tick);
	}
	
}
