package com.airtalkee.Util;

import com.airtalkee.sdk.util.PicFactory;

public class Const
{
	public static final class image_select
	{
		/**
		 * startActivityForResult的request的类型。<br>
		 * 浏览本地图片做头像。
		 */
		public static final int REQUEST_CODE_BROWSE_IMAGE = 111;
		public static final int REQUEST_CODE_BROWSE_VIDEO = 112;

		/**
		 * startActivityForResult的request的类型。<br>
		 * 拍照做头像。
		 */
		public static final int REQUEST_CODE_CREATE_IMAGE = 121;
		public static final int REQUEST_CODE_CREATE_VIDEO = 122;

		/**
		 * mine type
		 */
		public static final int REQUEST_CODE_GET_CROP = 113;
		public static final int REQUEST_CODE_UPLOAD = 114;
		public static final int REQUEST_EXCEPTION = 115;
		public static final int LIST_DIALOG_ITEM_BROWSE_IMAGE = 0;
		public static final int LIST_DIALOG_ITEM_CREATE_IMAGE = 1;
		public static final int PHOTO_BOUNDARY_SIZE = PicFactory.HEAD_WIDTH;

	}

}
