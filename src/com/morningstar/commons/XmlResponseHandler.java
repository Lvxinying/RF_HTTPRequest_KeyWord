package com.morningstar.commons;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmlResponseHandler {
//因为DOM4J与W3C的DOM用法差别比较大，所以必须定义两个	
	static org.dom4j.Document documentDom4J = null;
	static org.w3c.dom.Document documentW3C = null;
	private static final Logger log = Logger.getLogger(XmlResponseHandler.class);

	public static List<String> getNodeValueList(String nodeName, InputStream ip){
		org.w3c.dom.Document doc =  readStreamW3C(ip);
		NodeList readerNodes = doc.getElementsByTagName(nodeName);
		List<String> list = new ArrayList<String>();
		for(int i=0;i<readerNodes.getLength();i++){
			org.w3c.dom.Element responseNode = (org.w3c.dom.Element) readerNodes.item(i);
			org.w3c.dom.Node node = responseNode.getFirstChild();
			list.add(node.getNodeValue());
		}
		return list;
	}
	
	public static List<String> getNodeAttributesValueList(String nodeName,String attributeName,InputStream ip){
		List<String> attributeValueList = new LinkedList<String>();
		org.w3c.dom.Document doc =  readStreamW3C(ip);
		NodeList readerNodes = doc.getElementsByTagName(nodeName);
		for(int i=0;i<readerNodes.getLength();i++){
			org.w3c.dom.Element responseNode = (org.w3c.dom.Element) readerNodes.item(i);
			attributeValueList.add(responseNode.getAttribute(attributeName));
		}
		if(attributeValueList.isEmpty()){
			log.warn("The assighned node and attribute contain no value!");
		}
		return attributeValueList;
	}
	
	public static String printXMLResponse(InputStream ip){
		Document document = readStreamDom4J(ip);
		Element rootElement = document.getRootElement();
		String xmlResponse = rootElement.asXML();
		return xmlResponse;
	}
	
	public static String getValueFromInputStream(String xPath,InputStream ip) {
		Document document = readStreamDom4J(ip);
		XPath xpath = null;
		if (document == null)
			return null;
		xpath = document.createXPath(xPath);
		Node node = xpath.selectSingleNode(document);
		if (node == null)
			return null;
		return node.getText();
	}
	
//Document 为W3C发布的
	private static org.w3c.dom.Document readStreamW3C(InputStream ip){
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder;
			try {
				docBuilder = docBuilderFactory.newDocumentBuilder();
				documentW3C = docBuilder.parse(ip);
				ip.close();
			} catch (ParserConfigurationException | SAXException | IOException e) {
				e.printStackTrace();
			}
			return documentW3C;	
	}
	
//Document 为Dom4J发布的	
	private static  org.dom4j.Document readStreamDom4J(InputStream in) {
		if (in == null)
			return null;
		SAXReader saxReader = new SAXReader();
		try {
			documentDom4J = saxReader.read(in);
		} catch (DocumentException e) {
			log.warn(e.getMessage());
			e.printStackTrace();
		}
		return documentDom4J;
	}

	@SuppressWarnings("unchecked")
	public static int getNodeCount(String xPath,InputStream ip) {
		Document document = readStreamDom4J(ip);
		List<String> list = new ArrayList<String>();
		if (document == null)
			return 0;
		list = document.selectNodes(xPath);
		return list.size();
	}

	public static String getDataWithNewNameSpace(String xpath) {
		if (documentDom4J == null)
			return null;

		Map<String, String> names = generateNamespaceURI(documentDom4J);
		XPath x = documentDom4J.createXPath(xpath);
		x.setNamespaceURIs(names);
		Node node = x.selectSingleNode(documentDom4J);
		if (node == null)
			return null;
		return node.getText();
	}

	public static Map<String, String> generateNamespaceURI(Document document) {
		Element root = document.getRootElement();
		String uri = root.getNamespaceURI();
		Map<String, String> names = new HashMap<String, String>();
		names.put("uri", uri);
		return names;
	}
	
	
	public static String getAttributeValue(String XPath, String item)
	{
		String result = "";
		Element element = (Element) documentDom4J.selectSingleNode(XPath);
		result = element.attributeValue(item);
		return result;
	}
	
	
	public static Document getLocalXMLDocument(String filePath)
	{
		Document doc = null;
		SAXReader xmlReader = new SAXReader();
		try
        {			
			doc = xmlReader.read(filePath);
        }catch(Exception e)
        {
        	log.warn("Read XML Document error " + filePath + "\t" + e.getMessage());
        }
		return doc;
	}
}
