package com.tgl.redis.migrate.inf.net;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
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

public class RedisConnectionImpl implements RedisConnection {

	private final Logger logger = LogManager.getLogger(RedisConnectionImpl.class);

	private Object lock;

	@Override
	public void sync(SingleSourceData singleSourceData) {
		// TODO Auto-generated method stub

		lock = new Object();

		File outFile = new File("D:\\learning\\leaning-repository\\redis-migrate-tools\\target\\translate.rdb");

		if (outFile.exists()) {
			outFile.delete();
			try {
				outFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		NioEventLoopGroup work = new NioEventLoopGroup();

		final InputHandler inputHandler = new InputHandler(singleSourceData, outFile);
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(work).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {

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

	public static class InputHandler extends SimpleChannelInboundHandler<ByteBuf> {
		private final Logger logger = LogManager.getLogger(InputHandler.class);

		private Channel channel;

		private SingleSourceData singleSourceData;

		private File outFile;

		private boolean startSync;

		private boolean beginFlag;

		private Long fileSize;

		private Long readSize;
		
		private AtomicLong readRecord ;
		private AtomicLong handleCount ;
		
		private ThreadPoolExecutor executor;

		public InputHandler(SingleSourceData singleSourceData, File outFile) {
			this.singleSourceData = singleSourceData;
			this.outFile = outFile;
			this.startSync = false;
			beginFlag = false;
			readSize = 0L;
			fileSize = 0L;
			readRecord = new AtomicLong(0L);
			handleCount = new AtomicLong(0L);
			
			executor = new ThreadPoolExecutor(20, 20,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>());
		}

		@Override
		protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
			// TODO Auto-generated method stub
			synchronized (this) {
				logger.info("read byte lenght:" + msg.readableBytes());

				String result = msg.toString(CharsetUtil.UTF_8);

				if (!startSync) {
					if (result.startsWith("$")) {
						startSync = true;
						beginFlag = true;
						int index = result.indexOf("\n");
						try {
							fileSize = Long.valueOf(result.substring(1, index - 1)) + 20;
						} catch (Exception e) {
							logger.error(e.getMessage());
						}

					} else {
						logger.info("sync no begin ============== " + result);
					}

				}

				if (startSync) {
					if (fileSize > readSize) {

						System.out.println("begin sync");

						Long leftSize = fileSize - readSize;

						Integer readLen = leftSize < msg.readableBytes() ? leftSize.intValue() : msg.readableBytes();

						byte[] content = new byte[readLen];

						msg.readBytes(content);

						readSize = readSize + readLen;

						byte[] fileBytes = content;

						if (beginFlag) {
							int index = result.indexOf("\n");
							fileBytes = Arrays.copyOfRange(content, index + 1, content.length);
							beginFlag = false;
						}

						try {

							FileOutputStream out = new FileOutputStream(outFile, true);

							out.write(fileBytes);
							out.flush();

							out.close();
						} catch (Exception e) {
							logger.error(e.getMessage());
						}

						logger.info("=================read Size:" + readSize);
						logger.info("=================fileSize:" + fileSize);

						if (fileSize <= readSize) {
							try
							{
							decodeRDB(outFile);
							}catch(Exception e)
							{
								logger.error(e.getMessage());
							}
						}

					} else {
						logger.info("Client received:\n" + result);
						singleSourceData.receiveMsg(result);
					}

				}

			}
		}

		@Override
		public void channelActive(ChannelHandlerContext ctx) {

			channel = ctx.channel();
			// channel.writeAndFlush(Unpooled.copiedBuffer("Netty rocks!",
			// CharsetUtil.UTF_8));
		}

		public void sendMsg(String msg) {
			String[] args = {};
			BinaryCommand cmd = new BinaryCommand(msg, args);

			// channel.attr(AttributeKey.valueOf("resp_callback")).set(null);

			ByteBuf msgSYNC = RedisProtocol.generateRequest(cmd, channel.alloc());

			channel.writeAndFlush(msgSYNC);
		}

		public void decodeRDB(File file) throws Exception {
			logger.error("begin decode===============");
			
			long wasteTime = System.currentTimeMillis();
			
			try (RdbParser parser = new RdbParser(file)) {

				Entry e;

				try {
					while ((e = parser.readNext()) != null) {

						try {
							if (EntryType.KEY_VALUE_PAIR.equals(e.getType())) {
								// long wasteTime = System.currentTimeMillis();
								// logger.info("begin decode msg");

								KeyValuePair kvp = (KeyValuePair) e;

								String keyType = kvp.getValueType().toString();

								long count = 0;
								

								String datakey = "";

								for (byte[] val : kvp.getValues()) {

									StringBuilder strbuilder = new StringBuilder();
									if (count % 2 == 1) {

										strbuilder.append("{ key:");
										strbuilder.append("\""+new String(kvp.getKey(), "utf8") + "\",");

										if (kvp.hasExpiry()) {
											strbuilder.append("expire:" + kvp.getExpiryMillis() + ",");
										}

										strbuilder.append("datakey:\"" + datakey + "\",");

										strbuilder.append("value:");

										strbuilder.append(new String(val, "utf8"));

										strbuilder.append("}");

										String msg = strbuilder.toString();
										handleRdbRecord(msg);

									} else {
										datakey = new String(val, "utf8");

										if ("VALUE".equals(keyType)) {
											strbuilder.append("{ key:");
											strbuilder.append("\""+new String(kvp.getKey(), "utf8") + "\",");

											if (kvp.hasExpiry()) {
												strbuilder.append("expire:" + kvp.getExpiryMillis() + ",");
											}
											strbuilder.append("value:" + datakey);

											strbuilder.append("}");
											handleRdbRecord(strbuilder.toString());
										}
									}

									count++;

								}

							}

						} catch (Exception el) {
							logger.error(el.getMessage());
						}

					}
				} catch (Exception el) {
					logger.error(el.getMessage());
				}
				
				
				while(readRecord.get() > handleCount.get())
				{
					Thread.sleep(2000);
				}

				
				/*logger.error("end decode=============== readRecord:" + readRecord.get() + ";handleRecord:"
						+ handleCount.get() +";wasteTime:"+(System.currentTimeMillis() - wasteTime));*/
				
				
				logger.error("end decode=============== readRecord:" + readRecord.get() + ";handleRecord:"
						+ handleCount.get() +";wasteTime:"+(System.currentTimeMillis() - wasteTime));

			}

		}
		
		
		public void handleRdbRecord(String msg)
		{
			
			long taskCount = executor.getQueue().size();
			
			if(taskCount>50000)
			{
				try {
					Thread.sleep(1000);
					System.out.println("too much task; count:" + taskCount);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			readRecord.incrementAndGet();
			
			executor.execute(new Runnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					try{
					singleSourceData.syncFileMsg(msg);
					}catch(Exception e)
					{
						logger.error(e.getMessage());
					}
					
					handleCount.incrementAndGet();
				}
				
			});
			
		}
		
		

	}

}
