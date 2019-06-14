package com.tgl.redis.migrate.inf.net;

import org.junit.runner.RunWith;

import com.tgl.redis.migrate.domain.inf.service.RedisConnection;
import com.tgl.redis.migrate.domain.model.data.SingleSourceData;

import net.serenitybdd.junit.runners.SerenityRunner;
import net.thucydides.core.annotations.Step;
import net.thucydides.core.annotations.Steps;
import static org.junit.Assert.*;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.junit.Test;

@RunWith(SerenityRunner.class)
public class RedisConnectionImplTest {
	
	@Steps
	private TaskStep taskStep;
	
	
	@Test
	public void testSync()
	{
		//Given
		//taskStep.initial("10.1.75.167", 6379);
		taskStep.initial("10.1.75.224", 6400);
		//when
		taskStep.sync();
		//then
		taskStep.stop();
	}
	
	
	public static class TaskStep{
		
		private RedisConnection connection;
		
		private SingleSourceData singleSourceData;
		
		@Step("initial connection configuration ip:{0},port:{1}")
		public void initial(String ip,Integer port)
		{
			connection = new RedisConnectionImpl();
			singleSourceData = new SingleSourceData(ip,port,connection);
			
			
		}
		
		@Step("start sync")
		public void sync()
		{
			Executor executor = Executors.newSingleThreadExecutor();
			
			executor.execute(new Runnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					connection.sync(singleSourceData);
				}
				
			});
			
			
		}
		
		
		@Step("stop sync")
		public void stop()
		{
			try {
				Thread.sleep(3600*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//connection.stop();
		}
	}

}
