package com.tgl.redis.migrate.domain.model.data;

/**
 * redis 地址
 * @author liuwei1
 *
 */
public class AddressVO {
	
	private String ip;
	
	private Integer port;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

}
