package com.oss.offer;

import java.util.Date;

import com.eova.aop.AopContext;
import com.eova.aop.MetaObjectIntercept;
import com.eova.model.MetaField;

public class OfferIntercept extends MetaObjectIntercept {
	

	@Override
	public void addInit(AopContext ac) throws Exception {
		super.addInit(ac);
		// 遍历给字段赋值
		for (MetaField ei : ac.object.getFields()) {
			String key = ei.getEn();
			Object value = null;
			if ("author_name".equalsIgnoreCase(key)) {
				value = ac.user.get("nickname");
			} else if ("create_time".equalsIgnoreCase(key)) {
				value = new Date();
			} else {
				continue;
			}

			if (value == null) {
				value = "";
			}
			ei.put("value", value);
		}
	}

	@Override
	public String addBefore(AopContext ac) throws Exception {
		ac.record.set("author_id", ac.user.get("id"));
		return super.addBefore(ac);
	}

	@Override
	public void queryBefore(AopContext ac) throws Exception {
		super.queryBefore(ac);
		String sql = " and (exists(select 1 from oss_group_user gu where gu.user_id = alias.author_id and gu.group_id = "
				+ Integer.parseInt(ac.user.get("group_id").toString())
				+ ") or alias.author_id = "
				+ Integer.parseInt(ac.user.get("id").toString()) + ")";
		ac.condition = sql;
	}
}
