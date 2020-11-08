package cn.jia.user.api;

import cn.jia.core.common.EsSecurityHandler;
import cn.jia.core.entity.Action;
import cn.jia.core.entity.JSONRequestPage;
import cn.jia.core.entity.JSONResult;
import cn.jia.core.entity.JSONResultPage;
import cn.jia.core.util.JSONUtil;
import cn.jia.user.entity.Role;
import cn.jia.user.entity.User;
import cn.jia.user.service.RoleService;
import cn.jia.user.service.UserService;
import com.github.pagehelper.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/role")
public class RoleController {
	
	@Autowired
	private RoleService roleService;
	@Autowired
	private UserService userService;
	
	/**
	 * 获取角色信息
	 * @param id
	 * @return
	 */
	@PreAuthorize("hasAuthority('role-get')")
	@RequestMapping(value = "/get", method = RequestMethod.GET)
	public Object findById(@RequestParam(name = "id") Integer id) {
		Role role = roleService.find(id);
		return JSONResult.success(role);
	}
	
	/**
	 * 获取当前角色下的所有用户
	 * @param page
	 * @return
	 */
	@PreAuthorize("hasAuthority('role-get_users')")
	@RequestMapping(value = "/get/users", method = RequestMethod.POST)
	public Object findUsers(@RequestBody JSONRequestPage<String> page) {
		Role role = JSONUtil.fromJson(page.getSearch(), Role.class);
		Page<User> userList = userService.listByRoleId(role.getId(), page.getPageNum(), page.getPageSize());
		JSONResultPage<User> result = new JSONResultPage<>(userList.getResult());
		result.setPageNum(userList.getPageNum());
		result.setTotal(userList.getTotal());
		return result;
	}

	/**
	 * 获取当前角色下的所有权限
	 * @param page
	 * @return
	 */
	@PreAuthorize("hasAuthority('role-get_perms')")
	@RequestMapping(value = "/get/perms", method = RequestMethod.POST)
	public Object findPerms(@RequestBody JSONRequestPage<String> page) {
		Role role = JSONUtil.fromJson(page.getSearch(), Role.class);
		Page<Action> permsList = roleService.listPerms(role.getId(), page.getPageNum(), page.getPageSize());
		JSONResultPage<Action> result = new JSONResultPage<>(permsList.getResult());
		result.setPageNum(permsList.getPageNum());
		result.setTotal(permsList.getTotal());
		return result;
	}

	/**
	 * 创建角色
	 * @param role
	 * @return
	 */
	@PreAuthorize("hasAuthority('role-create')")
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public Object create(@RequestBody Role role) {
		roleService.create(role);
		return JSONResult.success();
	}

	/**
	 * 更新角色信息
	 * @param role
	 * @return
	 */
	@PreAuthorize("hasAuthority('role-update')")
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public Object update(@RequestBody Role role) {
		roleService.update(role);
		return JSONResult.success();
	}

	/**
	 * 删除角色
	 * @param id
	 * @return
	 */
	@PreAuthorize("hasAuthority('role-delete')")
	@RequestMapping(value = "/delete", method = RequestMethod.GET)
	public Object delete(@RequestParam(name = "id") Integer id) {
		roleService.delete(id);
		return JSONResult.success();
	}
	
	/**
	 * 获取所有角色信息
	 * @return
	 */
	@PreAuthorize("hasAuthority('role-list')")
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	public Object list(@RequestBody JSONRequestPage<String> page) {
		Role example = JSONUtil.fromJson(page.getSearch(), Role.class);
		Page<Role> roleList = roleService.list(example, page.getPageNum(), page.getPageSize());
		JSONResultPage<Role> result = new JSONResultPage<>(roleList.getResult());
		result.setPageNum(roleList.getPageNum());
		result.setTotal(roleList.getTotal());
		return result;
	}
	
	/**
	 * 全量更新角色权限
	 * @param role
	 * @return
	 */
	@PreAuthorize("hasAuthority('role-perms_change')")
	@RequestMapping(value = "/perms/change", method = RequestMethod.POST)
	public Object changePerms(@RequestBody Role role) {
		roleService.changePerms(role);
		return JSONResult.success();
	}
	
	/**
	 * 批量添加角色用户或组
	 * @param role
	 * @return
	 */
	@PreAuthorize("hasAuthority('role-users_add')")
	@RequestMapping(value = "/users/add", method = RequestMethod.POST)
	public Object userBatchAdd(@RequestBody Role role) {
		role.setClientId(EsSecurityHandler.clientId());
		roleService.batchAddUser(role);
		return JSONResult.success();
	}
	
	/**
	 * 批量删除角色用户或组
	 * @param role
	 * @return
	 */
	@PreAuthorize("hasAuthority('role-users_del')")
	@RequestMapping(value = "/users/del", method = RequestMethod.POST)
	public Object userBatchDel(@RequestBody Role role) {
		role.setClientId(EsSecurityHandler.clientId());
		roleService.batchDelUser(role);
		return JSONResult.success();
	}
}
