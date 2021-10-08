package cn.jia.task.service.impl;

import cn.jia.core.util.DateUtil;
import cn.jia.core.util.LunarUtil;
import cn.jia.task.common.TaskConstants;
import cn.jia.task.dao.TaskItemMapper;
import cn.jia.task.dao.TaskItemVOMapper;
import cn.jia.task.dao.TaskPlanMapper;
import cn.jia.task.entity.*;
import cn.jia.task.service.TaskService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

/**
 * @author chc
 */
@Service
public class TaskServiceImpl implements TaskService {
	
	@Autowired
	private TaskPlanMapper taskPlanMapper;
	@Autowired
	private TaskItemMapper taskItemMapper;
	@Autowired
	private TaskItemVOMapper taskItemVoMapper;

	@Override
	public TaskPlan create(TaskPlan task) {
		//保存任务信息
		Long now = DateUtil.genTime(new Date());
		task.setCreateTime(now);
		task.setUpdateTime(now);
		taskPlanMapper.insertSelective(task);
		//保存任务执行明细
		if(TaskConstants.TASK_PERIOD_ALLTIME.equals(task.getType())) {
			TaskItem item = new TaskItem();
			item.setPlanId(task.getId());
			taskItemMapper.insertSelective(item);
		} else if(TaskConstants.TASK_PERIOD_DATE.equals(task.getType())) {
			TaskItem item = new TaskItem();
			item.setPlanId(task.getId());
			item.setTime(task.getStartTime());
			taskItemMapper.insertSelective(item);
		} else {
			Long time = task.getStartTime();
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(time * 1000);
			LunarUtil.Lunar lunar = LunarUtil.solarToLunar(calendar.getTime());
			int month = lunar.lunarMonth;
			int day = lunar.lunarDay;
			do {
				Calendar c = Calendar.getInstance();
				c.setTime(new Date(time * 1000));
				if(TaskConstants.COMMON_YES.equals(task.getLunar())) {
					LunarUtil.Lunar l = LunarUtil.solarToLunar(c.getTime());
					int m = l.lunarMonth;
					int d = l.lunarDay;
					if((TaskConstants.TASK_PERIOD_YEAR.equals(task.getPeriod()) && month == m && day == d) ||
							(TaskConstants.TASK_PERIOD_MONTH.equals(task.getPeriod()) && day == d)) {
						//保存当前执行点
						TaskItem item = new TaskItem();
						item.setPlanId(task.getId());
						item.setTime(time);
						taskItemMapper.insertSelective(item);
					}
					//设置下一个执行点
					c.add(Calendar.DAY_OF_MONTH, 1);
					time = DateUtil.genTime(c.getTime());
				} else {
					//保存当前执行点
					TaskItem item = new TaskItem();
					item.setPlanId(task.getId());
					item.setTime(time);
					taskItemMapper.insertSelective(item);
					//设置下一个执行点
					//noinspection MagicConstant
					c.add(task.getPeriod(), 1);
					time = DateUtil.genTime(c.getTime());
				}
			} while (time <= task.getEndTime());
		}
		return task;
	}

	@Override
	public TaskPlan find(Integer id) {
		return taskPlanMapper.selectByPrimaryKey(id);
	}

	@Override
	public TaskPlan update(TaskPlan task) {
		//保存任务信息
		Long now = DateUtil.genTime(new Date());
		task.setUpdateTime(now);
		taskPlanMapper.updateByPrimaryKeySelective(task);
		//清空原有任务执行明细
		taskItemMapper.deleteByPlan(task.getId());
		//保存任务执行明细
		if(TaskConstants.TASK_PERIOD_ALLTIME.equals(task.getType())) {
			TaskItem item = new TaskItem();
			item.setPlanId(task.getId());
			taskItemMapper.insertSelective(item);
		} else if(TaskConstants.TASK_PERIOD_DATE.equals(task.getType())) {
			TaskItem item = new TaskItem();
			item.setPlanId(task.getId());
			item.setTime(task.getStartTime());
			taskItemMapper.insertSelective(item);
		} else {
			Long time = task.getStartTime();
			do {	
				//保存当前执行点
				TaskItem item = new TaskItem();
				item.setPlanId(task.getId());
				item.setTime(time);
				taskItemMapper.insertSelective(item);
				//设置下一个执行点
				Calendar c = Calendar.getInstance();
				c.setTime(new Date(time * 1000));
				//noinspection MagicConstant
				c.add(task.getPeriod(), 1);
				time = DateUtil.genTime(c.getTime());
			} while (time <= task.getEndTime());
		}
		return task;
	}

	@Override
	public void delete(Integer id) {
		taskPlanMapper.deleteByPrimaryKey(id);
	}

	@Override
	public Page<TaskPlan> search(TaskPlanExample plan, int pageNo, int pageSize) {
		PageHelper.startPage(pageNo, pageSize);
		return taskPlanMapper.selectByExample(plan);
	}

	@Override
	public Page<TaskItemVO> findItems(TaskItemVOExample example, int pageNo, int pageSize) {
		PageHelper.startPage(pageNo, pageSize);
		return taskItemVoMapper.selectByExample(example);
	}

	@Override
	public void cancel(Integer id) {
		//失效当前任务
		TaskPlan task = new TaskPlan();
		task.setId(id);
		task.setStatus(0);
		taskPlanMapper.updateByPrimaryKeySelective(task);
		//失效还没有执行的任务明细
		Long now = DateUtil.genTime(new Date());
		taskItemMapper.cancelByPlan(id, now);
	}

}
