package com.cmccpoc.activity.home.adapter;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.airtalkee.sdk.util.Log;
import com.cmccpoc.R;
import com.cmccpoc.Util.Util;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

/**
 * 自定义基础适配器
 * @author Yao
 */
public abstract class AdapterBase extends BaseAdapter
{
	public static final int ORIENTATION_VERTICAL = 0;
	public static final int ORIENTATION_HORIZONTAL = 1;
	public static final int ORIENTATION_SQUARE = 2;

	protected interface OnImageLoadCompletedListener
	{
		/**
		 * 当图片加载完成时
		 * @param orientation 图片方向
		 * @param v view
		 * @param width 宽
		 * @param height 高
		 */
		public void onImageLoadCompleted(int orientation, View v, int width, int height);
	}

	DisplayImageOptions options;
	protected ImageLoader imageLoader = null;

	public AdapterBase()
	{
		// TODO Auto-generated constructor stub
		imageLoader = ImageLoader.getInstance();
		options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.image_default).showImageForEmptyUri(R.drawable.image_default).showImageOnFail(R.drawable.image_default).imageScaleType(ImageScaleType.EXACTLY).cacheInMemory(true).cacheOnDisc(true).displayer(new RoundedBitmapDisplayer(0)).considerExifParams(true).build();
	}

	/**
	 * 展示图片
	 * @param photoId 图片Id
	 * @param iv 图片View
	 */
	public void displayImage(String photoId, ImageView iv)
	{
		imageLoader.displayImage(Util.getPhotoUrl(photoId), iv, options, null);
	}

	/**
	 *  展示Url图片
	 * @param Url Url地址
	 * @param iv 图片View
	 * @param listener 
	 */
	public void displayImageByUrl(String Url, ImageView iv, final OnImageLoadCompletedListener listener)
	{
		imageLoader.displayImage(Url, iv, options, new ImageLoadingListener()
		{
			@Override
			public void onLoadingStarted(String imageUri, View view)
			{
				// TODO Auto-generated method stub
			}

			@Override
			public void onLoadingFailed(String imageUri, View view, FailReason failReason)
			{
				// TODO Auto-generated method stub
			}

			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
			{
				// 图片加载完成后，重新设置图片方向与大小
				if (listener != null)
				{
					if (loadedImage != null)
					{
						int orientation = ORIENTATION_SQUARE;
						int width = loadedImage.getWidth();
						int height = loadedImage.getHeight();
						if (width > height)
						{
							orientation = ORIENTATION_HORIZONTAL;
						}
						else if (width == height)
						{
							orientation = ORIENTATION_SQUARE;
						}
						else
						{
							orientation = ORIENTATION_VERTICAL;
						}

						Log.i(AdapterBase.class, "onLoadingComplete width" + loadedImage.getWidth() + "height" + loadedImage.getHeight());
						listener.onImageLoadCompleted(orientation, view, width, height);
					}
				}

			}

			@Override
			public void onLoadingCancelled(String imageUri, View view)
			{
				// TODO Auto-generated method stub
			}
		});
	}

}
