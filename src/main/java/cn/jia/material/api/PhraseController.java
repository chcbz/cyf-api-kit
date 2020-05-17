package cn.jia.material.api;

import cn.jia.core.entity.JSONResult;
import cn.jia.core.exception.EsRuntimeException;
import cn.jia.material.common.ErrorConstants;
import cn.jia.material.entity.Phrase;
import cn.jia.material.entity.PhraseVote;
import cn.jia.material.service.PhraseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/phrase")
@Slf4j
public class PhraseController {
	
	@Autowired
	private PhraseService phraseService;
	
	/**
	 * 获取短语信息
	 * @param id 短语ID
	 * @return 短语信息
	 */
	@GetMapping(value = "/get")
	public Object findById(@RequestParam(name = "id") Integer id) throws Exception{
		Phrase phrase = phraseService.find(id);
		if(phrase == null) {
			throw new EsRuntimeException(ErrorConstants.MEDIA_NOT_EXIST);
		}
		return JSONResult.success(phrase);
	}
	
	/**
	 * 创建短语
	 * @param phrase 短语信息
	 * @return 短语信息
	 */
	@PostMapping(value = "/create")
	public Object create(@RequestBody Phrase phrase) throws Exception {
		phraseService.create(phrase);
		return JSONResult.success();
	}

	/**
	 * 更新短语信息
	 * @param phrase 短语信息
	 * @return 短语信息
	 */
	@PostMapping(value = "/update")
	public Object update(@RequestBody Phrase phrase) {
		phraseService.update(phrase);
		return JSONResult.success();
	}

	/**
	 * 删除短语
	 * @param id 短语ID
	 * @return 处理结果
	 */
	@GetMapping(value = "/delete")
	public Object delete(@RequestParam(name = "id") Integer id) throws Exception {
		Phrase phrase = phraseService.find(id);
		if(phrase == null) {
			throw new EsRuntimeException(ErrorConstants.MEDIA_NOT_EXIST);
		}
		phraseService.delete(id);
		return JSONResult.success();
	}
	
	/**
	 * 获取所有短语信息
	 * @return 短语列表
	 */
	@PostMapping(value = "/get/random")
	public Object getRandom(@RequestBody Phrase example) throws Exception {
		Phrase phrase = phraseService.findRandom(example);
		if(phrase == null) {
			throw new EsRuntimeException(ErrorConstants.MEDIA_NOT_EXIST);
		}
		return JSONResult.success(phrase);
	}

	/**
	 * 投票
	 * @param vote 投票信息
	 * @return 处理结果
	 * @throws Exception 异常信息
	 */
	@PostMapping(value = "/vote")
	public Object vote(@RequestBody PhraseVote vote) throws Exception {
		phraseService.vote(vote);
		return JSONResult.success();
	}

	/**
	 * 访问
	 * @param id 短语ID
	 * @return 处理结果
	 * @throws Exception 异常信息
	 */
	@GetMapping(value = "/read")
	public Object read(@RequestParam Integer id) throws Exception {
		phraseService.read(id);
		return JSONResult.success();
	}
}
