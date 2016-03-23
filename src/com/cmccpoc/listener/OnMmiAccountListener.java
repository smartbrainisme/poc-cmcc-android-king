package com.cmccpoc.listener;

/**
 * 监听用户登录状态
 * @author Yao
 */
public interface OnMmiAccountListener
{
    public void onMmiHeartbeatLogin(int result);

	public void onMmiHeartbeatLogout();

	public void onMmiHeartbeatException(int result);
}
