package cn.jia.task.service;

import cn.jia.task.entity.TaskItemVO;
import cn.jia.task.entity.TaskItemVOExample;
import cn.jia.task.entity.TaskPlan;
import cn.jia.task.entity.TaskPlanExample;
import com.github.pagehelper.Page;

public interface TaskService {
	
	TaskPlan create(TaskPlan task);

	TaskPlan find(Integer id) throws Exception;

	TaskPlan update(TaskPlan task);

	void delete(Integer id);
	
	void cancel(Integer id);
	
	Page<TaskPlan> search(TaskPlanExample plan, int pageNo, int pageSize);
	
	Page<TaskItemVO> findItems(TaskItemVOExample example, int pageNo, int pageSize);
}
