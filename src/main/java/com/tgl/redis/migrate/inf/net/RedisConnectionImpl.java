package com.tgl.redis.migrate.inf.net;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tgl.redis.migrate.domain.inf.service.RedisConnection;
import com.tgl.redis.migrate.domain.model.data.SingleSourceData;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import net.whitbeck.rdbparser.*;

public class RedisConnectionImpl implements RedisConnection{
	
	private final Logger logger = LogManager.getLogger(RedisConnectionImpl.class);

	private Object lock;
	
	@Override
	public void sync(SingleSourceData singleSourceData) {
		// TODO Auto-generated method stub
		
		lock = new Object();
		
		File outFile = new File("D:\\learning\\leaning-repository\\redis-migrate-tools\\target\\translate.rdb");
		
		if(outFile.exists())
		{
			outFile.delete();
			try {
				outFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		NioEventLoopGroup work = new NioEventLoopGroup();

		final InputHandler inputHandler = new InputHandler(singleSourceData,outFile);
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(work).channel(NioSocketChannel.class)
		.handler(new ChannelInitializer<SocketChannel>() {

			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				// TODO Auto-generated method stub
				ch.pipeline().addLast(inputHandler);
			}

		});
		
		try {
			
			String ip = singleSourceData.getAddress().getIp();
			Integer port = singleSourceData.getAddress().getPort();
			
			ChannelFuture future = bootstrap.connect(ip, port).sync();
			
			logger.error("begin sync ==========================");
			
			inputHandler.sendMsg("SYNC");
			
			synchronized (lock) {
				lock.wait();
			}

			
		} catch (Exception e) {

		}
		
	}
	
	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
		synchronized (lock) {
			lock.notifyAll();
		}
	}
	
	public static class InputHandler extends SimpleChannelInboundHandler<ByteBuf>
	{
		private final Logger logger = LogManager.getLogger(InputHandler.class);
		
		private Channel channel;
		
		private SingleSourceData singleSourceData;
		
		private File outFile;
		
		private boolean startSync;
		
		public InputHandler(SingleSourceData singleSourceData,File outFile)
		{
			this.singleSourceData = singleSourceData;
			this.outFile = outFile;
			this.startSync = false;
		}

		@Override
		protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
			// TODO Auto-generated method stub	
			String result = msg.toString(CharsetUtil.UTF_8);
			boolean isNum = false;
			if(result.length()>2)
			{
				int index = 0;
				boolean end = false;
				index = result.indexOf("$");
				boolean start = Pattern.matches("[0-9]",String.valueOf(result.charAt(index+1)));
				index = result.indexOf("\r\n");
				if (index > 0) {
					end = Pattern.matches("[0-9]", String.valueOf(result.charAt(index - 1)));
				}
				
				if(start && end)
				{
					isNum = true;
				}
				
				
			}
			
			if ((result.startsWith("*") || result.startsWith("$"))&& isNum && !result.contains("REDIS")) {
				logger.error("Client begin received\n" + result);
				if (startSync) {
					startSync=false;
					
					try {
						decodeRDB(outFile);
					} catch (Exception e) {
						logger.error("decode error" + e.getStackTrace().toString());
					}
					
				}
				singleSourceData.receiveMsg(result);
				logger.info("Client received:\n" + result);
			} else {
				if (result.contains("REDIS") || startSync) {
					logger.info("begin sync");

					FileOutputStream out = new FileOutputStream(outFile, true);

					byte[] content = new byte[msg.readableBytes()];

					msg.readBytes(content);

					byte[] fileBytes = content;
					if (!startSync) {
						int index = result.indexOf("\n");
						fileBytes = Arrays.copyOfRange(content, index+1, content.length);
					}

					out.write(fileBytes);
					out.flush();

					out.close();

					startSync = true;
				}
			}
		}

		@Override
		public void channelActive(ChannelHandlerContext ctx) {
			
			channel = ctx.channel();
			//channel.writeAndFlush(Unpooled.copiedBuffer("Netty rocks!", CharsetUtil.UTF_8));
		}
		
		public void sendMsg(String msg)
		{
			String [] args = {};
			BinaryCommand cmd = new BinaryCommand(msg,args);
			
			//channel.attr(AttributeKey.valueOf("resp_callback")).set(null);
			
			ByteBuf msgSYNC = RedisProtocol.generateRequest(cmd, channel.alloc());
			
			channel.writeAndFlush(msgSYNC);
		}
		
		public void decodeRDB(File file) throws Exception
		{
			logger.error("begin decode===============");
			try (RdbParser parser = new RdbParser(file)) {

				Entry e;
				while ((e = parser.readNext()) != null) {/*
					
					if(EntryType.KEY_VALUE_PAIR.equals(e.getType()))
					{
						long wasteTime = System.currentTimeMillis();
						logger.info("begin decode msg");
						
						KeyValuePair kvp = (KeyValuePair) e;
						StringBuilder strbuilder = new StringBuilder();

						strbuilder.append("{ key:");
						strbuilder.append(new String(kvp.getKey(), "utf8") + ",");

						if (kvp.hasExpiry()) {
							strbuilder.append("expire:" + kvp.getExpiryMillis() + ",");
						}
						
						strbuilder.append("values:");
						
						for (byte[] val : kvp.getValues()) {
							//System.out.print(new String(val, "ASCII") + " ");
							strbuilder.append(new String(val, "utf8") + " ");
							
						}
						
						strbuilder.append("}");

						String msg = strbuilder.toString();
						
						singleSourceData.syncFileMsg(msg);
						
						
						logger.info("end decode msg wasteTime:"+(System.currentTimeMillis() - wasteTime));
						

					}
				*/}

			}
			
			logger.error("end decode===============");

		
		}

	}

	

}
