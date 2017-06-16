package com.oss.model;

import com.eova.common.base.BaseModel;
import com.jfinal.core.Controller;

public class ProjectSjsj extends BaseModel<ProjectSjsj> {

	private static final long serialVersionUID = -3508672040929561759L;

	public static final ProjectSjsj dao = new ProjectSjsj();

	public void doCheck(Controller con, int projectId, String bsdwmc,
			String sjbgmc, String bgwh) {
		ProjectSjsj sjsj = new ProjectSjsj();
		sjsj.set("project_id", projectId);
		sjsj.set("bsdwmc", bsdwmc);
		sjsj.set("sjbgmc", sjbgmc);
		sjsj.set("bgwh", bgwh);
		sjsj.save();
	}

}
