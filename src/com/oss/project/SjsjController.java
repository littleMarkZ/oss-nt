package com.oss.project;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.eova.aop.AopContext;
import com.eova.aop.MetaObjectIntercept;
import com.eova.common.Easy;
import com.eova.common.utils.xx;
import com.eova.model.EovaLog;
import com.eova.model.MetaField;
import com.eova.model.MetaObject;
import com.eova.service.sm;
import com.eova.template.common.util.TemplateUtil;
import com.eova.widget.WidgetManager;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Record;
import com.oss.model.Company;
import com.oss.model.Project;
import com.oss.model.ProjectFj;
import com.oss.model.ProjectSjsj;

public class SjsjController extends Controller {

	final Controller ctrl = this;

	/** 自定义拦截器 **/
	protected MetaObjectIntercept intercept = null;

	/** 异常信息 **/
	private String errorInfo = "";

	public void add() throws Exception {
		String objectCode = this.getPara(0);
		MetaObject object = sm.meta.getMeta(objectCode);
		// 构建关联参数值
		WidgetManager.buildRef(this, object);
		intercept = TemplateUtil.initIntercept(object.getBizIntercept());
		if (intercept != null) {
			AopContext ac = new AopContext(ctrl, object);
			intercept.addInit(ac);
		}

		setAttr("object", object);
		setAttr("fields", object.getFields());
		render("/oss/project/sjsj/add.html");
	}

	public void update() throws Exception {
		int id = getParaToInt(0);

		ProjectSjsj sjsj = ProjectSjsj.dao.findById(id);
		setAttr("sjsj", sjsj);
		render("/oss/project/sjsj/update.html");
	}

	public void detail() throws Exception {
		int id = getParaToInt(0);

		ProjectSjsj sjsj = ProjectSjsj.dao.findById(id);
		setAttr("sjsj", sjsj);
		render("/oss/project/sjsj/detail.html");
	}
	
	public void doCheck() throws Exception {
		String objectCode = getPara(0);
		final MetaObject object = sm.meta.getMeta(objectCode);

		String json = getPara("rows");

		final List<Record> records = getRecordsByJson(json, object.getFields(), object.getPk());
		// 事务
		boolean flag = Db.tx(new IAtom() {
			@Override
			public boolean run() throws SQLException {
				try {
					for (Record record : records) {
						Project project = Project.dao.findById(record.get(object.getPk()));
						Company company = Company.dao.findById(project.getInt("company_id"));
						project.set("is_fqsh", "Y");
						ProjectSjsj.dao.doCheck(ctrl, project.getInt("id"), company.getStr("name"), project.getStr("name") + "审核表", project.getStr("bgwh"));
						project.update();
					}
				} catch (Exception e) {
					errorInfo = TemplateUtil.buildException(e);
					return false;
				}
				return true;
			}
		});
		
		// AOP提示信息
		if (!flag) {
			renderJson(new Easy(errorInfo));
			return;
		}

		if (!flag) {
			renderJson(new Easy("发起审计失败" + errorInfo));
			return;
		}

		renderJson(new Easy());
	}
	
	/**
	 * json转List
	 * 
	 * @param json
	 * @param pkName
	 * @return
	 */
	private static List<Record> getRecordsByJson(String json, List<MetaField> items, String pkName) {
		List<Record> records = new ArrayList<Record>();

		List<JSONObject> list = JSON.parseArray(json, JSONObject.class);
		for (JSONObject o : list) {
			Map<String, Object> map = JSON.parseObject(o + "", new TypeReference<Map<String, Object>>() {
			});
			Record re = new Record();
			re.setColumns(map);
			// 将Text翻译成Value,然后删除val字段
			for (MetaField x : items) {
				String en = x.getEn();// 字段名
				String exp = x.getStr("exp");// 表达式
				Object value = re.get(en);// 值

				if (!xx.isEmpty(exp)) {
					String valField = en + "_val";
					// 获取值列中的值
					value = re.get(valField).toString();
					// 获得值之后删除值列防止持久化报错
					re.remove(valField);
				}

				re.set(en, TemplateUtil.convertValue(x, value));
			}
			// 删除主键备份值列
			re.remove("pk_val");
			// 删除Orcle分页产生的rownum_
			if (xx.isOracle()) {
				re.remove("rownum_");
			}
			records.add(re);
		}

		return records;
	}
}
