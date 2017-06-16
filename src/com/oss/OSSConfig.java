/**
 * Copyright (c) 2013-2016, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.oss;

import override.com.eova.common.utils.xx;

import com.eova.config.EovaConfig;
import com.eova.interceptor.LoginInterceptor;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.plugin.spring.SpringPlugin;
import com.oss.company.CompanyController;
import com.oss.model.Company;
import com.oss.model.Hotel;
import com.oss.model.OrderItem;
import com.oss.model.Orders;
import com.oss.model.Product;
import com.oss.model.Project;
import com.oss.model.ProjectFj;
import com.oss.model.ProjectLog;
import com.oss.model.ProjectSjsj;
import com.oss.model.QuartzTx;
import com.oss.product.ProductController;
import com.oss.project.ProjectController;
import com.oss.project.SjsjController;
import com.override.UserController;

public class OSSConfig extends EovaConfig {

	/**
	 * 自定义路由
	 * 
	 * @param me
	 */
	@Override
	protected void route(Routes me) {
		// 自定义的路由配置往这里加。。。
		// Demo业务
		me.add("/product", ProductController.class);
		// Oss业务
		me.add("/company", CompanyController.class);
		me.add("/project", ProjectController.class);
		me.add("/sjsj", SjsjController.class);
		// Eova业务
		me.add("/user", UserController.class);
		
		// 不需要登录拦截的URL
		LoginInterceptor.excludes.add("/init");
	}

	/**
	 * 自定义Main数据源Model映射
	 * 
	 * @param arp
	 */
	@Override
	protected void mapping(ActiveRecordPlugin arp) {
		arp.addMapping("hotel", Hotel.class);
		arp.addMapping("product", Product.class);
		arp.addMapping("orders", Orders.class);
		arp.addMapping("order_item", OrderItem.class);
		// 自定义的Model映射往这里加。。。
	}
	
	/**
	 * 自定义Oss数据源Model映射
	 * 
	 * @param arp
	 */
	protected void mappingOss(ActiveRecordPlugin arp) {
		// 自定义的Model映射往这里加。。。
		arp.addMapping("oss_company", Company.class);
		arp.addMapping("oss_project", Project.class);
		arp.addMapping("oss_project_fj", ProjectFj.class);
		arp.addMapping("oss_project_sjsj", ProjectSjsj.class);
		arp.addMapping("oss_project_log", ProjectLog.class);
		arp.addMapping("oss_quartz_tx", QuartzTx.class);
	}

	/**
	 * 自定义插件
	 */
	@Override
	protected void plugin(Plugins plugins) {
		// 添加数据源
		String ossUrl, ossUser, ossPwd;
		
		ossUrl = props.get("oss_url");
		ossUser = props.get("oss_user");
		ossPwd = props.get("oss_pwd");
         
		{
			DruidPlugin dp = initDruidPlugin(ossUrl, ossUser, ossPwd);
			ActiveRecordPlugin arp = initActiveRecordPlugin(ossUrl, xx.DS_OSS, dp);
			System.out.println("load data source:" + ossUrl + "/" + ossUser);

			mappingOss(arp);
			
			plugins.add(dp).add(arp);
		}
		
		// 添加自动扫描插件
		
		// 添加Spring插件
		plugins.add(new SpringPlugin("classpath*:spring-ctx.xml"));
	}

}