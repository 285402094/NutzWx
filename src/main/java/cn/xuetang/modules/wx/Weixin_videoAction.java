package cn.xuetang.modules.wx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.math.NumberUtils;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.QueryResult;
import org.nutz.dao.Sqls;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Attr;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;
import org.nutz.web.Webs;

import cn.xuetang.common.config.Dict;
import cn.xuetang.common.util.DateUtil;
import cn.xuetang.modules.sys.bean.Sys_user;
import cn.xuetang.modules.user.bean.User_info;
import cn.xuetang.service.sys.AppInfoService;
import cn.xuetang.service.sys.AppProjectService;
import cn.xuetang.service.user.UserInfoService;
import cn.xuetang.service.wx.WeixinVideoService;

/**
 * @author Wizzer
 * @time 2014-04-08 22:16:05
 */
@IocBean
@At("/private/wx/video")
public class Weixin_videoAction {
	private final static Log log = Logs.get();
	@Inject
	private AppInfoService appInfoService;
	@Inject
	private AppProjectService appProjectService;
	@Inject
	private UserInfoService userInfoService;
	@Inject
	private WeixinVideoService weixinVideoService;

	@At("")
	@Ok("vm:template.private.wx.Weixin_video")
	public void index(@Attr(Webs.ME) Sys_user user, @Param("sys_menu") String sys_menu, HttpServletRequest req, HttpSession session) {
		Pager pager = new Pager();
		pager.setPageNumber(1);
		pager.setPageSize(8);
		req.setAttribute("pro", appProjectService.listByCnd(Cnd.where("id", "in", user.getProlist()).asc("id")));
		req.setAttribute("sys_menu", sys_menu);
	}

	@At
	@Ok("vm:template.private.wx.Weixin_imageMovetv")
	public void moveTv(@Param("tvid") int tvid, @Param("id") String id, HttpServletRequest req) {
		Pager pager = new Pager();
		pager.setPageNumber(1);
		pager.setPageSize(8);
		req.setAttribute("tvid", tvid);
		req.setAttribute("id", id);
	}

	@At
	public boolean move(@Param("tvid") int tvid, @Param("totvid") int totvid, @Param("id") Integer[] id) {
		weixinVideoService.update(Chain.make("tvid", totvid), Cnd.where("id", "in", id));
		return true;
	}

	@At
	public boolean deleteIds(@Param("ids") String[] ids) {
		return weixinVideoService.deleteByIds(ids);
	}

	@At
	public int getRow(@Param("id") int id) {
		return weixinVideoService.fetch(id).getStatus();
	}

	@At
	public boolean changeStatus(@Param("id") int id, @Param("status") int status) {
		weixinVideoService.update(Chain.make("status", status), Cnd.where("id", "=", id));
		return true;
	}

	@At
	@Ok("raw")
	public String list(@Param("pid") int pid, @Param("tvid") String tvid, @Param("ageStart") int ageStart, @Param("ageEnd") int ageEnd, @Param("name") String name, @Param("timeStart") String timeStart, @Param("timeEnd") String timeEnd, @Param("page") int curPage, @Param("rows") int pageSize) {
		long a = System.currentTimeMillis();
		Sql sql = Sqls.create("SELECT a.*,b.NAME,b.BIRTH_YEAR,b.BIRTH_DATE,b.SEX,b.UP_TIMES,b.OPEN_STATUS,b.SHOW_STATUS,b.UP_TIMES FROM Weixin_video a LEFT JOIN baby_info b ON a.openid=b.openid $sql order by a.id desc");
		String str = " WHERE 1=1 ";
		boolean isCountBaby = false;
		String countStr = "select count(1) from weixin_video a where 1=1";
		if (!Strings.isBlank(name)) {
			isCountBaby = true;
			str += " and b.name like '%" + name + "%'";
		}
		if (!Strings.isBlank(timeStart)) {
			countStr += " and a.post_time >='" + timeStart + " 00:00:00'";
			str += " and a.post_time >='" + timeStart + " 00:00:00'";
		}
		if (!Strings.isBlank(timeEnd)) {
			countStr += " and a.post_time <='" + timeEnd + " 23:59:59'";
			str += " and a.post_time <='" + timeEnd + " 23:59:59'";
		}
		if (ageStart > 0) {
			isCountBaby = true;
			int curyear = NumberUtils.toInt(DateUtil.getTime("yyyy"));
			str += " and b.birth_year <='" + (curyear - ageStart) + "'";
		}
		if (ageEnd > 0) {
			isCountBaby = true;
			int curyear = NumberUtils.toInt(DateUtil.getTime("yyyy"));
			str += " and b.birth_year >='" + (curyear - ageEnd) + "'";
		}

		countStr += " and a.pid =" + pid;
		str += " and a.pid =" + pid;

		if ("".equals(Strings.sNull(tvid))) {
			countStr += " and a.status=0";
			str += " and a.status=0";
		} else if ("notv".equals(Strings.sNull(tvid))) {
			countStr += " and a.tvid=0 and a.status=0";
			str += " and a.tvid=0 and a.status=0";
		} else if ("noshow".equals(Strings.sNull(tvid))) {
			countStr += " and a.status=1";
			str += " and a.status=1";
		} else {
			countStr += " and a.tvid=" + tvid;
			str += " and a.tvid=" + tvid;
		}
		sql.vars().set("sql", str);
		int count;
		if (isCountBaby) {
			count = weixinVideoService.getIntRowValue(Sqls.create("SELECT COUNT(1) " + sql.toString().substring(sql.toString().indexOf("FROM"), sql.toString().indexOf("order"))));
		} else {
			count = weixinVideoService.getIntRowValue(Sqls.create(countStr));
		}
		// 获得图片及宝贝资料
		QueryResult qr = weixinVideoService.listPageSql(sql, curPage, pageSize, count);
		List<Integer> list = new ArrayList<Integer>();
		List<Integer> tvlist = new ArrayList<Integer>();
		for (Map info : qr.getList(Map.class)) {
			int uid = NumberUtils.toInt(Strings.sNull(info.get("uid")));
			int tid = NumberUtils.toInt(Strings.sNull(info.get("tvid")));
			if (!list.contains(uid) && uid > 0)
				list.add(uid);// 取出用户ID
			if (!tvlist.contains(tid) && tid > 0)
				tvlist.add(tid);// 取出节目信息

		}

		// 获得用户资料
		Map<Integer, Object> map = new HashMap<Integer, Object>();
		if (list.size() > 0) {
			List<User_info> userInfoList = userInfoService.listByCnd(Cnd.where("uid", "in", list));
			for (User_info info : userInfoList) {
				map.put(info.getUid(), info);
			}
		}
		// 组装分页数据
		List<Object> listjson = new ArrayList<Object>();
		Map<String, String> dict = (HashMap) appInfoService.getDATA_DICT().get(Dict.DIVSION);
		for (Map info : qr.getList(Map.class)) {
			User_info userInfo = (User_info) map.get(NumberUtils.toInt(Strings.sNull(info.get("uid"))));
			Map<String, Object> objectMap = Lang.obj2map(info);
			if (!Strings.isBlank(Strings.sNull(info.get("birth_year")))) {
				objectMap.put("age", (NumberUtils.toInt(DateUtil.getTime("yyyy")) - NumberUtils.toInt(Strings.sNull(info.get("birth_year")))));
			} else {
				objectMap.put("age", 0);
			}
			if (userInfo != null) {
				objectMap.put("province", Strings.sNull(dict.get(userInfo.getProvince())));
				objectMap.put("city", Strings.sNull(dict.get(userInfo.getCity())));
				objectMap.put("area", Strings.sNull(dict.get(userInfo.getArea())));
				objectMap.put("address", Strings.sNull(userInfo.getAddress()));
				objectMap.put("phone", Strings.sNull(userInfo.getPhone()));
				objectMap.put("mobile", Strings.sNull(userInfo.getMobile()));
				objectMap.put("postcode", Strings.sNull(userInfo.getPostcode()));
			}
			listjson.add(objectMap);
		}
		Map<String, Object> jsonobj = new HashMap<String, Object>();
		jsonobj.put("total", qr.getPager().getRecordCount());
		jsonobj.put("rows", listjson);
		log.debug("/private/wx/video/list Load in " + (System.currentTimeMillis() - a) + "ms");
		return Json.toJson(jsonobj);
	}

}