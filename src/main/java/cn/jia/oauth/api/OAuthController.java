package cn.jia.oauth.api;

import cn.jia.core.common.EsSecurityHandler;
import cn.jia.core.entity.JSONRequestPage;
import cn.jia.core.entity.JSONResult;
import cn.jia.core.entity.JSONResultPage;
import cn.jia.core.exception.EsRuntimeException;
import cn.jia.core.util.JSONUtil;
import cn.jia.core.util.StringUtils;
import cn.jia.oauth.entity.Client;
import cn.jia.oauth.entity.Resource;
import cn.jia.oauth.service.ClientService;
import cn.jia.oauth.service.ResourceService;
import cn.jia.user.common.UserErrorConstants;
import cn.jia.user.entity.User;
import cn.jia.user.service.UserService;
import com.github.pagehelper.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/oauth")
public class OAuthController {
	
	@Autowired
	private ClientService clientService;
	@Autowired
	private ResourceService resourceService;
	@Autowired
	private UserService userService;

	/**
	 * 根据应用标识码获取客户ID
	 * @param appcn
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/clientid", method = RequestMethod.GET)
	public Object findClientId(@RequestParam(name = "appcn") String appcn) throws Exception {
		Client client = clientService.findByAppcn(appcn);
		if(client == null) {
			throw new EsRuntimeException(UserErrorConstants.DATA_NOT_FOUND);
		}
		return JSONResult.success(client.getClientId());
	}
	
	/**
	 * 获取客户端信息
	 * @return
	 */
	@RequestMapping(value = "/client/get", method = RequestMethod.GET)
	public Object find() {
		Client client = clientService.find(EsSecurityHandler.clientId());
		return JSONResult.success(client);
	}
	
	/**
	 * 更新客户端信息
	 * @param client
	 * @return
	 */
	@RequestMapping(value = "/client/update", method = RequestMethod.POST)
	public Object updateClient(@RequestBody Client client) {
		client.setClientId(EsSecurityHandler.clientId());
		clientService.update(client);
		return JSONResult.success(client);
	}

	/**
	 * 添加客户端资源
	 * @param resourceId
	 * @return
	 */
	@RequestMapping(value = "/client/addresource", method = RequestMethod.GET)
	public Object addResource(@RequestParam(name = "resourceId") String resourceId) {
		clientService.addResource(resourceId, EsSecurityHandler.clientId());
		return JSONResult.success();
	}
	
	/**
	 * 获取资源
	 * @param resourceId
	 * @return
	 */
	@PreAuthorize("hasAuthority('oauth-resource_get')")
	@RequestMapping(value = "/resource/get", method = RequestMethod.GET)
	public Object findResourceById(@RequestParam String resourceId) throws Exception {
		Resource resource = resourceService.find(resourceId);
		return JSONResult.success(resource);
	}

	/**
	 * 创建资源
	 * @param resource
	 * @return
	 */
	@PreAuthorize("hasAuthority('oauth-resource_create')")
	@RequestMapping(value = "/resource/create", method = RequestMethod.POST)
	public Object createResource(@RequestBody Resource resource) {
		resourceService.create(resource);
		return JSONResult.success();
	}

	/**
	 * 更新资源信息
	 * @param resource
	 * @return
	 */
	@PreAuthorize("hasAuthority('oauth-resource_update')")
	@RequestMapping(value = "/resource/update", method = RequestMethod.POST)
	public Object updateResource(@RequestBody Resource resource) {
		resourceService.update(resource);
		return JSONResult.success();
	}

	/**
	 * 删除资源
	 * @param resourceId
	 * @return
	 */
	@PreAuthorize("hasAuthority('oauth-resource_delete')")
	@RequestMapping(value = "/resource/delete", method = RequestMethod.GET)
	public Object delete(@RequestParam String resourceId) {
		resourceService.delete(resourceId);
		return JSONResult.success();
	}

	/**
	 * 获取所有资源
	 * @return
	 */
	@PreAuthorize("hasAuthority('oauth-resource_list')")
	@RequestMapping(value = "/resource/list", method = RequestMethod.POST)
	public Object list(@RequestBody JSONRequestPage<String> page) {
		Resource example = JSONUtil.fromJson(page.getSearch(), Resource.class);
		Page<Resource> resourceList = resourceService.list(example, page.getPageNum(), page.getPageSize());
		JSONResultPage<Resource> result = new JSONResultPage<>(resourceList.getResult());
		result.setPageNum(resourceList.getPageNum());
		result.setTotal(resourceList.getTotal());
		return result;
	}

	/**
	 * 获取用户OAUTH2权限信息
	 * @param user 用户信息
	 * @return 用户信息
	 */
	@RequestMapping(value = "/user", method = RequestMethod.GET)
	public Principal info(Principal user){
		return user;
	}

	@RequestMapping(value = "/userinfo", method = RequestMethod.GET)
	public Object userInfo() throws Exception {
		String username = EsSecurityHandler.username();
		if(StringUtils.isEmpty(username)) {
			throw new EsRuntimeException(UserErrorConstants.USER_NOT_EXIST);
		}
		User user;
		if(username.startsWith("wx-")) {
			user = userService.findByOpenid(username.substring(3));
		} else if (username.startsWith("mb-")) {
			user = userService.findByPhone(username.substring(3));
		} else {
			user = userService.findByUsername(username);
		}
		if(user == null) {
			throw new EsRuntimeException(UserErrorConstants.USER_NOT_EXIST);
		}
		user.setPassword("***");
		return JSONResult.success(user);
	}
}
