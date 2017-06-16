package com.oss.group;

import java.util.Date;

import com.eova.aop.AopContext;
import com.eova.aop.MetaObjectIntercept;
import com.eova.model.MetaField;

public class GroupIntercept extends MetaObjectIntercept {
	@Override
	public void addInit(AopContext ac) throws Exception {
		super.addInit(ac);
		// 遍历给字段赋值
		for (MetaField ei : ac.object.getFields()) {
			String key = ei.getEn();
			Object value = null;
			if("create_time".equalsIgnoreCase(key)){
				value = new Date();
			}else{
				continue;
			}
			ei.put("value", value);
		}
	}

	@Override
	public String addBefore(AopContext ac) throws Exception {
		ac.record.set("create_time", new Date());
		return super.addBefore(ac);
	}
}
