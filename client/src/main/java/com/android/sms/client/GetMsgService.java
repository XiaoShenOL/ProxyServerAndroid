package com.android.sms.client;

/**
 * @author zyq 16-3-31
 */
public class GetMsgService extends BaseHeartService<GetMsgRunnable> {


	@Override
	public void onRunnableDestroy() {

	}

	@Override
	public void onRunnableInit() {
        t = new GetMsgRunnable(this);
	}
}
