package cn.xuetang.modules.wx;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import cn.xuetang.common.config.Dict;
import cn.xuetang.common.config.Globals;
import org.apache.commons.lang.math.NumberUtils;
import org.nutz.dao.*;
import org.nutz.dao.sql.Criteria;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;

import cn.xuetang.common.action.BaseAction;
import cn.xuetang.common.filter.GlobalsFilter;
import cn.xuetang.common.filter.UserLoginFilter;

import cn.xuetang.modules.wx.bean.Weixin_channel_attr;

import java.util.Map;

/**
 * @author Wizzer
 * @time 2014-05-18 09:41:37
 */
@IocBean
@At("/private/wx/channel/attr")
@Filters({@By(type = GlobalsFilter.class), @By(type = UserLoginFilter.class)})
public class Weixin_channel_attrAction extends BaseAction {
    @Inject
    protected Dao dao;

    @At
    @Ok("->:/private/wx/Weixin_channelAttrAdd.html")
    public void toadd(@Param("classid") String classid, HttpServletRequest req) {
        Map<String, String> map = (Map) Globals.DATA_DICT.get(Dict.FORM_TYPE);
        req.setAttribute("formmap", map);
        req.setAttribute("classid", classid);
    }

    @At
    @Ok("raw")
    public boolean add(@Param("..") Weixin_channel_attr channel_attr) {
        Weixin_channel_attr attr = daoCtl.detailByCnd(dao, Weixin_channel_attr.class, Cnd.where("classid", "=", channel_attr.getClassid()).desc("attr_code"));
        String code = "attr_1";
        if (attr != null) {
            code = "attr_" + (NumberUtils.toInt(Strings.sNull(attr.getAttr_code()).replace("attr_", "")) + 1);

        }
        channel_attr.setAttr_code(code);
        return daoCtl.add(dao, channel_attr);
    }


    @At
    @Ok("->:/private/wx/Weixin_channelAttrUpdate.html")
    public Weixin_channel_attr toupdate(@Param("id") int id, HttpServletRequest req) {
        Map<String, String> map = (Map) Globals.DATA_DICT.get(Dict.FORM_TYPE);
        req.setAttribute("formmap", map);
        return daoCtl.detailById(dao, Weixin_channel_attr.class, id);//html:obj
    }

    @At
    public boolean update(@Param("..") Weixin_channel_attr weixin_channel_attr) {
        return daoCtl.update(dao, weixin_channel_attr);
    }

    @At
    public boolean delete(@Param("id") int id) {
        return daoCtl.deleteById(dao, Weixin_channel_attr.class, id);
    }

    @At
    public boolean deleteIds(@Param("ids") Integer[] ids) {
        return daoCtl.delete(dao, Weixin_channel_attr.class, Cnd.where("id", "in", ids)) > 0;
    }

    @At
    @Ok("raw")
    public String list(@Param("classid") String classid, @Param("page") int curPage, @Param("rows") int pageSize) {
        Criteria cri = Cnd.cri();
        cri.where().and("classid", "=", classid);
        cri.getOrderBy().desc("id");
        return daoCtl.listPageJson(dao, Weixin_channel_attr.class, curPage, pageSize, cri);
    }

}