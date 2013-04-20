package cs5625.deferred.datastruct;


/**
 * Maybe.java
 * 
 * Meant for lookups that can fail
 * If isValid is false, then data is undefined.  
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2012, Computer Science Department, Cornell University.
 * 
 * @author Rohit Garg (rg534)
 * @date 2012-03-23
 */

public class Maybe<T> {
	private T data;
	private boolean isValid;
	
	public Maybe()
		{
		this.isValid = false;
		}
	
	public Maybe( T data_)
		{
		this.data = data_;
		this.isValid = true;
		}
	
	public T getData()
	{
		return this.data;
	}
	
	public boolean hasData()
	{
		return this.isValid;
	}

}
