package com.jeecg.p3.system.util;

import java.util.UUID;

public class UUIDUtil {

	public static String genUUid() {
		String s = UUID.randomUUID().toString();
		return s.substring(0, 8) + s.substring(9, 13) + s.substring(14, 18) + s.substring(19, 23) + s.substring(24);
	}
}
