package com.morningstar.commons.HttpRequestException;

public class CustomerizedException extends Exception{
	private static final long serialVersionUID = 1L;
	
	public CustomerizedException(String message){
		super(message);
	}
	
	public CustomerizedException(Throwable cause){
		super(cause);
	}
}
