package com.tgl.redis.migrate.domain.inf.service;

import com.mongodb.DBObject;
import com.tgl.redis.migrate.domain.model.data.MongoInput;

public interface MongodbConnection {
	
	public boolean save(DBObject bson,String collectionName);

}
