package com.airtalkee.Util;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.InflateException;
import android.view.KeyEvent;
import android.view.View;

import com.airtalkee.R;
import com.airtalkee.config.Config;
import com.airtalkee.sdk.util.Log;

public class XmlModelReader
{
	private static final String XML_MODELS = "menu";
	private static final String XML_ITEM = "item";
	private Context mContext = null;

	public XmlModelReader(Context context)
	{
		this.mContext = context;
		
	}

	public void inflate(int menuRes)
	{
		Config.model = android.os.Build.MODEL;
		Log.i(XmlModelReader.class, "PhoneModel=" + Config.model);
		XmlResourceParser parser = null;
		try
		{
			parser = mContext.getResources().getLayout(menuRes);
			AttributeSet attrs = Xml.asAttributeSet(parser);
			parseMenu(parser, attrs);
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

	/**
	 * Called internally to fill the given menu. If a sub menu is seen, it will
	 * call this recursively.
	 */
	private void parseMenu(XmlPullParser parser, AttributeSet attrs) throws XmlPullParserException, IOException
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
						parseItem(attrs, mContext);
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

	public void parseItem(AttributeSet attrs, Context context)
	{
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PttConfig);
		String _model = a.getString(R.styleable.PttConfig_model);
		if (_model.equals(Config.model))
		{
			Config.pttButtonAction = a.getString(R.styleable.PttConfig_pttButtonAction);
			Config.pttButtonActionUpDownCode = a.getString(R.styleable.PttConfig_pttButtonActionUpDownCode);
			Config.pttButtonActionUp = a.getString(R.styleable.PttConfig_pttButtonActionUp);
			Config.pttButtonActionDown = a.getString(R.styleable.PttConfig_pttButtonActionDown);
			Config.pttButtonKeycode = a.getInteger(R.styleable.PttConfig_pttButtonKeycode, 0);
			Config.pttButtonVisibility = a.getBoolean(R.styleable.PttConfig_pttVisible, true) ? View.VISIBLE : View.GONE;
			Config.pttEarphoneLongPress = a.getBoolean(R.styleable.PttConfig_pttEarphoneLongPress, false);
			Config.pttEarphoneKeycode = a.getInteger(R.styleable.PttConfig_pttEarphoneKeycode, KeyEvent.KEYCODE_HEADSETHOOK);
			Config.pttEarphonePlug = a.getBoolean(R.styleable.PttConfig_pttEarphonePlug, false);
			Config.screenOrientation = a.getInteger(R.styleable.PttConfig_screenOrientation, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			Config.screenAlwaysPtt = a.getBoolean(R.styleable.PttConfig_screenAlwaysPtt, false);
			Config.audioAmplifierEnabled = a.getBoolean(R.styleable.PttConfig_audioAmplifierEnabled, false);
			Config.funcTTS =a.getBoolean(R.styleable.PttConfig_tts, false);
			Config.funcBootLaunch = a.getBoolean(R.styleable.PttConfig_bootLaunch, false);
			Config.funcAllowFirstLaunch = a.getBoolean(R.styleable.PttConfig_allowFirstLaunch, true);
		}
		a.recycle();
	}
}
