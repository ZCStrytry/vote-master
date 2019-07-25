package com.jeecg.p3.system.enums;

import java.util.LinkedList;
import java.util.List;

public enum UserTypeEnum {
	
	
	ROSE("0","总代"),
	AGENT("1","代理"),
	GATHER("2","采编"),
	CUSTOMER("3","客服");
	
	/**
	 * 编码
	 */
    private String code;
    
    /**
     * 描述
     */
    private String value;
    
    private UserTypeEnum(String code, String value) {
        this.code = code;
        this.value = value;
    }
	
    public static String getUserTypeByCode(String code) {
		for(UserTypeEnum item : UserTypeEnum.values()) {
			if(item.getCode().equals(code)) {
				return item.value;
			}
		}
		return null;
	}
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public static List<Enum<UserTypeEnum>> getAllTypeList(){
		List<Enum<UserTypeEnum>> list = new LinkedList<Enum<UserTypeEnum>>();
		list.add(0, ROSE);
		list.add(1, AGENT);
		list.add(2, GATHER);
		list.add(3, CUSTOMER);
		return list;
	}
	
}
