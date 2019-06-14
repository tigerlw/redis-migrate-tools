package com.tgl.redis.migrate.domain.inf.service;

import com.tgl.redis.migrate.domain.model.data.SingleSourceData;

public interface RedisConnection {
	
	public void sync(SingleSourceData singleSourceData);
	
	public void stop();

}
