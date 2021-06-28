package com.simon.utils;

public class IdUtil {

	private static IdGenerator idGenerator = new SnowflakeIdGenerator(1);

	public static synchronized Long getId() {
		return idGenerator.nextId();
	}

	public static synchronized String getStringId() {
		return idGenerator.nextId().toString();
	}

}
