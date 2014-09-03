package cn.xuetang.modules.sys;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import cn.xuetang.modules.sys.bean.Sys_resource;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Attr;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;
import org.nutz.web.Webs;

import cn.xuetang.modules.sys.bean.Sys_user;
import cn.xuetang.service.sys.SysResourceService;
import cn.xuetang.service.sys.SysUserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Wizzer.cn
 * @time 2012-9-13 上午10:54:04
 */
@IocBean
@At("/private")
@RequiresAuthentication
public class IndexAction {
	@Inject
	protected SysUserService sysUserService;

	@Inject
	private SysResourceService sysResourceService;

	@At
	@Ok("raw")
	public boolean reload(@Attr(Webs.ME) Sys_user user, @Param("resid") String resid, HttpSession session) {
		sysUserService.update(Chain.make("loginresid", resid), Cnd.where("userid", "=", user.getUserid()));
		user.setLoginresid(resid);
		return true;
	}

	@At
	@Ok("vm:template.private.index")
	public Object index(@Attr(Webs.ME) Sys_user user, HttpServletRequest req) {
        Sql sql = Sqls
                .create("select distinct resourceid from sys_role_resource where ( roleid in(select roleid from sys_user_role where userid=@userid) or roleid=1) and resourceid not in(select id from sys_resource where state=1)");
        sql.params().set("userid", user.getUserid());
        user.setReslist(sysResourceService.getStrRowValues(sql));
        Sql role_sql = Sqls.create("select * from sys_role where id in(select roleid from sys_user_role where userid=@userid)");
        role_sql.params().set("userid", user.getUserid());
        List<Map> rolelist=sysResourceService.listMap(role_sql);
        List<Integer> rolelist1 = new ArrayList<Integer>();
        List<Integer> plist = new ArrayList<Integer>();
        for (Map map : rolelist) {
            rolelist1.add(NumberUtils.toInt(Strings.sNull(map.get("id"))));
            int pid = NumberUtils.toInt(Strings.sNull(map.get("pid")));
            if (!plist.contains(pid))
                plist.add(pid);
        }
        if (rolelist1.contains(2)) {//role id中包含2则为超级管理员角色
            user.setSystem(true);
        } else {
            user.setSystem(false);
        }
        user.setRolelist(rolelist1);//设置登陆用户的role id列表
        user.setProlist(plist);//设置登陆用户所属角色的项目id 列表（一个用户可以管理多个项目数据，一个项目下可以设置多个公众号）
        //user.getStyle() 用户最后选择的界面样式，原来是有菜单在左和菜单在上的
        req.setAttribute("menulist", "\n" + getMenuList("", user) + "\n");
		return sysResourceService.listByCnd(null);
	}

    private String getMenuList(String id, Sys_user user) {
        if (user.getReslist() == null || user.getReslist().size() < 1)
            return "";
        List<Sys_resource> list = sysResourceService.listByCnd(Cnd.where("id", "like", id + "____").and("id", "in", user.getReslist()).asc("location"));
        String str = "\n";

        for (Sys_resource resource : list) {
            int sub = sysResourceService.getRowCount(Cnd.where("id", "like", resource.getId() + "____").and("id", "in", user.getReslist()));
            if (sub > 0) {
                str += "\t\n<li>\t\t\n";
                if (Strings.isBlank(resource.getUrl())) {
                    str += "<a href=\"javascript:;\">";
                } else {
                    str += "<a class=\"ajaxify\" href=\"" + resource.getUrl() + "\">";
                }
                str += "\t\t\t\n<i class=\"" + resource.getStyle() + "\"></i>\t\t\t\n<span class=\"title\">" + resource.getName() + "</span>\t\t\t\n<span class=\"arrow\"></span>";

                str += "\t\t\n</a>\n";
                str += "\t\t\n<ul class=\"sub-menu\">";
                str += getMenuList(resource.getId(), user);
                str += "\t\t\n</ul>\t\n</li>\n";
            } else {
                if (Strings.isBlank(resource.getStyle())) {
                    str += "\t\n<li>\t\t\n<a class=\"ajaxify\" href=\"" + resource.getUrl() + "\">\t\t\t\n<i class=\"fa\"></i>\t\t\t\n" + resource.getName() + "</a>\n</li>\n";
                } else {
                    str += "\t\n<li>\t\t\n<a class=\"ajaxify\" href=\"" + resource.getUrl() + "\">\t\t\t\n<i class=\"" + resource.getStyle() + "\"></i>\t\t\t\n" + resource.getName() + "</a>\n</li>\n";
                }
            }
        }
        str += "\n";
        return str;
    }

}
