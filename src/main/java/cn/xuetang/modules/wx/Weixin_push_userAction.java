package cn.xuetang.modules.wx;
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

import cn.xuetang.modules.wx.bean.Weixin_push_user;

/**
 * @author Wizzer
 * @time 2014-04-28 15:23:18
 * 
 */
@IocBean
@At("/private/wx/weixin_push_user")
@Filters({ @By(type = GlobalsFilter.class), @By(type = UserLoginFilter.class) })
public class Weixin_push_userAction extends BaseAction {
	@Inject
	protected Dao dao;

	@At("")
	@Ok("->:/private/wx/Weixin_push_user.html")
	public void index(@Param("sys_menu") String sys_menu,HttpServletRequest req) {
		req.setAttribute("sys_menu",sys_menu);
	}
	
	@At
	@Ok("->:/private/wx/Weixin_push_userAdd.html")
	public void toadd() {
	
	}
	
	@At
	@Ok("raw")
	public boolean add(@Param("..") Weixin_push_user weixin_push_user) {
		return daoCtl.add(dao,weixin_push_user);
	}
	
	//@At
	//@Ok("raw")
	//public int add(@Param("..") Weixin_push_user weixin_push_user) {
	//	return daoCtl.addT(dao,weixin_push_user).getId();
	//}
	
	@At
	@Ok("json")
	public Weixin_push_user view(@Param("id") int id) {
		return daoCtl.detailById(dao,Weixin_push_user.class, id);
	}
	
	@At
	@Ok("->:/private/wx/Weixin_push_userUpdate.html")
	public Weixin_push_user toupdate(@Param("id") int id, HttpServletRequest req) {
		return daoCtl.detailById(dao, Weixin_push_user.class, id);//html:obj
	}
	
	@At
	public boolean update(@Param("..") Weixin_push_user weixin_push_user) {
		return daoCtl.update(dao, weixin_push_user);
	}
	
	@At
	public boolean delete(@Param("id") int id) {
		return daoCtl.deleteById(dao, Weixin_push_user.class, id);
	}
	
	@At
	public boolean deleteIds(@Param("ids") Integer[] ids) {
		return daoCtl.delete(dao, Weixin_push_user.class, Cnd.where("id", "in", ids)) > 0;
	}
	
	@At
	@Ok("raw")
	public String list(@Param("page") int curPage, @Param("rows") int pageSize){
		Criteria cri = Cnd.cri();
		cri.where().and("1","=",1);
		cri.getOrderBy().desc("id");
		return daoCtl.listPageJson(dao, Weixin_push_user.class, curPage, pageSize, cri);
	}

}