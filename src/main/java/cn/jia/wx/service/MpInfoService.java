package cn.jia.wx.service;

import cn.jia.core.exception.EsRuntimeException;
import cn.jia.core.util.BeanUtil;
import cn.jia.core.util.DateUtil;
import cn.jia.core.util.StringUtils;
import cn.jia.wx.common.ErrorConstants;
import cn.jia.wx.entity.MpInfo;
import cn.jia.wx.entity.MpInfoExample;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.BeanUtils;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import me.chanjar.weixin.mp.api.WxMpInMemoryConfigStorage;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MpInfoService {
	
	@Autowired
	private IMpInfoService mpInfoService;
	private Map<String, WxMpService> wxMpServiceMap;
	
	@PostConstruct
	public void init() {
		wxMpServiceMap = new HashMap<>(16);
		List<MpInfo> mpInfoList = mpInfoService.list();
		for(MpInfo mp : mpInfoList) {
			WxMpService wxMpService = new WxMpServiceImpl();
			WxMpInMemoryConfigStorage config = new WxMpInMemoryConfigStorage();
			config.setAppId(mp.getAppid());
			config.setSecret(mp.getSecret());
			config.setToken(mp.getToken());
			config.setAesKey(mp.getEncodingaeskey());
			wxMpService.setWxMpConfigStorage(config);
			wxMpServiceMap.put(mp.getAppid(), wxMpService);
			wxMpServiceMap.put(mp.getOriginal(), wxMpService);
		}
	}
	
	public WxMpService findWxMpService(HttpServletRequest request) throws Exception {
		String appid = request.getParameter("appid");
		if(StringUtils.isEmpty(appid)) {
			throw new EsRuntimeException(ErrorConstants.APPID_NOT_NULL);
		}
		return findWxMpService(appid);
	}
	
	public WxMpService findWxMpService(String key) throws Exception {
		if(StringUtils.isEmpty(key)) {
			throw new EsRuntimeException(ErrorConstants.APPID_NOT_NULL);
		}
		WxMpService wxMpService = wxMpServiceMap.get(key);
		if(wxMpService == null) {
			MpInfo info = findByKey(key);
			if(info != null) {
				wxMpService = new WxMpServiceImpl();
				WxMpInMemoryConfigStorage config = new WxMpInMemoryConfigStorage();
				config.setAppId(info.getAppid());
				config.setSecret(info.getSecret());
				config.setToken(info.getToken());
				config.setAesKey(info.getEncodingaeskey());
				wxMpService.setWxMpConfigStorage(config);
				wxMpServiceMap.put(info.getAppid(), wxMpService);
				wxMpServiceMap.put(info.getOriginal(), wxMpService);
			}
		}
		if(wxMpService == null) {
			throw new EsRuntimeException(ErrorConstants.WXMP_NOT_EXIST);
		}
		return wxMpService;
	}

	public Boolean create(MpInfo mpInfo) {
		//保存公众号信息
		Long now = DateUtil.genTime(new Date());
		mpInfo.setCreateTime(now);
		mpInfo.setUpdateTime(now);
		return mpInfoService.save(mpInfo);
	}

	private void ttm() {
		System.out.println("ttm");
	}

	public MpInfo find(Integer id) {
		ttm();
		return mpInfoService.getById(id);
	}
	
	public MpInfo findByKey(String key) {
		LambdaQueryWrapper<MpInfo> queryWrapper = Wrappers.lambdaQuery(new MpInfo()).eq(MpInfo::getAppid, key)
				.or().eq(MpInfo::getOriginal, key);
		return mpInfoService.getOne(queryWrapper);
	}
	
	public PageInfo<MpInfo> list(MpInfoExample example, int pageNo, int pageSize) {
		PageHelper.startPage(pageNo, pageSize);
		List<MpInfo> payOrderList = list(example);
		return new PageInfo<>(payOrderList);
	}

	public List<MpInfo> list(MpInfoExample example) {
		MpInfo mpInfo = new MpInfo();
		BeanUtil.copyPropertiesIgnoreNull(example, mpInfo);
		QueryWrapper<MpInfo> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().in(CollectionUtils.isNotEmpty(
				example.getClientIdList()), MpInfo::getClientId, example.getClientIdList());
		return mpInfoService.list(queryWrapper.allEq(BeanUtils.beanToMap(mpInfo), false));
	}

	public Boolean update(MpInfo mpInfo) {
		//保存公众号信息
		Long now = DateUtil.genTime(new Date());
		mpInfo.setUpdateTime(now);
		return mpInfoService.updateById(mpInfo);
	}

	public Boolean delete(Integer id) {
		return mpInfoService.removeById(id);
	}

	public List<MpInfo> selectAll() {
		return mpInfoService.list();
	}

}
