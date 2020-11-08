package cn.jia.material.api;

import cn.jia.core.entity.JSONRequestPage;
import cn.jia.core.entity.JSONResult;
import cn.jia.core.entity.JSONResultPage;
import cn.jia.core.exception.EsRuntimeException;
import cn.jia.material.common.ErrorConstants;
import cn.jia.material.entity.Tip;
import cn.jia.material.service.TipService;
import com.github.pagehelper.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tip")
@Slf4j
public class TipController {
	
	@Autowired
	private TipService tipService;
	
	/**
	 * 获取打赏信息
	 * @param id 打赏ID
	 * @return 打赏信息
	 */
	@GetMapping(value = "/get")
	public Object findById(@RequestParam(name = "id") Integer id) throws Exception{
		Tip tip = tipService.find(id);
		if(tip == null) {
			throw new EsRuntimeException(ErrorConstants.MEDIA_NOT_EXIST);
		}
		return JSONResult.success(tip);
	}
	
	/**
	 * 创建打赏
	 * @param tip 打赏信息
	 * @return 打赏信息
	 */
	@PostMapping(value = "/create")
	public Object create(@RequestBody Tip tip) {
		tipService.create(tip);
		return JSONResult.success(tip);
	}

	/**
	 * 更新打赏信息
	 * @param tip 打赏信息
	 * @return 打赏信息
	 */
	@PostMapping(value = "/update")
	public Object update(@RequestBody Tip tip) {
		tipService.update(tip);
		return JSONResult.success(tip);
	}

	/**
	 * 删除打赏
	 * @param id 打赏ID
	 * @return 处理结果
	 */
	@GetMapping(value = "/delete")
	public Object delete(@RequestParam(name = "id") Integer id) throws Exception {
		Tip tip = tipService.find(id);
		if(tip == null) {
			throw new EsRuntimeException(ErrorConstants.MEDIA_NOT_EXIST);
		}
		tipService.delete(id);
		return JSONResult.success();
	}

	/**
	 * 获取所有打赏信息
	 * @return 打赏信息
	 */
	@PostMapping(value = "/list")
	public Object list(@RequestBody JSONRequestPage<Tip> page) {
		Page<Tip> list = tipService.list(page.getPageNum(), page.getPageSize(), page.getSearch());
		JSONResultPage<Tip> result = new JSONResultPage<>(list);
		result.setPageNum(list.getPageNum());
		result.setTotal(list.getTotal());
		return result;
	}
}
