package cn.jia.task.api;

import cn.jia.core.entity.JSONRequestPage;
import cn.jia.core.entity.JSONResult;
import cn.jia.core.entity.JSONResultPage;
import cn.jia.core.util.JSONUtil;
import cn.jia.task.entity.TaskItemVO;
import cn.jia.task.entity.TaskItemVOExample;
import cn.jia.task.entity.TaskPlan;
import cn.jia.task.entity.TaskPlanExample;
import cn.jia.task.service.TaskService;
import com.github.pagehelper.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/task")
public class TaskController {
	
	@Autowired
	private TaskService taskService;
	
	/**
	 * 获取任务信息
	 * @param id
	 * @return
	 */
	/*@PreAuthorize("hasAuthority('task-get')")*/
	@RequestMapping(value = "/get", method = RequestMethod.GET)
	public Object findById(@RequestParam(name = "id") Integer id) throws Exception {
		TaskPlan task = taskService.find(id);
		return JSONResult.success(task);
	}

	/**
	 * 创建任务
	 * @param task
	 * @return
	 */
	/*@PreAuthorize("hasAuthority('task-create')")*/
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public Object create(@RequestBody TaskPlan task) {
		taskService.create(task);
		return JSONResult.success();
	}

	/**
	 * 更新任务信息
	 * @param task
	 * @return
	 */
	/*@PreAuthorize("hasAuthority('task-update')")*/
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public Object update(@RequestBody TaskPlan task) {
		taskService.update(task);
		return JSONResult.success();
	}

	/**
	 * 删除任务
	 * @param id
	 * @return
	 */
	/*@PreAuthorize("hasAuthority('task-delete')")*/
	@RequestMapping(value = "/delete", method = RequestMethod.GET)
	public Object delete(@RequestParam(name = "id") Integer id) {
		taskService.delete(id);
		return JSONResult.success();
	}
	
	/**
	 * 取消任务
	 * @param id
	 * @return
	 */
	/*@PreAuthorize("hasAuthority('task-cancel')")*/
	@RequestMapping(value = "/cancel", method = RequestMethod.GET)
	public Object cancel(@RequestParam(name = "id") Integer id) {
		taskService.cancel(id);
		return JSONResult.success();
	}
	
	/**
	 * 获取所有任务信息
	 * @return
	 */
	/*@PreAuthorize("hasAuthority('task-search')")*/
	@RequestMapping(value = "/search", method = RequestMethod.POST)
	public Object search(@RequestBody JSONRequestPage<String> page) {
		TaskPlanExample plan = JSONUtil.fromJson(page.getSearch(), TaskPlanExample.class);
		Page<TaskPlan> taskList = taskService.search(plan, page.getPageNum(), page.getPageSize());
		@SuppressWarnings({ "unchecked", "rawtypes" })
		JSONResultPage<Object> result = new JSONResultPage(taskList.getResult());
		result.setPageNum(taskList.getPageNum());
		result.setTotal(taskList.getTotal());
		return result;
	}
	
	/**
	 * 获取所有任务信息
	 * @return
	 */
	@RequestMapping(value = "/item/search", method = RequestMethod.POST)
	public Object searchItem(@RequestBody JSONRequestPage<String> page) {
		TaskItemVOExample plan = JSONUtil.fromJson(page.getSearch(), TaskItemVOExample.class);
		Page<TaskItemVO> taskList = taskService.findItems(plan, page.getPageNum(), page.getPageSize());
		@SuppressWarnings({ "unchecked", "rawtypes" })
		JSONResultPage<Object> result = new JSONResultPage(taskList.getResult());
		result.setPageNum(taskList.getPageNum());
		result.setTotal(taskList.getTotal());
		return result;
	}
}
