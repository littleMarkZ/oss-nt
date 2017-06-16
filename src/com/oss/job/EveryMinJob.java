package com.oss.job;

import org.quartz.JobExecutionContext;

import com.oss.model.QuartzTx;

/**
 * 每分钟执行
 *
 * @author Jieven
 * @date 2014-7-7
 */
public class EveryMinJob extends AbsJob {

	@Override
	protected void process(JobExecutionContext context) {
		QuartzTx.dao.attach("主题", "内容", 1);
	}
}
