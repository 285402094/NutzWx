package cn.xuetang.modules.sys.bean;

import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.EL;
import org.nutz.dao.entity.annotation.Name;
import org.nutz.dao.entity.annotation.Prev;
import org.nutz.dao.entity.annotation.Table;

/**
 * @author
 * @time 2014-03-26 14:25:53
 */
@Table("sys_dict")
public class Sys_dict {
	@Name
	@Prev(els = { @EL("uuid()") })
	private String id;
	@Column
	private String dkey;
	@Column
	@ColDefine(width = 1024)
	private String dval;
	@Column
	@ColDefine(width = 1024)
	private String txt;
	@Column
	private int status;
	@Column
	private int location;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDkey() {
		return dkey;
	}

	public void setDkey(String dkey) {
		this.dkey = dkey;
	}

	public String getDval() {
		return dval;
	}

	public void setDval(String dval) {
		this.dval = dval;
	}

	public String getTxt() {
		return txt;
	}

	public void setTxt(String txt) {
		this.txt = txt;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getLocation() {
		return location;
	}

	public void setLocation(int location) {
		this.location = location;
	}

}