package com.oss.model;

import com.eova.common.base.BaseModel;
import com.jfinal.core.Controller;

public class Project extends BaseModel<Project> {

	private static final long serialVersionUID = 7485981710991983115L;

	public static final Project dao = new Project();
	
	public void updateFqsh(Controller con, int id) {
		Project project = this.findById(id);
		project.set("is_fqsh", "Y");
		project.update();
	}
}
