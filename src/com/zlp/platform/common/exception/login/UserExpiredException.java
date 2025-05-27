/**
 * Header : com.zlp.platform.common.exception.login
 * Copyright (c) 2018 Novacloud corporation All Rights Reserved.
 * LICENSE INFORMATION
 */
package com.zlp.platform.common.exception.login;

/**
 *	用户操作相关异常
 *
 *  @Package com.zlp.platform.common.exception.login
 *	@author liyh
 *	@version Ver 1.0 2018-04-02 17:12 新增
 */
public class UserExpiredException extends RuntimeException{

    private static final long serialVersionUID = -6098343152899748638L;
    public static final String EXPIRED = "登录过期请重新登录";

    public UserExpiredException(String s) {
        super(s);
    }
}
