/**
 * Copyright (c) 2013-2016, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.eova.core.user;

import com.eova.aop.AopContext;
import com.eova.aop.MetaObjectIntercept;
import com.eova.common.utils.EncryptUtil;
import com.eova.config.EovaConst;

public class UserIntercept extends MetaObjectIntercept {

	@Override
	public String addBefore(AopContext ac) throws Exception {
		// 新增时密码加密储存
		ac.record.set("login_pwd", EncryptUtil.getSM32(EovaConst.DEFAULT_PWD));
		return null;
	}

}