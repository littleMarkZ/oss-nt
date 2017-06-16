package com.oss.project;

import java.util.Date;

import com.eova.aop.AopContext;
import com.eova.aop.MetaObjectIntercept;
import com.eova.model.EovaLog;
import com.eova.model.MetaField;
import com.oss.model.ProjectLog;

public class ProjectIntercept extends MetaObjectIntercept {

	@Override
	public void addInit(AopContext ac) throws Exception {
		super.addInit(ac);
		// 遍历给字段赋值
		for (MetaField ei : ac.object.getFields()) {
			String key = ei.getEn();
			Object value = null;
			if ("author_id".equalsIgnoreCase(key)) {
				value = ac.user.get("id");
			} else if ("author_name".equalsIgnoreCase(key)) {
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
	public String updateBefore(AopContext ac) throws Exception {
		if (null == ac.record.getInt("modified_id")) {
			this.log(ac, EovaLog.ADD);
		}
		ac.record.set("modified_id", ac.user.get("id"));
		ac.record.set("modifier", ac.user.get("nickname"));
		ac.record.set("update_time", new Date());
		return super.updateBefore(ac);
	}

	@Override
	public String updateAfter(AopContext ac) throws Exception {
		this.log(ac, EovaLog.UPDATE);
		return super.updateAfter(ac);
	}

	@Override
	public String deleteBefore(AopContext ac) throws Exception {
		this.log(ac, EovaLog.DELETE);
		return super.deleteBefore(ac);
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
	
	private void log(AopContext ac, int type) throws Exception {
		ProjectLog projectLog = new ProjectLog();
		projectLog.set("name", ac.record.get("name"));
		projectLog.set("company_id", ac.record.get("company_id"));
		projectLog.set("bgwh", ac.record.get("bgwh"));
		projectLog.set("bbgs", ac.record.get("bbgs"));
		projectLog.set("bbje", ac.record.get("bbje"));
		projectLog.set("bbsjh", ac.record.get("bbsjh"));
		projectLog.set("ssje", ac.record.get("ssje"));
		projectLog.set("jbr_id", ac.record.get("jbr_id"));
		projectLog.set("jbr_name", ac.record.get("jbr_name"));
		projectLog.set("ywlyer_id", ac.record.get("ywlyer_id"));
		projectLog.set("ywlyer_name", ac.record.get("ywlyer_name"));
		projectLog.set("zjgs", ac.record.get("zjgs"));
		projectLog.set("jfbg_time", ac.record.get("jfbg_time"));
		projectLog.set("zsry_id", ac.record.get("zsry_id"));
		projectLog.set("zsry_name", ac.record.get("zsry_name"));
		projectLog.set("tc", ac.record.get("tc"));
		projectLog.set("dgzt", ac.record.get("dgzt"));
		projectLog.set("dgwz", ac.record.get("dgwz"));
		projectLog.set("kpje", ac.record.get("kpje"));
		projectLog.set("kpmc", ac.record.get("kpmc"));
		projectLog.set("fphm", ac.record.get("fphm"));
		projectLog.set("dkje", ac.record.get("dkje"));
		projectLog.set("xm_status", ac.record.get("xm_status"));
		projectLog.set("xmzt_qksm", ac.record.get("xmzt_qksm"));
		projectLog.set("xmzz", ac.record.get("xmzz"));
		projectLog.set("xmzz_qksm", ac.record.get("xmzz_qksm"));
		projectLog.set("remark", ac.record.get("remark"));
		projectLog.set("is_fftc", ac.record.get("is_fftc"));
		projectLog.set("ff_time", ac.record.get("ff_time"));
		projectLog.set("project_id", ac.record.get("id"));
		projectLog.set("modified_id", ac.user.get("id"));
		projectLog.set("modifier", ac.user.get("nickname"));
		String cn = "";
		if (type == EovaLog.UPDATE) {
			cn = "更新";
			projectLog.set("file_date", new Date());
		} else if (type == EovaLog.DELETE) {
			cn = "删除";
			projectLog.set("file_date", new Date());
		} else if (type == EovaLog.ADD) {
			cn = "新增";
			projectLog.set("file_date", ac.record.get("create_time"));
		}
		projectLog.set("type", cn);
		ProjectLog.dao.insert(ac.ctrl, projectLog);
	}
}
