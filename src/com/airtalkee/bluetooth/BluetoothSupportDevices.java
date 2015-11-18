package com.airtalkee.bluetooth;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.InflateException;
import com.airtalkee.R;
import com.airtalkee.sdk.util.Log;

public class BluetoothSupportDevices
{
	private static final String XML_MODELS = "menu";
	private static final String XML_ITEM = "item";
	private  Context context ;
	private Map<String, KeyHolder> mapDevices = new HashMap<String, KeyHolder>();
	private BluetoothSupportDevices(){}
	private static BluetoothSupportDevices instance;
	
	public static BluetoothSupportDevices getInstance()
	{
		if(instance == null)
		{
			instance = new BluetoothSupportDevices();
		}
		return instance;
	}
	
	public void init(Context context)
	{
		this.context = context;
		inflateConfigXml();
	}
	
	public KeyHolder getSupportDevice(BluetoothDevice  device)
	{
		if(device != null)
		{
			String deviceName = device.getName();
			if(TextUtils.isEmpty(deviceName))
				return null;
			String address = device.getAddress().replace(":", "");
			Log.i(BluetoothSupportDevices.class, "bluetooth: getSupportDevice deviceName="+device.getName()+"address="+address);
			if(device.getName().contains(address))
			{
				int index = device.getName().indexOf(address);
				deviceName = device.getName().substring(0, index);
			}
			Log.i(BluetoothSupportDevices.class, "bluetooth: getSupportDevice deviceName1="+deviceName);
			return mapDevices.get(deviceName);
		}
		return null;
	}
	
	public boolean isContainsDevice(BluetoothDevice device)
	{
		if(device == null) return false; 
		String deviceName = device.getName();
		if(deviceName == null) return false;
		String address = device.getAddress().replace(":", "");
		if(device.getName().contains(address))
		{
			int index = device.getName().indexOf(address);
			deviceName = device.getName().substring(0, index);
		}
		return mapDevices.containsKey(deviceName);
	}
	
	class KeyHolder
	{
		String deviceName = "";
		String keyP = "";
		String keyR = "";
		String ackSpeakTalk="";
		String ackSpeakRelease="";
		boolean isEncrypt =false;
		boolean onlySpp = false;

	}

	private void inflateConfigXml()
	{
		XmlResourceParser parser = null;
		try
		{
			parser = context.getResources().getLayout(R.menu.bt_config);
			AttributeSet attrs = Xml.asAttributeSet(parser);
			parseXml(parser, attrs);
		}
		catch (XmlPullParserException e)
		{
			throw new InflateException("Error inflating menu XML", e);
		}
		catch (IOException e)
		{
			throw new InflateException("Error inflating menu XML", e);
		}
		finally
		{
			if (parser != null)
				parser.close();
		}
	}

	private void parseXml(XmlResourceParser parser, AttributeSet attrs) throws XmlPullParserException, IOException
	{
		int eventType = parser.getEventType();
		String tagName;
		boolean lookingForEndOfUnknownTag = false;
		String unknownTagName = null;
		// This loop will skip to the menu start tag
		do
		{
			if (eventType == XmlPullParser.START_TAG)
			{
				tagName = parser.getName();
				if (tagName.equals(XML_MODELS))
				{
					// Go to next tag
					eventType = parser.next();
					break;
				}

				throw new RuntimeException("Expecting menu, got " + tagName);
			}
			eventType = parser.next();
		}
		while (eventType != XmlPullParser.END_DOCUMENT);

		boolean reachedEndOfMenu = false;
		while (!reachedEndOfMenu)
		{
			switch (eventType)
			{
				case XmlPullParser.START_TAG:
					if (lookingForEndOfUnknownTag)
					{
						break;
					}

					tagName = parser.getName();
					if (tagName.equals(XML_ITEM))
					{
						parseXmlItem(attrs);
					}
					else
					{
						lookingForEndOfUnknownTag = true;
						unknownTagName = tagName;
					}
					break;
				case XmlPullParser.END_TAG:

					tagName = parser.getName();
					if (lookingForEndOfUnknownTag && tagName.equals(unknownTagName))
					{
						lookingForEndOfUnknownTag = false;
						unknownTagName = null;
					}
					else if (tagName.equals(XML_MODELS))
					{
						reachedEndOfMenu = true;
					}
					break;

				case XmlPullParser.END_DOCUMENT:
					throw new RuntimeException("Unexpected end of document");
			}

			eventType = parser.next();
		}
	}

	private void parseXmlItem(AttributeSet attrs)
	{
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BtConfig);
		if (a != null)
		{
			KeyHolder holder = new KeyHolder();
			holder.deviceName = a.getString(R.styleable.BtConfig_deviceName);
			holder.keyP = a.getString(R.styleable.BtConfig_keyP);
			holder.keyR = a.getString(R.styleable.BtConfig_keyR);
			holder.ackSpeakTalk = a.getString(R.styleable.BtConfig_ackSpeakTalk);
			holder.ackSpeakRelease = a.getString(R.styleable.BtConfig_ackSpeakRelease);
			holder.isEncrypt =a.getBoolean(R.styleable.BtConfig_encrypt, false);
			holder.onlySpp = a.getBoolean(R.styleable.BtConfig_onlyspp, false);

			mapDevices.put(holder.deviceName, holder);
		}
	}
	
}
