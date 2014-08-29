package cn.xuetang.modules.user;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.StringUtils;

import org.nutz.dao.*;
import org.nutz.dao.sql.Criteria;
import org.nutz.dao.util.cri.SqlExpressionGroup;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param; 

import cn.xuetang.common.action.BaseAction;
import cn.xuetang.common.filter.GlobalsFilter;
import cn.xuetang.common.filter.UserLoginFilter;

import cn.xuetang.modules.user.bean.User_conn_qq;

/**
 * @author Wizzer
 * @time 2014-04-01 10:11:06
 * 
 */
@IocBean
@At("/private/user/user_conn_qq")
@Filters({ @By(type = GlobalsFilter.class), @By(type = UserLoginFilter.class) })
public class User_conn_qqAction extends BaseAction {
	@Inject
	protected Dao dao;

	@At("/index")
	@Ok("->:/private/user/User_conn_qq.html")
	public void index(HttpSession session,HttpServletRequest req) {
	
	}
	
	@At
	@Ok("->:/private/user/User_conn_qqAdd.html")
	public void toadd() {
	
	}
	
	@At
	@Ok("raw")
	public boolean add(@Param("..") User_conn_qq user_conn_qq) {
		return daoCtl.add(dao,user_conn_qq);
	}
	
	//@At
	//@Ok("raw")
	//public int add(@Param("..") User_conn_qq user_conn_qq) {
	//	return daoCtl.addT(dao,user_conn_qq).getId();
	//}
	
	@At
	@Ok("json")
	public User_conn_qq view(@Param("id") int id) {
		return daoCtl.detailById(dao,User_conn_qq.class, id);
	}
	
	@At
	@Ok("->:/private/user/User_conn_qqUpdate.html")
	public User_conn_qq toupdate(@Param("id") int id, HttpServletRequest req) {
		return daoCtl.detailById(dao, User_conn_qq.class, id);//html:obj
	}
	
	@At
	public boolean update(@Param("..") User_conn_qq user_conn_qq) {
		return daoCtl.update(dao, user_conn_qq);
	}
	
	@At
	public boolean delete(@Param("id") int id) {
		return daoCtl.deleteById(dao, User_conn_qq.class, id);
	}
	
	@At
	public boolean deleteIds(@Param("ids") String ids) {
		return daoCtl.deleteByIds(dao, User_conn_qq.class, StringUtils.split(ids,","));
	}
	
	@At
	@Ok("raw")
	public String list(@Param("page") int curPage, @Param("rows") int pageSize){
		Criteria cri = Cnd.cri();
		cri.where().and("1","=",1);
		cri.getOrderBy().desc("id");
		return daoCtl.listPageJson(dao, User_conn_qq.class, curPage, pageSize, cri);
	}

}