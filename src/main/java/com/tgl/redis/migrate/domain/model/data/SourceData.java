package com.tgl.redis.migrate.domain.model.data;
/**
 * 源数据
 * @author liuwei1
 *
 */
public abstract class SourceData {
	
	private String id;
	
	private String name;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
