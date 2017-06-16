package com.oss.model;

import com.eova.common.base.BaseModel;

public class QuartzTx extends BaseModel<QuartzTx> {

	private static final long serialVersionUID = 8066416060670334217L;
	
	public static final QuartzTx dao = new QuartzTx();
	
	public void attach(String subject, String content, long todoerID) {
		QuartzTx tx = new QuartzTx();
		tx.set("subject", subject);
		tx.set("content", content);
		tx.set("todoer_id", todoerID);
		tx.save();
	}

}
