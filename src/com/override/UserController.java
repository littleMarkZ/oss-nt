package com.override;

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
import com.eova.common.utils.EncryptUtil;
import com.eova.common.utils.xx;
import com.eova.config.EovaConst;
import com.eova.model.EovaLog;
import com.eova.model.MetaField;
import com.eova.model.MetaObject;
import com.eova.service.sm;
import com.eova.template.common.util.TemplateUtil;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Record;

public class UserController extends Controller {

	final Controller ctrl = this;
	
	/** 元对象业务拦截器 **/
	protected MetaObjectIntercept intercept = null;
	
	/** 异常信息 **/
	private String errorInfo = "";
	
	/** 初始化密码 **/
	public void initpwd() throws Exception {
		String objectCode = getPara(0);
		final MetaObject object = sm.meta.getMeta(objectCode);

		String json = getPara("rows");

		final List<Record> records = getRecordsByJson(json, object.getFields(), object.getPk());

		intercept = TemplateUtil.initIntercept(object.getBizIntercept());
		// 事务
		boolean flag = Db.tx(new IAtom() {
			@Override
			public boolean run() throws SQLException {
				try {
					for (Record record : records) {

						AopContext ac = new AopContext(ctrl, record);

						// 更新前置任务
						if (intercept != null) {
							String msg = intercept.updateBefore(ac);
							if (!xx.isEmpty(msg)) {
								errorInfo = msg;
								return false;
							}
						}
						String pk = object.getPk();
						String pkValue = record.get(pk).toString();
						// 根据主键更新对象
						if (!xx.isEmpty(object.getTable())) {
							record.set("login_pwd", EncryptUtil.getSM32(EovaConst.DEFAULT_PWD));
							Db.use(object.getDs()).update(object.getTable(), pk, record);
						}
						EovaLog.dao.info(ctrl, EovaLog.UPDATE, object.getStr("code") + "[" + pkValue + "]");
						// 更新后置任务
						if (intercept != null) {
							String msg = intercept.updateAfter(ac);
							if (!xx.isEmpty(msg)) {
								errorInfo = msg;
								return false;
							}
						}
					}
				} catch (Exception e) {
					errorInfo = TemplateUtil.buildException(e);
					return false;
				}
				return true;
			}
		});

		if (!flag) {
			renderJson(new Easy("更新失败" + errorInfo));
			return;
		}

		// 更新成功之后
		if (intercept != null) {
			try {
				AopContext ac = new AopContext(this, records);
				String msg = intercept.updateSucceed(ac);
				if (!xx.isEmpty(msg)) {
					errorInfo = msg;
				}
			} catch (Exception e) {
				errorInfo = TemplateUtil.buildException(e);
				renderJson(new Easy("更新成功,updateSucceed执行异常!" + errorInfo));
				return;
			}
		}

		if (!xx.isEmpty(errorInfo)) {
			renderJson(new Easy(errorInfo));
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
