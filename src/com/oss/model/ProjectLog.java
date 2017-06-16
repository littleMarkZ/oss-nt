package com.oss.model;

import com.eova.common.base.BaseModel;
import com.jfinal.core.Controller;

public class ProjectLog extends BaseModel<ProjectLog> {

	private static final long serialVersionUID = -52688793067700559L;

	public static final ProjectLog dao = new ProjectLog();

	public void insert(Controller con, ProjectLog projectLog) {
		projectLog.save();
	}

}
