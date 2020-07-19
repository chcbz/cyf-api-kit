package cn.jia.wx.service;

import cn.jia.wx.entity.MpUser;
import com.github.pagehelper.Page;

import java.util.List;

public interface MpUserService {
	
	MpUser create(MpUser mpUser) throws Exception;

	MpUser find(Integer id) throws Exception;

	MpUser findByOpenId(String openId);

	MpUser findByJiacn(String jiacn);
	
	Page<MpUser> list(MpUser example, int pageNo, int pageSize);

	MpUser update(MpUser mpUser);

	void delete(Integer id);

	void sync(List<MpUser> userList) throws Exception;

	void unsubstribe(MpUser example);
}
