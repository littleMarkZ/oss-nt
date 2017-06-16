package com.oss.project;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.joda.time.DateTime;

import com.eova.aop.AopContext;
import com.eova.aop.MetaObjectIntercept;
import com.eova.common.utils.io.FileUtil;
import com.eova.common.utils.time.FormatUtil;
import com.eova.config.EovaConfig;
import com.eova.model.MetaObject;
import com.eova.service.sm;
import com.eova.template.common.util.TemplateUtil;
import com.eova.widget.WidgetManager;
import com.jfinal.core.Controller;
import com.oss.model.Project;
import com.oss.model.ProjectFj;

public class ProjectController extends Controller {

	final Controller ctrl = this;

	static final int BUFFER_SIZE = 100 * 1024;

	/** 自定义拦截器 **/
	protected MetaObjectIntercept intercept = null;

	public void add() throws Exception {
		String objectCode = this.getPara(0);
		MetaObject object = sm.meta.getMeta(objectCode);
		// 构建关联参数值
		WidgetManager.buildRef(this, object);
		intercept = TemplateUtil.initIntercept(object.getBizIntercept());
		if (intercept != null) {
			AopContext ac = new AopContext(ctrl, object);
			intercept.addInit(ac);
			setAttr("author_name", ac.user.get("nickname"));
			setAttr("create_time", FormatUtil.format(new Date(), FormatUtil.YYYY_MM_DD_HH_MM_SS));
		}

		setAttr("object", object);
		setAttr("fields", object.getFields());
		render("/oss/project/add.html");
	}

	public void update() throws Exception {
		int id = getParaToInt(0);

		Project project = Project.dao.findById(id);
		setAttr("project", project);
		render("/oss/project/update.html");
	}

	public void detail() throws Exception {
		int id = getParaToInt(0);

		Project project = Project.dao.findById(id);
		setAttr("project", project);
		render("/oss/project/detail.html");
	}

	public void upload() throws Exception {
		int id = getParaToInt(0);

		Project project = Project.dao.findById(id);
		setAttr("project", project);
		render("/oss/project/upload.html");
	}

	public void plupload() throws Exception {
		HttpServletRequest request = this.getRequest();
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		int id = getParaToInt(0);
		if (isMultipart) {
			String fileName = "";
			Integer chunk = 0, chunks = 0;

			String fileDir = EovaConfig.props.get("static_root");
			String canonical_path = File.separator + "project" + File.separator
					+ String.valueOf(id);
			// 检查文件目录，不存在则创建
			File folder = new File(fileDir + canonical_path);
			if (!folder.exists()) {
				folder.mkdirs();
			}

			DiskFileItemFactory diskFactory = new DiskFileItemFactory();
			// threshold 极限、临界值，即硬盘缓存 1M
			diskFactory.setSizeThreshold(4 * 1024);

			ServletFileUpload upload = new ServletFileUpload(diskFactory);
			// 设置允许上传的最大文件大小（单位MB）
			upload.setSizeMax(1024 * 1048576);
			upload.setHeaderEncoding("UTF-8");

			try {
				List<FileItem> fileList = upload.parseRequest(request);
				Iterator<FileItem> it = fileList.iterator();
				boolean flag = true;
				while (it.hasNext()) {
					FileItem item = it.next();
					String name = item.getFieldName();
					InputStream input = item.getInputStream();
					if ("name".equals(name)) {
						fileName = Streams.asString(input);
						continue;
					}
					if ("chunk".equals(name)) {
						chunk = Integer.valueOf(Streams.asString(input));
						continue;
					}
					if ("chunks".equals(name)) {
						chunks = Integer.valueOf(Streams.asString(input));
						continue;
					}
					// 处理上传文件内容
					if (!item.isFormField()) {
						// 目标文件
						File destFile = new File(folder, fileName);
						// 文件已存在修改旧文件名（上传了同名的文件）
						if (chunk == 0 && destFile.exists()) {
							renameFile(destFile);
							destFile = new File(folder, fileName);
						}
						// 合成文件
						flag = appendFile(input, destFile);
						if (!flag) {
							break;
						}
					}
				}
				if (flag) {
					ProjectFj.dao.attach(this, id, fileName, canonical_path,
							fileName.substring(fileName.lastIndexOf(".") + 1));
				}
			} catch (FileUploadException ex) {
				System.out.println("上传文件失败：" + ex.getMessage());
				return;
			}
		}
		render("/oss/project/upload.html");
	}

	public void download() {
		String id = getPara(0);
		ProjectFj fj = ProjectFj.dao.loadById(this, id);
		renderFile(FileUtil.formatPath(fj.get("canonical_path").toString())
				+ File.separator + fj.get("filename").toString());
	}

	private boolean appendFile(InputStream in, File destFile) {
		OutputStream out = null;
		try {
			// plupload 配置了chunk的时候新上传的文件append到文件末尾
			if (destFile.exists()) {
				out = new BufferedOutputStream(new FileOutputStream(destFile,
						true), BUFFER_SIZE);
			} else {
				out = new BufferedOutputStream(new FileOutputStream(destFile),
						BUFFER_SIZE);
			}
			in = new BufferedInputStream(in, BUFFER_SIZE);

			int len = 0;
			byte[] buffer = new byte[BUFFER_SIZE];
			while ((len = in.read(buffer)) > 0) {
				out.write(buffer, 0, len);
			}
			return true;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		} finally {
			try {
				if (null != in) {
					in.close();
				}
				if (null != out) {
					out.close();
				}
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	/**
	 * 文件重命名
	 * 
	 * @throws Exception
	 */
	private void renameFile(File file) throws Exception {
		if (null != file) {
			String fName = file.getCanonicalPath();
			String path = fName.substring(0, fName.lastIndexOf("\\"));
			String fileName = file.getName();
			String newFileName = fileName.substring(0,
					fileName.lastIndexOf("."));
			String prefix = fileName.substring(fileName.lastIndexOf("."));
			file.renameTo(new File(path, newFileName + "-"
					+ DateTime.now().toString("yyMMddHHmmss") + prefix));
		}
	}
}
