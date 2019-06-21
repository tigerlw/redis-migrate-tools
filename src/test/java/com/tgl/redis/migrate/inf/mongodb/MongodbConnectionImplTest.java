package com.tgl.redis.migrate.inf.mongodb;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.tgl.redis.migrate.ApplicationTest;
import com.tgl.redis.migrate.domain.inf.service.MongodbConnection;
import com.tgl.redis.migrate.domain.model.data.MongoInput;

import net.thucydides.core.annotations.Step;
import net.thucydides.core.annotations.Steps;

public class MongodbConnectionImplTest extends ApplicationTest{
	
	@Autowired 
	private MongodbConnection mongodbConnection;
	
	@Steps
	private ConnectStep connectStep;
	
	@Test
	public void testMongoConnection()
	{
		//given
		connectStep.initialConnection();
		//when
		connectStep.saveData("222", "123", "data", mongodbConnection);
		//then
		connectStep.saveSuccess();
	}
	
	
	public static class ConnectStep
	{
		@Step("initial connection")
		public void initialConnection()
		{
			
		}
		
		@Step("save data key:{0},dataKey:{1},value:{2}")
		public void saveData(String key,String dataKey,String value,MongodbConnection mongodbConnection)
		{
			StringBuilder builder = new StringBuilder();
			builder.append("{key:\""+key+"\",");
			builder.append("dataKey:\""+dataKey+"\",");
			builder.append("value:"+dataKey+"}");
			
			DBObject bson = (DBObject)JSON.parse(builder.toString());
			
			mongodbConnection.save(bson,"MongoInput");
			
		}
		
		@Step("save success")
		public void saveSuccess()
		{
			
		}
	}
	

}
