package cn.jia.user.service.impl;

import cn.jia.core.common.EsSecurityHandler;
import cn.jia.core.configuration.SpringContextHolder;
import cn.jia.core.exception.EsRuntimeException;
import cn.jia.core.util.DateUtil;
import cn.jia.core.util.ImgUtil;
import cn.jia.core.util.StringUtils;
import cn.jia.isp.entity.LdapUser;
import cn.jia.isp.service.LdapUserService;
import cn.jia.user.common.Constants;
import cn.jia.user.common.ErrorConstants;
import cn.jia.user.dao.GroupRelMapper;
import cn.jia.user.dao.OrgRelMapper;
import cn.jia.user.dao.RoleRelMapper;
import cn.jia.user.dao.UserMapper;
import cn.jia.user.entity.*;
import cn.jia.user.service.UserService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.base.Joiner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
	
	@Autowired
	private UserMapper userMapper;
	@Autowired
	private RoleRelMapper roleRelMapper;
	@Autowired
	private OrgRelMapper orgRelMapper;
	@Autowired
	private GroupRelMapper groupRelMapper;
	@Autowired
	private LdapUserService ldapUserService;

	@Override
	public User create(User user) throws Exception {
		//查找LDAP服务器是否有该用户
		LdapUser ldapUser = null;
		LdapUser params = new LdapUser();
		params.setTelephoneNumber(user.getPhone());
		params.setEmail(user.getEmail());
		params.setOpenid(user.getOpenid());

		List<LdapUser> ldapUserResult = ldapUserService.search(params);
		if(ldapUserResult.size() > 0) {
			ldapUser = ldapUserResult.get(0);
			user.setJiacn(String.valueOf(ldapUser.getUid()));
		}
		//将用户添加到ldap服务器
		if(ldapUser == null) {
			params = new LdapUser();
			String cn = StringUtils.isEmpty(user.getPhone()) ? (StringUtils.isEmpty(user.getEmail()) ? user.getOpenid() : user.getEmail()) : user.getPhone();
			params.setUid(cn);
			params.setCn(cn);
			params.setSn(cn);
			params.setTelephoneNumber(StringUtils.isEmpty(user.getPhone()) ? null : user.getPhone());
			params.setEmail(StringUtils.isEmpty(user.getEmail()) ? null : user.getEmail());
			params.setOpenid(StringUtils.isEmpty(user.getOpenid()) ? null : user.getOpenid());
			params.setCountry(StringUtils.isEmpty(user.getCountry()) ? null : user.getCountry());
			params.setProvince(StringUtils.isEmpty(user.getProvince()) ? null : user.getProvince());
			params.setCity(StringUtils.isEmpty(user.getCity()) ? null : user.getCity());
			params.setSex(user.getSex());
			params.setNickname(StringUtils.isEmpty(user.getNickname()) ? null : user.getNickname());
			if(user.getAvatar() != null) {
				String filePath = SpringContextHolder.getProperty("jia.file.path", String.class);
				params.setHeadimg(ImgUtil.fromFile(new File(filePath + "/" + user.getAvatar())));
			}
			ldapUserService.create(params);
			user.setJiacn(params.getUid());
		}
		//查找本地数据库是否有该用户，如果没有则新增，如果有则更新
		User searchUser = new User();
		searchUser.setJiacn(user.getJiacn());
		searchUser.setPhone(StringUtils.isEmpty(user.getPhone()) ? null : user.getPhone());
		searchUser.setEmail(StringUtils.isEmpty(user.getEmail()) ? null : user.getEmail());
		searchUser.setOpenid(StringUtils.isEmpty(user.getOpenid()) ? null : user.getOpenid());
		Page<User> curUser = this.search(searchUser, 1, 1);
		if(curUser != null && curUser.getResult().size() > 0) {
			throw new EsRuntimeException(ErrorConstants.USER_HAS_EXIST);
		}else {
			Long now = DateUtil.genTime(new Date());
			user.setCreateTime(now);
			user.setUpdateTime(now);
			userMapper.insertSelective(user);
			//设置默认角色
			RoleRel rel = new RoleRel();
			rel.setRoleId(Constants.DEFAULT_ROLE_ID);
			rel.setUserId(user.getId());
			rel.setCreateTime(now);
			rel.setUpdateTime(now);
			roleRelMapper.insertSelective(rel);
		}
		return user;
	}

	@Override
	public User find(Integer id) {
		return userMapper.selectByPrimaryKey(id);
	}

	@Override
	public User update(User user) {
		Long now = DateUtil.genTime(new Date());
		user.setUpdateTime(now);
		userMapper.updateByPrimaryKeySelective(user);
		return user;
	}

	@Override
	public void delete(Integer id) {
		userMapper.deleteByPrimaryKey(id);
	}

	@Override
	public Page<User> list(User example, int pageNo, int pageSize) {
		PageHelper.startPage(pageNo, pageSize);
		return userMapper.selectByExample(example);
	}

	/**
	 * 增加或减少用户积分
	 */
	@Override
	public void changePoint(String jiacn, int add) throws Exception{
		User user = userMapper.selectByJiacn(jiacn);
		if(user == null) {
			throw new EsRuntimeException(ErrorConstants.USER_NOT_EXIST);
		}
		User upUser = new User();
		upUser.setId(user.getId());
		upUser.setPoint(user.getPoint() + add);
		if(upUser.getPoint() < 0) {
			throw new EsRuntimeException(ErrorConstants.POINT_NO_ENOUGH);
		}
		upUser.setUpdateTime(DateUtil.genTime(new Date()));
		userMapper.updateByPrimaryKeySelective(upUser);
	}

	@Override
	public void changeRole(User user, String clientId) {
		//查询用户当前角色
		List<RoleRel> roleRelList = roleRelMapper.selectByUserId(user.getId(), clientId);
		List<RoleRel> addList = new ArrayList<>(); //需要添加的角色
		List<RoleRel> cancelList = new ArrayList<>(); //需要取消的角色
		//查找需要添加的角色
		user.getRoleIds().stream().filter(roleId -> roleRelList.stream().noneMatch(roleRel -> roleId.equals(roleRel.getRoleId()))).forEach(roleId -> {
			RoleRel rel = new RoleRel();
			rel.setUserId(user.getId());
			rel.setRoleId(roleId);
			Long now = DateUtil.genTime(new Date());
			rel.setClientId(clientId);
			rel.setCreateTime(now);
			rel.setUpdateTime(now);
			addList.add(rel);
		});
		if(addList.size() > 0) {
			roleRelMapper.batchAdd(addList);
		}
		
		//查找需要取消的角色
		roleRelList.stream().filter(roleRel -> !user.getRoleIds().contains(roleRel.getRoleId())).forEach(cancelList::add);
		if(cancelList.size() > 0) {
			roleRelMapper.batchDel(cancelList);
		}
	}

	@Override
	public void changeGroup(User user) {
		//查询用户当前角色
		List<GroupRel> groupRelList = groupRelMapper.selectByUserId(user.getId());
		List<GroupRel> addList = new ArrayList<>(); //需要添加的角色
		List<GroupRel> cancelList = new ArrayList<>(); //需要取消的角色
		//查找需要添加的角色
		user.getGroupIds().stream().filter(groupId -> groupRelList.stream().noneMatch(groupRel -> groupId.equals(groupRel.getGroupId()))).forEach(groupId -> {
			GroupRel rel = new GroupRel();
			rel.setUserId(user.getId());
			rel.setGroupId(groupId);
			Long now = DateUtil.genTime(new Date());
			rel.setCreateTime(now);
			rel.setUpdateTime(now);
			addList.add(rel);
		});
		if(addList.size() > 0) {
			groupRelMapper.batchAdd(addList);
		}

		//查找需要取消的角色
		groupRelList.stream().filter(groupRel -> !user.getGroupIds().contains(groupRel.getGroupId())).forEach(cancelList::add);
		if(cancelList.size() > 0) {
			groupRelMapper.batchDel(cancelList);
		}
	}
	
	@Override
	public void changeOrg(User user) {
		//查询用户当前组织
		List<OrgRel> orgRelList = orgRelMapper.selectByUserId(user.getId());
		List<OrgRel> addList = new ArrayList<>(); //需要添加的组织
		List<OrgRel> cancelList = new ArrayList<>(); //需要取消的组织
		//查找需要添加的组织
		user.getOrgIds().stream().filter(orgId -> orgRelList.stream().noneMatch(orgRel -> orgId.equals(orgRel.getOrgId()))).forEach(orgId -> {
			OrgRel rel = new OrgRel();
			rel.setUserId(user.getId());
			rel.setOrgId(orgId);
			Long now = DateUtil.genTime(new Date());
			rel.setCreateTime(now);
			rel.setUpdateTime(now);
			addList.add(rel);
		});
		if(addList.size() > 0) {
			orgRelMapper.batchAdd(addList);
		}
		
		//查找需要取消的组织
		orgRelList.stream().filter(orgRel -> !user.getOrgIds().contains(orgRel.getOrgId())).forEach(cancelList::add);
		if(cancelList.size() > 0) {
			orgRelMapper.batchDel(cancelList);
		}
	}

	@Override
	public Page<User> listByRoleId(Integer roleId, int pageNo, int pageSize) {
		PageHelper.startPage(pageNo, pageSize);
		return userMapper.selectByRole(roleId);
	}
	
	@Override
	public Page<User> listByGroupId(Integer groupId, int pageNo, int pageSize) {
		PageHelper.startPage(pageNo, pageSize);
		return userMapper.selectByGroup(groupId);
	}

	@Override
	public Page<User> listByOrgId(UserExample example, int pageNo, int pageSize) {
		PageHelper.startPage(pageNo, pageSize);
		return userMapper.selectByOrg(example);
	}

	@Override
	public User findByJiacn(String jiacn) {
		return userMapper.selectByJiacn(jiacn);
	}

	@Override
	public User findByOpenid(String openid) {
		return userMapper.selectByOpenid(openid);
	}

	@Override
	public User findByPhone(String phone) {
		return userMapper.selectByPhone(phone);
	}

	@Override
	public Page<User> search(User user, int pageNo, int pageSize) {
		PageHelper.startPage(pageNo, pageSize);
		return userMapper.searchByExample(user);
	}

	@Override
	public void sync(List<User> userList) throws Exception {
		for(User user : userList) {
			//查找LDAP服务器是否有该用户
			LdapUser ldapUser = null;
			LdapUser params = new LdapUser();
			params.setTelephoneNumber(user.getPhone());
			params.setEmail(user.getEmail());
			params.setOpenid(user.getOpenid());

			List<LdapUser> ldapUserResult = ldapUserService.search(params);
			if(ldapUserResult.size() > 0) {
				ldapUser = ldapUserResult.get(0);
				ldapUser.setTelephoneNumber(StringUtils.isEmpty(user.getPhone()) ? ldapUser.getTelephoneNumber() : user.getPhone());
				ldapUser.setEmail(StringUtils.isEmpty(user.getEmail()) ? ldapUser.getEmail() : user.getEmail());
				ldapUser.setOpenid(StringUtils.isEmpty(user.getOpenid()) ? ldapUser.getOpenid() : user.getOpenid());
				ldapUser.setCountry(StringUtils.isEmpty(user.getCountry()) ? ldapUser.getCountry() : user.getCountry());
				ldapUser.setProvince(StringUtils.isEmpty(user.getProvince()) ? ldapUser.getProvince() : user.getProvince());
				ldapUser.setCity(StringUtils.isEmpty(user.getCity()) ? ldapUser.getCity() : user.getCity());
				ldapUser.setSex(user.getSex() == null ? ldapUser.getSex() : user.getSex());
				ldapUser.setNickname(StringUtils.isEmpty(user.getNickname()) ? ldapUser.getNickname() : user.getNickname());
				if(StringUtils.isNotEmpty(user.getAvatar())) {
					String filePath = SpringContextHolder.getProperty("jia.file.path", String.class);
					ldapUser.setHeadimg(ImgUtil.fromFile(new File(filePath + "/" + user.getAvatar())));
				}
				ldapUserService.modifyLdapUser(ldapUser);
				user.setJiacn(String.valueOf(ldapUser.getUid()));
			}
			//将用户添加到ldap服务器
			if(ldapUser == null) {
				params = new LdapUser();
				String cn = StringUtils.isEmpty(user.getPhone()) ? (StringUtils.isEmpty(user.getEmail()) ? user.getOpenid() : user.getEmail()) : user.getPhone();
				params.setCn(cn);
				params.setSn(cn);
				params.setTelephoneNumber(StringUtils.isEmpty(user.getPhone()) ? null : user.getPhone());
				params.setEmail(StringUtils.isEmpty(user.getEmail()) ? null : user.getEmail());
				params.setOpenid(StringUtils.isEmpty(user.getOpenid()) ? null : user.getOpenid());
				params.setCountry(StringUtils.isEmpty(user.getCountry()) ? null : user.getCountry());
				params.setProvince(StringUtils.isEmpty(user.getProvince()) ? null : user.getProvince());
				params.setCity(StringUtils.isEmpty(user.getCity()) ? null : user.getCity());
				params.setSex(user.getSex());
				params.setNickname(StringUtils.isEmpty(user.getNickname()) ? null : user.getNickname());
				if(user.getAvatar() != null) {
					String filePath = SpringContextHolder.getProperty("jia.file.path", String.class);
					params.setHeadimg(ImgUtil.fromFile(new File(filePath + "/" + user.getAvatar())));
				}
				ldapUserService.create(params);
				user.setJiacn(params.getUid());
			}

			Long now = DateUtil.genTime(new Date());
			//查找本地数据库是否有该用户，如果没有则新增，如果有则更新
			User searchUser = new User();
			searchUser.setJiacn(user.getJiacn());
			searchUser.setPhone(user.getPhone());
			searchUser.setEmail(user.getEmail());
			searchUser.setOpenid(user.getOpenid());
			Page<User> curUser = this.search(searchUser, 1, 1);
			if(curUser == null || curUser.getResult().size() == 0) {
				user.setCreateTime(now);
				user.setUpdateTime(now);
				//新增用户
				userMapper.insertSelective(user);
				//设置默认角色
				RoleRel rel = new RoleRel();
				rel.setRoleId(Constants.DEFAULT_ROLE_ID);
				rel.setUserId(user.getId());
				rel.setCreateTime(now);
				rel.setUpdateTime(now);
				roleRelMapper.insertSelective(rel);
			}else {
				User cu = curUser.getResult().get(0);
				user.setId(cu.getId());
				List<String> subscribe = new ArrayList<>(Arrays.asList(cu.getSubscribe().split(",")));
				if(!subscribe.contains(user.getSubscribe())) {
					subscribe.add(user.getSubscribe());
					user.setSubscribe(Joiner.on(",").join(subscribe));
				}
				user.setUpdateTime(now);
				userMapper.updateByPrimaryKeySelective(user);
			}
		}
	}

	@Override
	public List<Integer> findRoleIds(Integer userId) {
		List<RoleRel> roleRelList = roleRelMapper.selectByUserId(userId, EsSecurityHandler.clientId());
		List<Integer> roleIds = new ArrayList<>();
		for(RoleRel rel : roleRelList) {
			roleIds.add(rel.getRoleId());
		}
		return roleIds;
	}

	@Override
	public List<Integer> findOrgIds(Integer userId) {
		List<OrgRel> orgRelList = orgRelMapper.selectByUserId(userId);
		List<Integer> orgIds = new ArrayList<>();
		for(OrgRel rel : orgRelList) {
			orgIds.add(rel.getOrgId());
		}
		return orgIds;
	}

	@Override
	public List<Integer> findGroupIds(Integer userId) {
		List<GroupRel> groupRelList = groupRelMapper.selectByUserId(userId);
		List<Integer> groupIds = new ArrayList<>();
		for(GroupRel rel : groupRelList) {
			groupIds.add(rel.getGroupId());
		}
		return groupIds;
	}

	@Override
	public User findByUsername(String username) {
		return userMapper.selectByUsername(username);
	}

	@Override
	public void changePassword(Integer userId, String oldPassword, String newPassword) throws Exception {
		User user = userMapper.selectByPrimaryKey(userId);
		if(user == null) {
			throw new EsRuntimeException(ErrorConstants.USER_NOT_EXIST);
		}
		if(!user.getPassword().equals(oldPassword)) {
			throw new EsRuntimeException(ErrorConstants.OLD_PASSWORD_WRONG);
		}
		User upUser = new User();
		upUser.setId(userId);
		upUser.setPassword(newPassword);
		userMapper.updateByPrimaryKeySelective(upUser);
	}

	@Override
	public void resetPassword(String phone, String newPassword) throws Exception {
		User user = userMapper.selectByPhone(phone);
		if(user == null) {
			throw new EsRuntimeException(ErrorConstants.USER_NOT_EXIST);
		}

		User upUser = new User();
		upUser.setId(user.getId());
		upUser.setPassword(newPassword);
		userMapper.updateByPrimaryKeySelective(upUser);
	}

	@Override
	public void setDefaultOrg(Integer userId) {
		User user = userMapper.selectByPrimaryKey(userId);
		List<OrgRel> orgRelList = orgRelMapper.selectByUserId(userId);
		if(orgRelList.size() > 0) {
			User upUser = new User();
			upUser.setId(user.getId());
			//如果用户还没有设置默认职位，则默认第一个职位
			if(user.getPosition() == null) {
				upUser.setPosition(orgRelList.get(0).getOrgId());
				userMapper.updateByPrimaryKeySelective(upUser);
			} else { //如果用户当前职位已失效，则默认第一个职位
				boolean aval = false;
				for(OrgRel rel : orgRelList) {
					if(user.getPosition().equals(rel.getOrgId())) {
						aval = true;
						break;
					}
				}
				if(!aval) {
					upUser.setPosition(orgRelList.get(0).getOrgId());
					userMapper.updateByPrimaryKeySelective(upUser);
				}
			}
		}
	}
}
