package com.morningstar.commons;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Logger;
import org.robotframework.javalib.annotation.ArgumentNames;
import org.robotframework.javalib.annotation.RobotKeyword;
import org.robotframework.javalib.annotation.RobotKeywords;


import com.morningstar.commons.HttpRequestException.*;

@RobotKeywords
public class HttpRequestLibrary extends ResourceManager{
	public static final String ROBOT_LIBRARY_SCOPE = "GLOBAL";
	public static final String ROBOT_LIBRARY_VERSION = "0.0.1";
	HttpURLConnection connection;
	static String currentRunPath = System.getProperty("user.dir");
	String configPath = currentRunPath+"/Config/ResourceBundle.properties";
//	String configPath = "./ResourceBundle.properties";
	String url;
	String cookie;
	int responseCode;
	int contentLength;
	String responseContentEncodingType;
	String responseHeaderField;
	Properties prop = null;
	InputStream inputStream;
	Logger log = Logger.getLogger(HttpRequestLibrary.class);
	
	@RobotKeyword()
	@ArgumentNames({"envInfo"})
	public void connectToHttpServer(String envInfo) throws CustomerizedException{
		switch(envInfo){
		case "NotificationSystemQA":
			this.url = getResourceInfo("getNotificationSysUrlQAEnv");
			System.out.println("REQUESTED URL ===> " + this.url);
			if(this.url.isEmpty()){
				new CustomerizedException("Getting notificationsystem url failed or you haven't set a url!");
				log.error("Getting notificationsystem url failed or you haven't set a url!");
			}
			this.cookie = getResourceInfo("getNotificationSysCookieQAEnv");
			System.out.println("REQUESTED COOKIE ===> " + this.cookie);
			if(this.cookie == null){
				new CustomerizedException("Getting notificationsystem cookie failed or you!");
				log.error("Getting notificationsystem cookie failed!");
			}
			try {
				getConnection("GET",url,cookie);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
			
		case "DataAPI_DEMO":
//	此连接暂时不需要设置cookie		
			this.url = getResourceInfo("getDataAPIV2UrlQAEnv");
			System.out.println("REQUESTED URL ===> " + this.url);
			if(this.url.isEmpty()){
				new CustomerizedException("Getting DataAPIV2 url failed or you haven't set a url!");
				log.error("Getting DataAPIV2 url failed or you haven't set a url!");
			}
			try {
				getConnectionDataAPI(this.url);
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;			
		}		
	}
	
	@RobotKeyword()
	public void shutDownTheCurrentHttpConnection(){
		if(this.connection != null){
			this.connection.disconnect();			
			log.info("Current HTTP Connection has closed!");			
		}
		this.connection = null;
	}
	
	@RobotKeyword()
	public int getHttpServerResponseCode(){
		if(this.connection != null){
			try {
				this.responseCode = this.connection.getResponseCode();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return this.responseCode;
	}
	
	@RobotKeyword()
	public String getHttpResponseContentLength(){
		if(this.connection != null){
			this.contentLength = this.connection.getContentLength();
		}
		return String.valueOf(this.contentLength)+"Bytes";
	}
	
	@RobotKeyword()
	public String getHttpResponseContentEncodingType(){
		if(this.connection != null){
			this.responseContentEncodingType = this.connection.getContentEncoding();
		}
		return this.responseContentEncodingType;
	}
	
	@RobotKeyword()
	public void showHttpResponseHeaderField(){
		if(this.connection != null){
			Map<String, List<String>> map = new HashMap<String,List<String>>();
			map = this.connection.getHeaderFields();
			for(Entry<String, List<String>> entry:map.entrySet()){
				System.out.println(entry.getKey()+"--->"+entry.getValue());
			}
		}
	}

//Response Handle(XML Format)
	@RobotKeyword()
	public String showFullXMLResponse(){
		String fullXML = null;
		InputStream ip = getHttpResponseInputstream();
		try {
			if(ip != null && ip.available()>0){
				fullXML = XmlResponseHandler.printXMLResponse(ip);
			}
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return fullXML;
	}
	
	@RobotKeyword()
	@ArgumentNames({"xPath"})
//	似乎xpath的这种方式还有点问题
	public String usingXpathGetXMLResponseNodeValue(String xPath){
		InputStream ip = getHttpResponseInputstream();
		String nodeValue = null;
		try {
			if(ip != null && ip.available()>0){
				nodeValue = XmlResponseHandler.getValueFromInputStream(xPath,ip);
			}
		} catch (IOException e) {
			log.error(e.getMessage());
		}		
		return nodeValue;
	}
	
	@RobotKeyword()
	@ArgumentNames({"xPath"})
	public int usingXpathGetXMLResponseNodeCount(String xPath){
		InputStream ip = getHttpResponseInputstream();
		int nodeCount = 0;
		try {
			if(ip != null && ip.available()>0){
				nodeCount = XmlResponseHandler.getNodeCount(xPath, ip);
			}
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return nodeCount;
	}
	
	@RobotKeyword()
	@ArgumentNames({"nodeName"})
	public List<String> getNodeValueListByUsingNodeName(String nodeName){
		InputStream ip = getHttpResponseInputstream();
		List<String> valueList = new ArrayList<String>();
			try {
				if(ip != null && ip.available()>0){
					valueList = XmlResponseHandler.getNodeValueList(nodeName, ip);
				}
			} catch (IOException e) {
				log.error(e.getMessage());
			}
		return valueList;
	}
	
	@RobotKeyword()
	@ArgumentNames({"nodeName"})
	public int getNodeCountByUsingNodeName(String nodeName){
		int nodeCount = 0;
		List<String> valueList = new ArrayList<String>();
		valueList = getNodeValueListByUsingNodeName(nodeName);
		if(valueList != null && !valueList.isEmpty()){
			nodeCount = valueList.size();
		}
		if(valueList != null){
			valueList.clear();	
		}						
		return nodeCount;
	}
	
	@RobotKeyword()
	@ArgumentNames({"nodeName","attributeName"})
	public List<String> getNodeAttributeValueListByUsingNodeNameAndAttributeName(String nodeName,String attributeName){
		List<String> attributeValueList = new LinkedList<String>();
		InputStream ip = getHttpResponseInputstream();
		attributeValueList = XmlResponseHandler.getNodeAttributesValueList(nodeName, attributeName, ip);
		return attributeValueList;
	}
	
//Response Handle(JASON Format)
	@RobotKeyword()
	public void showFullJSONResponse() throws IOException, CustomerizedException{
		InputStream ip = getHttpResponseInputstream();
		if(ip != null){
			JsonResponseHandler.printJSONResponse(ip);
		}
	}
	
	@RobotKeyword()
	public String getFullResponseJSONString() throws IOException, CustomerizedException{
		String resposeJSONStr = null;
		InputStream ip = getHttpResponseInputstream();
		if(ip != null){
			resposeJSONStr = JsonResponseHandler.getJSONResponse(ip);
		}
		if(resposeJSONStr == null){
			throw new CustomerizedException("Get Response JSON String meet trouble!");
		}else if(resposeJSONStr.isEmpty()){
			log.warn("Response JSON String is empty!");
		}
		return resposeJSONStr;
	}
	
	@RobotKeyword()
	@ArgumentNames({"KeyName"})
	public String getValueByUsingKeyName(String KeyName) throws IOException, CustomerizedException{
		String resultStr = null;
		String fullResponseJsonStr = null;
		Map<String,String> resultMap = new HashMap<String,String>();
		fullResponseJsonStr = getFullResponseJSONString();
		if(fullResponseJsonStr != null){
			resultMap = JsonResponseHandler.getAllKeyValuePair(fullResponseJsonStr);
		}		
		resultStr = resultMap.get(KeyName).toString();
		return resultStr;
	}
	
	@RobotKeyword()
	public Map<String,String> getAllKeyValuePairMapFromFullResponseJsonString() throws IOException, CustomerizedException{
		long startTime = System.currentTimeMillis();
		Map<String,String> map = new HashMap<String,String>();
		String fullResponseJsonStr = null;
		fullResponseJsonStr = getFullResponseJSONString();
		if(fullResponseJsonStr != null){
			map = JsonResponseHandler.getAllKeyValuePair(fullResponseJsonStr);
		}
		long endTime = System.currentTimeMillis() - startTime;
		log.info("Json Key<->Value pair has built up,total cost: "+endTime+" ms");
		return map;
	}
	
	@RobotKeyword()
	@ArgumentNames({"KeyName"})
	public List<String> getValueListFromFullResponseJsonStringByUsingKeyName(String KeyName) throws IOException, CustomerizedException{
		List<String> resultList = new ArrayList<String>();
		Map<String,String> map = new HashMap<String,String>();
		map = getAllKeyValuePairMapFromFullResponseJsonString();
		if(!map.isEmpty()){
			resultList = JsonResponseHandler.getValue2ListByUsingKeyName(KeyName, map);
		}else{
			log.warn("Sorry,we don't get any Key<->Value pairs in current response JSON string!");
		}
		return resultList;
	}
	
	@RobotKeyword()
	@ArgumentNames({"filePath","jsonFileName"})
	public void writeServerResponsedJsonString(String filePath,String jsonFileName) throws IOException, CustomerizedException{
		String jsonStr = null;
		jsonStr = getFullResponseJSONString();
		if(jsonStr != null){
			JsonResponseHandler.writePrettyJSONStream2Files(jsonStr, filePath, jsonFileName);
		}
	}
	
	@RobotKeyword()
	@ArgumentNames({"keyName"})
	public String getJsonValueSchema(String keyName) throws IOException, CustomerizedException{
		String schemaType = null;
		String jsonStr = null;
		jsonStr = getFullResponseJSONString();
		if(jsonStr != null){
			schemaType = JsonResponseHandler.getJSONSchema(jsonStr, keyName);
		}
		return schemaType;
	}
	
	@RobotKeyword()
	@ArgumentNames({"currentKeyName"})
	public boolean isChildJsonKey(String parentKeyName,String childKeyName) throws IOException, CustomerizedException{
		String jsonStr = null;
		boolean isChild = false;
		jsonStr = getFullResponseJSONString();
		if(jsonStr != null){
			isChild = JsonResponseHandler.getChildJsonNode(jsonStr, parentKeyName, childKeyName);
		}
		return isChild;
	}
		
	@Override
	public Properties getProperties(String configFilePath) throws CustomerizedException {
		try {
				this.prop = loadPropertiesFile(new File(configFilePath));
			if(this.prop ==  null){	
				log.error("Load ResourceBundle.properties file meet trouble!");
				throw new CustomerizedException("Load ResourceBundle.properties file meet trouble!");
			}			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this.prop;
	}
	
	@Override
	public String getResourceInfo(String methodName) throws CustomerizedException {
		String str = null;
		if(methodName.equalsIgnoreCase("getNotificationSysUrlQAEnv")){
			str = getNotificationSysUrlQAEnv();
			log.info("Call method--->"+methodName);
		}
		if(methodName.equalsIgnoreCase("getNotificationSysCookieQAEnv")){
			str = getNotificationSysCookieQAEnv();
			log.info("Call method--->"+methodName);
		}
		if(methodName.equalsIgnoreCase("getDataAPIV2UrlQAEnv")){
			str = getDataAPIV2UrlQAEnv();
			log.info("Call method--->"+methodName);
		}
		return str;
	}
	
	private void setHttpConection(HttpURLConnection conn){
		this.connection = conn;
	}
	
	private Properties loadPropertiesFile(File propsFile) throws IOException {
		Properties props = new Properties();				
		FileInputStream fis = new FileInputStream(propsFile);
		props.load(fis);
		fis.close();
		return props;
	}
	
	private String getNotificationSysUrlQAEnv() throws CustomerizedException{
		String returnStr;
		Properties prop = getProperties(this.configPath);
		returnStr = prop.getProperty("NotificationSys.Site.QA").trim();		
		return returnStr;
	}
	
	private String getNotificationSysCookieQAEnv() throws CustomerizedException{
		String returnStr;
		Properties prop = getProperties(this.configPath);
		returnStr = prop.getProperty("NotificationSys.Cookie.QA");		
		return returnStr;
	}
	
	private String getDataAPIV2UrlQAEnv() throws CustomerizedException{
		String returnStr;
		Properties prop = getProperties(this.configPath);
		returnStr = prop.getProperty("DataApiV2.Site.QA").trim();		
		return returnStr;
	}
	
	private void getConnection(String requestMethodMode,String url,String cookie) throws MalformedURLException, IOException{
		setHttpConection((HttpURLConnection) (new URL(url).openConnection()));
//设置重定向自动跟踪为false		
		this.connection.setInstanceFollowRedirects(false);		
		this.connection.setRequestProperty("Accept-Encoding", "Gzip");
//切换HttpRequest方法模式(支持GET,POST,HEAD,OPTIONS,PUT,DELETE,TRACE)
		if(requestMethodMode.equalsIgnoreCase("get")){
			this.connection.setRequestMethod("GET");
		}
		else if(requestMethodMode.equalsIgnoreCase("post")){
			this.connection.setRequestMethod("POST");
		}
		else if(requestMethodMode.equalsIgnoreCase("head")){
			this.connection.setRequestMethod("HEAD");
		}
		else if(requestMethodMode.equalsIgnoreCase("OPTIONS")){
			this.connection.setRequestMethod("OPTIONS");
		}
		else if(requestMethodMode.equalsIgnoreCase("PUT")){
			this.connection.setRequestMethod("PUT");
		}
		else if(requestMethodMode.equalsIgnoreCase("DELETE")){
			this.connection.setRequestMethod("DELETE");
		}
		else if(requestMethodMode.equalsIgnoreCase("TRACE")){
			this.connection.setRequestMethod("TRACE");
		}
//设置cookie		
		if(cookie != null && !cookie.isEmpty()){
			this.connection.setRequestProperty("Cookie", cookie);
		}else{
			log.info("Your cookie is empty,we don't set any cookies in current HTTP connection!");
		}	
		if(this.connection.getResponseCode() == 200){
			log.info("Already connected to target HTTP server!");
		}else{
			log.error("Connection may meet some trouble,response status code is: "+this.connection.getResponseCode());
		}
	}
	
	private InputStream getHttpResponseInputstream(){
		InputStream ip = null;
		if(this.connection != null){
			try {
				ip = this.connection.getInputStream();
			} catch (IOException e) {
				log.error(e.getMessage());
			}
			if(this.connection != null){
				if( this.connection.getContentEncoding() != null && this.connection.getContentEncoding().equalsIgnoreCase("GZip")){					
					try {
						this.inputStream = new GZIPInputStream(ip);
					} catch (IOException e) {
						log.error(e.getMessage());
					}
				}else{
					this.inputStream = ip;
				}
			}
		}
		return this.inputStream;
	}

//专门针对此连接单独写一个方法，不需要设置cookie	
	private void getConnectionDataAPI(String url) throws MalformedURLException, IOException{
		setHttpConection((HttpURLConnection) (new URL(url).openConnection()));
//设置重定向自动跟踪为false		
		this.connection.setInstanceFollowRedirects(false);		
		this.connection.setRequestMethod("GET");
//设置DATA API V2 DEMO的两个特殊的Header部分
		this.connection.setRequestProperty("X-API-ProductId", "Direct");
		this.connection.setRequestProperty("X-API-UserId", "42e5613b-49a0-4366-8cf7-480d21997883");
		if(this.connection.getResponseCode() == 200){
			log.info("Already connected to target HTTP server!");
		}else{
			log.error("Connection may meet some trouble,response status code is: "+this.connection.getResponseCode());
		}
	}
	public Map<String,String> getJsonKeyValuePair(){
		Map<String,String> resultMap = new HashMap<String,String>();
		InputStream ip = getHttpResponseInputstream();
		String resposeJSONStr;
		if(ip != null){
			try {
				resposeJSONStr = JsonResponseHandler.getJSONResponse(ip);
				if(resposeJSONStr != null && !resposeJSONStr.isEmpty()){
					resultMap = JsonResponseHandler.getAllKeyValuePair(resposeJSONStr);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return resultMap;
	}
	
	public static void main(String[] args) throws CustomerizedException, IOException{
		HttpRequestLibrary test = new HttpRequestLibrary();
//		test.connectToHttpServer("NotificationSystemQA");
//		System.out.println("Response Code: "+test.getHttpServerResponseCode());
//		System.out.println("Response Content Length: "+test.getHttpResponseContentLenghth());
//		System.out.println("Response Content Encoding Type: "+test.getHttpResponseContentEncodingType());
//		test.showHttpResponseHeaderField();
//		System.out.println("Full XML Response: "+test.showFullXMLResponse());
//		System.out.println(test.usingXpathGetXMLResponseNodeValue("ArrayOfDataMessage//DataGroup"));
//		System.out.println(test.usingXpathGetXMLResponseNodeCount("ArrayOfDataMessage/DataMessage/DataGroup"));
//		System.out.println(test.getNodeValueListByUsingNodeName("Sequence"));
//		System.out.println(test.getNodeCountByUsingNodeName("Sequence"));
//		System.out.println(test.getNodeAttributeValueListByUsingNodeNameAndAttributeName("Sequence", "test"));		
//		test.shutDownTheCurrentHttpConnection();
		test.connectToHttpServer("DataAPI_DEMO");
////		List<String> jsonList = new ArrayList<String>();
//		HashMap<String,String> map = new HashMap<String,String>();
//		System.out.println("Reponse=======================");
////		test.showFullJASONResponse();
////		System.out.println("JSON: "+test.getResponseJSONString());
//		/*Map<String,String> map = new HashMap<String,String>();
//		map = (HashMap<String, String>) test.getAllKeyValuePairMapFromFullResponseJsonString();
//		for(Entry<?, ?> entry:map.entrySet()){
//			System.out.println("KEY "+ entry.getKey().toString());
//			System.out.println("VALUE "+ entry.getValue().toString());
//		}
//		/*String str = test.getValueByUsingKeyName("listType");
//		System.out.println(str);*/
//		/*List<String> list = new ArrayList<String>();
//		list = test.getValueListFromFullResponseJsonStringByUsingKeyName("lastModifiedBy");
//		System.out.println(list);*/
////		System.out.println("Reponse=======================");
//		test.writeServerResponsedJsonString("C:/HJG_WORK/", "ResponsedJsonString_1.JSON");
		System.out.println(test.isChildJsonKey("attributes", "userdata"));
		test.shutDownTheCurrentHttpConnection();
	}
}
