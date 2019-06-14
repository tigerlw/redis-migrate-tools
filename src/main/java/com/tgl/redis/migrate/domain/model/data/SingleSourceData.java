package com.tgl.redis.migrate.domain.model.data;

import com.tgl.redis.migrate.domain.inf.service.RedisConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 单点redis 数据源
 * @author liuwei1
 *
 */
public class SingleSourceData extends SourceData{
	
	private final Logger logger = LogManager.getLogger(SingleSourceData.class);
	
	private AddressVO address;
	
	private RedisConnection connection;
	
	public SingleSourceData()
	{
		
	}
	
	public SingleSourceData(String ip,Integer port,RedisConnection connection)
	{
		AddressVO address = new AddressVO();
		address.setIp(ip);
		address.setPort(port);
		
		this.address = address;
		this.connection = connection;
	}
	
	public void sync()
	{
		connection.sync(this);
	}
	
	public boolean receiveMsg(String msg)
	{
		logger.error("receiveMsg==============" + msg);
		return true;
	}
	
	public boolean syncFileMsg(String msg)
	{
		logger.info("syncFileMsg ===========================" + msg);
		return true;
	}

	public AddressVO getAddress() {
		return address;
	}

	public void setAddress(AddressVO address) {
		this.address = address;
	}

}
