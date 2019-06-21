package com.tgl.redis.migrate.domain.model.data;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="MongoInput")
public class MongoInput {
	
	private String key;
	
	private String dataKey;
	
	private String value;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getDatakey() {
		return dataKey;
	}

	public void setDatakey(String datakey) {
		this.dataKey = datakey;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
