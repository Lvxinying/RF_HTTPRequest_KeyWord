package com.morningstar.commons;

import java.util.Properties;

import com.morningstar.commons.HttpRequestException.CustomerizedException;

public abstract class ResourceManager {
	public abstract Properties getProperties(String configFilePath) throws CustomerizedException;
	public abstract String getResourceInfo(String methodName) throws CustomerizedException;
}
