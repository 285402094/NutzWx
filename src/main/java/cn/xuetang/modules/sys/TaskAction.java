package cn.xuetang.modules.sys;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.nutz.dao.Cnd;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;

import cn.xuetang.common.util.DateUtil;
import cn.xuetang.modules.sys.bean.Sys_task;
import cn.xuetang.modules.sys.bean.Sys_user;
import cn.xuetang.service.sys.AppInfoService;
import cn.xuetang.service.sys.SysTaskService;

/**
 * @author Wizzer
 * @time 2014-02-27 10:01:23
 */
@IocBean
@At("/private/sys/task")
public class TaskAction {
	private final static Log log = Logs.get();
	@Inject
	private AppInfoService appInfoService;
	@Inject
	private SysTaskService sysTaskService;

	@At("")
	@Ok("vm:template.private.sys.task")
	public void index() {

	}

	@At
	@Ok("vm:template.private.sys.taskAdd")
	public void toadd() {
	}

	@At
	@Ok("raw")
	public boolean add(@Param("..") Sys_task task, HttpSession session) throws SchedulerException, ClassNotFoundException {
		Sys_user user = (Sys_user) session.getAttribute("userSession");
		task.setTaskCode(UUID.randomUUID().toString());
		task.setCreate_time(DateUtil.getCurDateTime());
		task.setUserId(user.getUserid());
		if (sysTaskService.insert(task)) {
			startTask(task);
			return true;
		}
		return false;
	}

	@At
	@Ok("vm:template.private.sys.taskUpdate")
	public Sys_task toupdate(@Param("task_id") int task_id, HttpServletRequest req) {
		return sysTaskService.fetch(task_id);// html:obj
	}

	@At
	public boolean update(@Param("..") Sys_task task, HttpSession session) throws SchedulerException, ClassNotFoundException {
		Sys_user user = (Sys_user) session.getAttribute("userSession");
		// 结束之前的任务，开始新任务调度
		String oldTaskCode = task.getTaskCode();
		if (!Strings.isBlank(oldTaskCode)) {
			log.info("Update End..." + oldTaskCode);
			endTask(oldTaskCode);
		}
		UUID uuid = UUID.randomUUID();
		task.setTaskCode(uuid.toString());
		task.setUserId(user.getUserid());
		boolean res = sysTaskService.update(task);
		if (res && !task.isEnable()) {
			log.info("Update Satrt...");
			startTask(task);
		}
		return res;
	}

	@At
	public boolean delete(@Param("task_id") int task_id) throws SchedulerException, ClassNotFoundException {
		Sys_task task = sysTaskService.fetch(task_id);
		boolean res = sysTaskService.delete(task_id) > 0;
		if (res) {
			endTask(task.getTaskCode());
		}
		return res;
	}

	@At
	public boolean deleteIds(@Param("ids") String[] ids) throws SchedulerException, ClassNotFoundException {
		List<Sys_task> list = sysTaskService.listByCnd(Cnd.where("task_id", "in", ids));
		for (Sys_task task : list) {
			endTask(task.getTaskCode());
		}
		return sysTaskService.deleteByIds(ids);
	}

	@At
	@Ok("raw")
	public String list(@Param("page") int curPage, @Param("rows") int pageSize, HttpServletRequest req) {
		Sql sql = Sqls.create("SELECT A.*,B.REALNAME FROM SYS_TASK A,SYS_USER B WHERE A.USER_ID=B.USERID ORDER BY A.TASK_ID");
		Sql sql1 = Sqls.create("SELECT COUNT(*) FROM SYS_TASK A,SYS_USER B WHERE A.USER_ID=B.USERID");
		return sysTaskService.listPageJsonSql(sql, curPage, pageSize, sysTaskService.getIntRowValue(sql1));
	}

	/**
	 * 开始任务调度
	 * 
	 * @param task
	 *            任务
	 */
	private void startTask(Sys_task task) {
		try {
			String cronExpression = getCronExpressionFromDB(task);
			if (StringUtils.isNotBlank(cronExpression)) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("param_value", task.getParamValue());
				JobBuilder jobBuilder = JobBuilder.newJob(getClassByTask(task.getJobClass()));
				jobBuilder.setJobData(getJobDataMap(map));
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
				log.info("task start code:" + task.getTaskCode());
				String cronExpressionFromDB = getCronExpressionFromDB(task);
				log.info(cronExpressionFromDB);
				triggerBuilder.withSchedule(getCronScheduleBuilder(cronExpressionFromDB));
				// 调度任务
				appInfoService.getSCHEDULER().scheduleJob(jobBuilder.build(), triggerBuilder.build());
				if (appInfoService.getSCHEDULER().isShutdown()) {
					appInfoService.getSCHEDULER().start();
				}
			}
		} catch (ClassNotFoundException e) {
			log.error(e);
		} catch (SchedulerException e) {
			log.error(e);
		}
	}

	/**
	 * 结束任务调度
	 * 
	 * @param taskName
	 */
	private void endTask(String taskName) throws SchedulerException {
		if (appInfoService.getSCHEDULER().checkExists(JobKey.jobKey(taskName, Scheduler.DEFAULT_GROUP)))
			appInfoService.getSCHEDULER().deleteJob(JobKey.jobKey(taskName, Scheduler.DEFAULT_GROUP));
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
	private Class getClassByTask(String taskClassName) throws ClassNotFoundException {
		return Class.forName(taskClassName);
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
}