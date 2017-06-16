package com.oss.model;

import com.eova.common.base.BaseModel;
import com.jfinal.core.Controller;

public class ProjectFj extends BaseModel<ProjectFj> {

	private static final long serialVersionUID = -3508672040929561759L;

	public static final ProjectFj dao = new ProjectFj();

	public void attach(Controller con, int projectId, String filename,
			String canonicalPath, String type) {
		ProjectFj fj = this
				.findFirst(
						"select * from oss_project_fj where project_id = ? and filename = ? and canonical_path = ?",
						projectId, filename, canonicalPath);
		if (null == fj) {
			fj = new ProjectFj();
			fj.set("project_id", projectId);
			fj.set("filename", filename);
			fj.set("canonical_path", canonicalPath);
			fj.set("type", type);
			fj.save();
		}
	}

	public ProjectFj loadById(Controller con, String id) {
		return this.findById(id);
	}
}
