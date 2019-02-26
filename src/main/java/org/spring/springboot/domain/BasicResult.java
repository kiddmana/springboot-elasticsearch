package org.spring.springboot.domain;

/**
 * 
 * @author wangding
 * 时间：2017年4月17日 上午11:25:49
 * 功能:基本数据基类
 */
public class BasicResult {
	private int result;
	private String msg;
	private long nowtime = System.currentTimeMillis();
	
	public long getNowtime() {
		return nowtime;
	}

	public void setNowtime(long nowtime) {
		this.nowtime = nowtime;
	}

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

}
