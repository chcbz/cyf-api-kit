package cn.jia.point.api;

import cn.jia.core.common.EsSecurityHandler;
import cn.jia.core.entity.JSONRequestPage;
import cn.jia.core.entity.JSONResult;
import cn.jia.core.entity.JSONResultPage;
import cn.jia.point.entity.Gift;
import cn.jia.point.entity.GiftExample;
import cn.jia.point.entity.GiftUsage;
import cn.jia.point.service.GiftService;
import com.github.pagehelper.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/gift")
public class GiftController {
	
	@Autowired
	private GiftService giftService;
	
	/**
	 * 获取礼品信息
	 * @param id
	 * @return
	 */
//	@PreAuthorize("hasAuthority('gift-get')")
	@RequestMapping(value = "/get", method = RequestMethod.GET)
	public Object findById(@RequestParam(name = "id") Integer id) throws Exception {
		Gift gift = giftService.find(id);
		return JSONResult.success(gift);
	}

	/**
	 * 创建礼品
	 * @param gift
	 * @return
	 */
	@PreAuthorize("hasAuthority('gift-create')")
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public Object create(@RequestBody Gift gift) {
		giftService.create(gift);
		return JSONResult.success();
	}

	/**
	 * 更新礼品信息
	 * @param gift
	 * @return
	 */
	@PreAuthorize("hasAuthority('gift-update')")
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public Object update(@RequestBody Gift gift) {
		giftService.update(gift);
		return JSONResult.success();
	}

	/**
	 * 删除礼品
	 * @param id
	 * @return
	 */
	@PreAuthorize("hasAuthority('gift-delete')")
	@RequestMapping(value = "/delete", method = RequestMethod.GET)
	public Object delete(@RequestParam(name = "id") Integer id) {
		giftService.delete(id);
		return JSONResult.success();
	}
	
	/**
	 * 获取所有礼品信息
	 * @return
	 */
//	@PreAuthorize("hasAuthority('gift-list')")
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	public Object list(@RequestBody JSONRequestPage<GiftExample> page) {
		Page<Gift> giftList = giftService.list(page.getPageNum(), page.getPageSize(), page.getSearch());
		JSONResultPage<Gift> result = new JSONResultPage<>(giftList.getResult());
		result.setPageNum(giftList.getPageNum());
		result.setTotal(giftList.getTotal());
		return result;
	}
	
	/**
	 * 礼品兑换
	 * @param giftUsage
	 * @return
	 * @throws Exception 
	 */
//	@PreAuthorize("hasAuthority('gift-usage_add')")
	@RequestMapping(value = "/usage/add", method = RequestMethod.POST)
	public Object usageAdd(@RequestBody GiftUsage giftUsage, HttpServletRequest request) throws Exception {
		giftUsage.setClientId(EsSecurityHandler.clientId());
		giftService.usage(giftUsage);
		// 通知管理员

		return JSONResult.success(giftUsage);
	}

	/**
	 * 取消礼品兑换
	 * @param giftUsageId 订单ID
	 * @return 结果
	 * @throws Exception 异常
	 */
	@RequestMapping(value = "/usage/cancel/{giftUsageId}", method = RequestMethod.POST)
	public Object usageCancel(@PathVariable Integer giftUsageId) throws Exception {
		giftService.usageCancel(giftUsageId);
		return JSONResult.success(giftUsageId);
	}

	/**
	 * 删除订单
	 * @param giftUsageId 订单ID
	 * @return 处理结果
	 * @throws Exception 异常
	 */
	@RequestMapping(value = "/usage/delete/{giftUsageId}", method = RequestMethod.POST)
	public Object usageDelete(@PathVariable Integer giftUsageId) throws Exception {
		giftService.usageDelete(giftUsageId);
		return JSONResult.success(giftUsageId);
	}
	
	/**
	 * 根据礼品ID查找使用情况
	 * @param page
	 * @param giftId
	 * @return
	 */
//	@PreAuthorize("hasAuthority('gift-usage_list_gift')")
	@RequestMapping(value = "/usage/list/gift/{giftId}", method = RequestMethod.POST)
	public Object usageListByGift(@RequestBody JSONRequestPage<String> page, @PathVariable Integer giftId) {
		Page<GiftUsage> usageList = giftService.usageListByGift(page.getPageNum(), page.getPageSize(), giftId);
		JSONResultPage<GiftUsage> result = new JSONResultPage<>(usageList.getResult());
		result.setPageNum(usageList.getPageNum());
		result.setTotal(usageList.getTotal());
		return result;
	}
	
	/**
	 * 根据Jia账号查找使用情况
	 * @param page
	 * @param user
	 * @return
	 */
//	@PreAuthorize("hasAuthority('gift-usage_list_user')")
	@RequestMapping(value = "/usage/list/user/{user}", method = RequestMethod.POST)
	public Object usageListByUser(@RequestBody JSONRequestPage<GiftUsage> page, @PathVariable String user) {
		Page<GiftUsage> usageList = giftService.usageListByUser(page.getPageNum(), page.getPageSize(), user);
		JSONResultPage<GiftUsage> result = new JSONResultPage<>(usageList.getResult());
		result.setPageNum(usageList.getPageNum());
		result.setTotal(usageList.getTotal());
		return result;
	}
	
}
