package cn.xuetang.common.shiro.realm;

import org.apache.shiro.authc.UsernamePasswordToken;

public class CaptchaUsernamePasswordToken extends UsernamePasswordToken {

	private static final long serialVersionUID = 4676958151524148623L;
	private String captcha;

	public String getCaptcha() {
		return captcha;
	}

	public void setCaptcha(String captcha) {
		this.captcha = captcha;
	}

	public CaptchaUsernamePasswordToken(String username, String password, boolean rememberMe, String host, String captcha) {
		super(username, password, rememberMe, host);
		this.captcha = captcha;
	}
}
