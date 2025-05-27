package com.zlp.platform.util.token;

import java.util.List;

//import com.novacloud.uauslese.model.sys.SysUser;
//import com.novacloud.uauslese.model.test.TUser;
import com.zlp.platform.common.NcpSession;
import com.zlp.platform.dao.sys.Org;


/**
 *	保存用户的token信息<br>
 *	   以后根据项目需要修改此类
 *
 *  @Package com.zlp.platform.util.token
 *	@author liyh
 *	@version Ver 1.0 2019-06-03 17:17 新增
 */

public class UserToken {
    private String userid;

    //帐号
    private String usercode;
    //姓名
    private String username;

    private String usertype;

    private String status;

    private String enterpinfoid;
    public UserToken(){

    }

    public UserToken(SysUser sysUser) {
        this.userid = sysUser.getUserId();
        //this.orgid = sysUser.();
        this.username = sysUser.getUserName();
        this.usertype = sysUser.getUserType();
        //this.status = sysUser.();
        this.usercode = sysUser.getUserCode();
        this.orgList = sysUser.getOrgList();
        //this.enterpinfoid = sysUser.getEnterpinfoid();
    }

    public String getEnterpinfoid() {
        return enterpinfoid;
    }

    public void setEnterpinfoid(String enterpinfoid) {
        this.enterpinfoid = enterpinfoid;
    }

    public String getUsercode() {
        return usercode;
    }

    public void setUsercode(String usercode) {
        this.usercode = usercode;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

	private List<Org> orgList;

	public List<Org> getOrgList() {
		return this.orgList;
	}

	public void setOrgList(List<Org> orgList) {
		this.orgList = orgList;
	}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsertype() {
        return usertype;
    }

    public void setUsertype(String usertype) {
        this.usertype = usertype;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
