package com.tgl.redis.migrate.domain.model.data;

import com.mongodb.util.JSON;
import com.mongodb.DBObject;
import com.tgl.redis.migrate.domain.inf.service.MongodbConnection;

public class MongodbSourceData extends SourceData{
	
	private MongodbConnection conncection;
	
	public MongodbSourceData()
	{
		
	}
	
	public MongodbSourceData(MongodbConnection mongodbConnection)
	{
		this.conncection = mongodbConnection;
	}
	
	public boolean syncFileMsg(String msg)
	{
		DBObject bson = (DBObject)JSON.parse(msg);
		conncection.save(bson,"MongoInput");
		return true;
	}

}
