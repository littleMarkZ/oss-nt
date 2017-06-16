package com.oss.company;

import com.jfinal.core.Controller;
import com.oss.model.Company;

/**
 * 企业管理
 * 
 * @author YanCunJian
 * @since 2017-01-15 
 */
public class CompanyController extends Controller {
	
	public void update() throws Exception {
		int id = getParaToInt(0);

		Company company = Company.dao.findById(id);
		setAttr("company", company);

		render("/oss/company/update.html");
	}
}
