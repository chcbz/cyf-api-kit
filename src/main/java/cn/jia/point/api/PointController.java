package cn.jia.point.api;

import cn.jia.core.entity.JSONResult;
import cn.jia.point.entity.Record;
import cn.jia.point.entity.Referral;
import cn.jia.point.entity.Sign;
import cn.jia.point.service.PointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/point")
public class PointController {
	
	@Autowired
	private PointService pointService;
	
	/**
	 * 新用户
	 * @param record
	 * @return
	 * @throws Exception
	 */
	@PreAuthorize("hasAuthority('point-init')")
	@RequestMapping(value = "/init", method = RequestMethod.POST)
	public Object init(@RequestBody Record record) throws Exception {
		record = pointService.init(record);
		return JSONResult.success(record);
	}
	
	/**
	 * 签到
	 * @param sign
	 * @return
	 * @throws Exception
	 */
	@PreAuthorize("hasAuthority('point-sign')")
	@RequestMapping(value = "/sign", method = RequestMethod.POST)
	public Object sign(@RequestBody Sign sign) throws Exception {
		Record record = pointService.sign(sign);
		return JSONResult.success(record);
	}

	/**
	 * 推荐
	 * @param referral
	 * @return
	 * @throws Exception
	 */
	@PreAuthorize("hasAuthority('point-referral')")
	@RequestMapping(value = "/referral", method = RequestMethod.POST)
	public Object referral(@RequestBody Referral referral) throws Exception {
		Record record = pointService.referral(referral);
		return JSONResult.success(record);
	}
	
	/**
	 * 试试手气
	 * @param record
	 * @return
	 * @throws Exception
	 */
	@PreAuthorize("hasAuthority('point-luck')")
	@RequestMapping(value = "/luck", method = RequestMethod.POST)
	public Object luck(@RequestBody Record record) throws Exception {
		record = pointService.luck(record);
		return JSONResult.success(record);
	}
}
