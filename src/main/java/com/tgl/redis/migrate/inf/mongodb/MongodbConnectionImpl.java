package com.tgl.redis.migrate.inf.mongodb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.mongodb.DBObject;
import com.tgl.redis.migrate.domain.inf.service.MongodbConnection;
import com.tgl.redis.migrate.domain.model.data.MongoInput;

@Component
public class MongodbConnectionImpl implements MongodbConnection{

	@Autowired
    private MongoTemplate mongoTemplate;
	
	@Override
	public boolean save(DBObject bson,String collectionName) {
		// TODO Auto-generated method stub
		
		mongoTemplate.save(bson,collectionName);
		
		return true;
	}

	

}
