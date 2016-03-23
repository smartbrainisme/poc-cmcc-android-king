package com.cmccpoc.Util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import com.airtalkee.sdk.entity.AirContact;

public class XMLContentHandler extends DefaultHandler
{
	private List<AirContact> users;
	private AirContact user;
	private String preTag;

	@Override
	public void startDocument() throws SAXException
	{
		users = new ArrayList<AirContact>();
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException
	{
		if (user != null)
		{
			String data = new String(ch, start, length);
			if ("ipocid".equals(preTag))
			{
				user.setIpocId(data);
			}
			else if ("pwd".equals(preTag))
			{
				user.setPwd(data);
			}
			else if ("sim".equals(preTag))
			{
				user.setType(Integer.parseInt(data));
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException
	{
		if ("user".equals(localName) && user != null)
		{
			users.add(user);
			user = null;
		}
		preTag = null; // ��һ������У�����XML��ʱ�����׳��?ע�⣡
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
	{
		if ("user".equals(localName))
		{
			user = new AirContact();

		}
		preTag = localName;
	}

	public List<AirContact> getUsers()
	{
		return users;
	}

	public static List<AirContact> readXML(InputStream inStream) throws Exception
	{
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser saxParser = spf.newSAXParser(); // ����������
		// ���ý�������������ԣ�http://xml.org/sax/features/namespaces = true
		// ��ʾ��������ռ�����
		// saxParser.setProperty("http://xml.org/sax/features/namespaces",
		// true);
		XMLContentHandler handler = new XMLContentHandler();
		saxParser.parse(inStream, handler);
		inStream.close();
		return handler.getUsers();
	}
}
