package cn.jia.wx.service.impl;

import cn.jia.core.common.EsConstants;
import cn.jia.core.common.EsSecurityHandler;
import cn.jia.core.configuration.SpringContextHolder;
import cn.jia.core.util.*;
import cn.jia.isp.entity.IspFile;
import cn.jia.isp.entity.LdapUser;
import cn.jia.isp.service.FileService;
import cn.jia.isp.service.LdapUserService;
import cn.jia.user.common.Constants;
import cn.jia.user.dao.RoleRelMapper;
import cn.jia.user.dao.UserMapper;
import cn.jia.user.entity.RoleRel;
import cn.jia.user.entity.User;
import cn.jia.wx.dao.MpUserMapper;
import cn.jia.wx.entity.MpUser;
import cn.jia.wx.service.MpUserService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;

@Service
public class MpUserServiceImpl implements MpUserService {
	
	@Autowired
	private MpUserMapper mpUserMapper;
	@Autowired
	private LdapUserService ldapUserService;
	@Autowired
	private UserMapper userMapper;
	@Autowired
	private FileService fileService;
	@Autowired
	private RoleRelMapper roleRelMapper;

	@Override
	public MpUser create(MpUser user) throws Exception {
		long now = DateUtil.genTime(new Date());
		// 将用户添加到ldap服务器
		LdapUser params = new LdapUser();
		params.setCn(user.getOpenId());
		params.setSn(user.getOpenId());
		params.setEmail(StringUtils.isEmpty(user.getEmail()) ? null : user.getEmail());
		params.setOpenid(user.getOpenId());
		params.setCountry(StringUtils.isEmpty(user.getCountry()) ? null : user.getCountry());
		params.setProvince(StringUtils.isEmpty(user.getProvince()) ? null : user.getProvince());
		params.setCity(StringUtils.isEmpty(user.getCity()) ? null : user.getCity());
		params.setSex(user.getSex());
		params.setNickname(StringUtils.isEmpty(user.getNickname()) ? null : user.getNickname());
		if(StringUtils.isNotEmpty(user.getHeadImgUrl())) {
			params.setHeadimg(ImgUtil.fromURL(user.getHeadImgUrl()));
		}
		ldapUserService.create(params);
		// 保存系统用户
		User u = new User();
		BeanUtil.copyPropertiesIgnoreNull(user, u);
		u.setJiacn(params.getUid());
		u.setOpenid(user.getOpenId());
		if (StringUtils.isNotEmpty(user.getHeadImgUrl())) {
			String filename = DateUtil.getDateString() + "_" + user.getOpenId() + ".jpg";
			String filePath = SpringContextHolder.getProperty("jia.file.path", String.class);
			File pathFile = new File(filePath + "/avatar");
			//noinspection ResultOfMethodCallIgnored
			pathFile.mkdirs();
			FileOutputStream fos = new FileOutputStream(filePath + "/avatar/" + filename);
			byte[] b = ImgUtil.fromURL(user.getHeadImgUrl());
			IOUtils.write(b, fos);
			fos.close();

			//保存文件信息
			IspFile cf = new IspFile();
			cf.setClientId(EsSecurityHandler.clientId());
			cf.setExtension(FileUtil.getExtension(filename));
			cf.setName(user.getOpenId() + ".jpg");
			cf.setSize((long) b.length);
			cf.setType(EsConstants.FILE_TYPE_AVATAR);
			cf.setUri("avatar/" + filename);
			fileService.create(cf);

			u.setAvatar("avatar/" + filename);
		}
		userMapper.insertSelective(u);
		//设置默认角色
		RoleRel rel = new RoleRel();
		rel.setRoleId(Constants.DEFAULT_ROLE_ID);
		rel.setUserId(user.getId());
		rel.setCreateTime(now);
		rel.setUpdateTime(now);
		roleRelMapper.insertSelective(rel);
		// 保存微信用户
		user.setJiacn(params.getUid());
		user.setCreateTime(now);
		user.setUpdateTime(now);
		user.setStatus(Constants.COMMON_ENABLE);
		mpUserMapper.insertSelective(user);
		return user;
	}

	@Override
	public MpUser find(Integer id) {
		return mpUserMapper.selectByPrimaryKey(id);
	}

	@Override
	public MpUser findByOpenId(String openId) {
		return mpUserMapper.selectByOpenId(openId);
	}

	@Override
	public MpUser findByJiacn(String jiacn) {
		return mpUserMapper.selectByJiacn(jiacn);
	}

	@Override
	public Page<MpUser> list(MpUser example, int pageNo, int pageSize) {
		PageHelper.startPage(pageNo, pageSize);
		return mpUserMapper.selectByExample(example);
	}

	@Override
	public MpUser update(MpUser mpUser) {
		//保存公众号信息
		Long now = DateUtil.genTime(new Date());
		mpUser.setUpdateTime(now);
		mpUserMapper.updateByPrimaryKeySelective(mpUser);
		return mpUser;
	}

	@Override
	public void delete(Integer id) {
		mpUserMapper.deleteByPrimaryKey(id);
	}

	@Async
	@Override
	public void sync(List<MpUser> userList) throws Exception {
		long now = DateUtil.genTime(new Date());
		for(MpUser user : userList) {
			// 保存公众号用户
			MpUser mpUser = mpUserMapper.selectByOpenId(user.getOpenId());
			if (mpUser == null) {
				// 将用户添加到ldap服务器
				LdapUser params = new LdapUser();
				params.setCn(user.getOpenId());
				params.setSn(user.getOpenId());
				params.setEmail(StringUtils.isEmpty(user.getEmail()) ? null : user.getEmail());
				params.setOpenid(user.getOpenId());
				params.setCountry(StringUtils.isEmpty(user.getCountry()) ? null : user.getCountry());
				params.setProvince(StringUtils.isEmpty(user.getProvince()) ? null : user.getProvince());
				params.setCity(StringUtils.isEmpty(user.getCity()) ? null : user.getCity());
				params.setSex(user.getSex());
				params.setNickname(StringUtils.isEmpty(user.getNickname()) ? null : user.getNickname());
				if(StringUtils.isNotEmpty(user.getHeadImgUrl())) {
					params.setHeadimg(ImgUtil.fromURL(user.getHeadImgUrl()));
				}
				ldapUserService.create(params);
				// 保存系统用户
				User u = new User();
				BeanUtil.copyPropertiesIgnoreNull(user, u);
				u.setJiacn(params.getUid());
				u.setOpenid(user.getOpenId());
				if (StringUtils.isNotEmpty(user.getHeadImgUrl())) {
					String filename = DateUtil.getDateString() + "_" + user.getOpenId() + ".jpg";
					String filePath = SpringContextHolder.getProperty("jia.file.path", String.class);
					File pathFile = new File(filePath + "/avatar");
					//noinspection ResultOfMethodCallIgnored
					pathFile.mkdirs();
					FileOutputStream fos = new FileOutputStream(filePath + "/avatar/" + filename);
					byte[] b = ImgUtil.fromURL(user.getHeadImgUrl());
					IOUtils.write(b, fos);
					fos.close();

					//保存文件信息
					IspFile cf = new IspFile();
					cf.setClientId(EsSecurityHandler.clientId());
					cf.setExtension(FileUtil.getExtension(filename));
					cf.setName(user.getOpenId() + ".jpg");
					cf.setSize((long) b.length);
					cf.setType(EsConstants.FILE_TYPE_AVATAR);
					cf.setUri("avatar/" + filename);
					fileService.create(cf);

					u.setAvatar("avatar/" + filename);
				}
				userMapper.insertSelective(u);
				//设置默认角色
				RoleRel rel = new RoleRel();
				rel.setRoleId(Constants.DEFAULT_ROLE_ID);
				rel.setUserId(user.getId());
				rel.setCreateTime(now);
				rel.setUpdateTime(now);
				roleRelMapper.insertSelective(rel);
				// 保存微信用户
				user.setJiacn(params.getUid());
				user.setCreateTime(now);
				user.setUpdateTime(now);
				user.setStatus(Constants.COMMON_ENABLE);
				mpUserMapper.insertSelective(user);
			} else {
				BeanUtil.copyPropertiesIgnoreEmpty(user, mpUser);
				mpUserMapper.updateByPrimaryKeySelective(mpUser);
			}
		}
	}

	@Override
	public void unsubstribe(MpUser example) {
		mpUserMapper.unsubscribeByExample(example);
	}
}
