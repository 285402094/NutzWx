package cn.xuetang.common.task;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;

import cn.xuetang.modules.sys.bean.Sys_task;
import cn.xuetang.service.sys.AppInfoService;
import cn.xuetang.service.sys.SysTaskService;

/**
 * Created by Wizzer on 14-3-31.
 */
@IocBean
public class LoadTask implements Runnable {
	@Inject
	protected SysTaskService sysTaskService;
	@Inject
	protected AppInfoService appInfoService;
	private final static Log log = Logs.get();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void run() {
		List<Sys_task> tasks = sysTaskService.list();
		try {
			log.info("tasks.size:" + tasks.size());
			appInfoService.getSCHEDULER().setJobFactory(new NutQuartzJobFactory());
			for (int i = 0; i < tasks.size(); i++) {
				Sys_task task = tasks.get(i);

				Map<String, String> map = new HashMap<String, String>();
				map.put("param_value", task.getParamValue());
				map.put("task_interval", String.valueOf(task.getTaskInterval()));
				JobBuilder jobBuilder = JobBuilder.newJob(getClassByTask(task.getJobClass()));
				TriggerBuilder triggerBuilder = TriggerBuilder.newTrigger();
				if (StringUtils.isNotBlank(task.getTaskCode())) {
					jobBuilder.withIdentity(task.getTaskCode(), Scheduler.DEFAULT_GROUP);
					triggerBuilder.withIdentity(task.getTaskCode(), Scheduler.DEFAULT_GROUP);
				} else {
					UUID uuid = UUID.randomUUID();
					jobBuilder.withIdentity(uuid.toString(), Scheduler.DEFAULT_GROUP);
					triggerBuilder.withIdentity(uuid.toString(), Scheduler.DEFAULT_GROUP);
					task.setTaskCode(uuid.toString());
					sysTaskService.update(task);
				}
				map.put("task_code", String.valueOf(task.getTaskCode()));
				map.put("task_id", String.valueOf(task.getTaskId()));
				map.put("task_threadnum", String.valueOf(task.getTaskThreadnum()));
				jobBuilder.setJobData(getJobDataMap(map));
				String cronExpressionFromDB = getCronExpressionFromDB(task);
				log.info(cronExpressionFromDB);
				triggerBuilder.withSchedule(getCronScheduleBuilder(cronExpressionFromDB));
				// 调度任务
				appInfoService.getSCHEDULER().scheduleJob(jobBuilder.build(), triggerBuilder.build());
			}
			if (tasks.size() > 0) {
				appInfoService.getSCHEDULER().start();
			}
		} catch (SchedulerException e) {
			log.error(e);
		} catch (ClassNotFoundException e) {
			log.error(e);
		} catch (Exception e) {
			log.error(e);
		}
	}

	public static CronScheduleBuilder getCronScheduleBuilder(String cronExpression) {
		return CronScheduleBuilder.cronSchedule(cronExpression);
	}

	public String getCronExpressionFromDB(Sys_task task) {
		if (task.getExecycle() == 2) {
			return task.getCronExpression();
		} else {
			int execycle = task.getTaskIntervalUnit();
			String excep = "";
			if (execycle == 5) {// 月
				excep = "0  " + task.getMinute() + " " + task.getHour() + " " + task.getDayOfMonth() + " * ?";
			} else if (execycle == 4) {// 周
				excep = "0  " + task.getMinute() + " " + task.getHour() + " " + " ? " + " * " + task.getDay_of_week();
			} else if (execycle == 3) {// 日
				excep = "0  " + task.getMinute() + " " + task.getHour() + " " + " * * ?";
			} else if (execycle == 2) {// 时
				excep = "0 0 */" + task.getIntervalHour() + " * * ?";
			} else if (execycle == 1) {// 分
				excep = "0  */" + task.getIntervalMinute() + " * * * ?";
			}
			return excep;
		}
	}

	/**
	 * @param params
	 *            任务参数
	 * @return
	 */
	private JobDataMap getJobDataMap(Map<String, String> params) {
		JobDataMap jdm = new JobDataMap();
		Set<String> keySet = params.keySet();
		Iterator<String> it = keySet.iterator();
		while (it.hasNext()) {
			String key = it.next();
			jdm.put(key, params.get(key));
		}
		return jdm;
	}

	/**
	 * @param taskClassName
	 *            任务执行类名
	 * @return
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("rawtypes")
	private Class getClassByTask(String taskClassName) throws ClassNotFoundException {
		return Class.forName(taskClassName);
	}
}
