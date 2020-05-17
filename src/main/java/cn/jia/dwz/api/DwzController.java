package cn.jia.dwz.api;

import cn.jia.core.entity.JSONRequestPage;
import cn.jia.core.entity.JSONResult;
import cn.jia.core.entity.JSONResultPage;
import cn.jia.core.exception.EsRuntimeException;
import cn.jia.core.util.DateUtil;
import cn.jia.dwz.entity.DwzRecord;
import cn.jia.dwz.service.DwzService;
import com.github.pagehelper.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.util.Date;

@Controller
@RequestMapping("/dwz")
public class DwzController {
	
	@Autowired
	private DwzService dwzService;
	
	/**
	 * 处理短链接
	 * @param uri 地址
	 * @return 跳转地址
	 */
	@RequestMapping(value = "/view/{uri:.+}/**", method = RequestMethod.GET)
	public String view(@PathVariable String uri, HttpServletRequest request) {
		final String path = request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
		final String bestMatchingPattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE).toString();
		String arguments = new AntPathMatcher().extractPathWithinPattern(bestMatchingPattern, path);

		if (null != arguments && !arguments.isEmpty()) {
			uri = uri + '/' + arguments;
		}
		DwzRecord record;
		try {
			uri = HtmlUtils.htmlUnescape(URLDecoder.decode(uri, "UTF-8"));
			record = dwzService.view(uri);
			long now = DateUtil.genTime(new Date());
			if(record.getExpireTime() < now) {
				throw new EsRuntimeException();
			}
		} catch (Exception e) {
			return "redirect:https://www.chaoyoufan.net/404.html";
		}
		return "redirect:" + record.getOrgi();
	}

	/**
	 * 获取短链接信息
	 * @param id 短链接ID
	 * @return 短链接信息
	 */
	@RequestMapping(value = "/get", method = RequestMethod.GET)
	@ResponseBody
	public Object findById(@RequestParam(name = "id") Integer id) throws Exception {
		DwzRecord record = dwzService.find(id);
		return JSONResult.success(record);
	}

	/**
	 * 生成短链接
	 * @param record 短链接信息
	 * @return 短链接信息
	 */
	@RequestMapping(value = "/gen", method = RequestMethod.POST)
	@ResponseBody
	public Object gen(@RequestBody DwzRecord record) {
		String uri = dwzService.gen(record.getJiacn(), record.getOrgi(), record.getExpireTime());
		return JSONResult.success(uri);
	}

	/**
	 * 还原短链接
	 * @param uri 链接编码
	 * @return 长链接
	 * @throws Exception 异常信息
	 */
	@RequestMapping(value = "/restore", method = RequestMethod.GET)
	@ResponseBody
	public Object restore(@RequestParam String uri) throws Exception {
		DwzRecord record = dwzService.view(uri);
		return JSONResult.success(record.getOrgi());
	}

	/**
	 * 更新短链接信息
	 * @param record 短链接信息
	 * @return 短链接信息
	 */
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public Object update(@RequestBody DwzRecord record) {
		dwzService.update(record);
		return JSONResult.success(record);
	}

	/**
	 * 删除短链接
	 * @param id 短链接ID
	 * @return 处理结果
	 */
	@RequestMapping(value = "/delete", method = RequestMethod.GET)
	@ResponseBody
	public Object delete(@RequestParam(name = "id") Integer id) {
		dwzService.delete(id);
		return JSONResult.success();
	}

	/**
	 * 获取短链接列表
	 * @param page 搜索条件
	 * @return 短链接列表
	 */
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	@ResponseBody
	public Object list(@RequestBody JSONRequestPage<DwzRecord> page) {
		Page<DwzRecord> list = dwzService.list(page.getSearch(), page.getPageNum(), page.getPageSize());
		@SuppressWarnings({ "unchecked", "rawtypes" })
		JSONResultPage<Object> result = new JSONResultPage(list.getResult());
		result.setPageNum(list.getPageNum());
		result.setTotal(list.getTotal());
		return result;
	}
}
