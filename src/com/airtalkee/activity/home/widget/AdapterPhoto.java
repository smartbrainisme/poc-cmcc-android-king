package com.airtalkee.activity.home.widget;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import com.airtalkee.R;
import com.airtalkee.Util.Bimp;
import com.airtalkee.Util.BitmapCache;
import com.airtalkee.Util.BitmapCache.ImageCallback;
import com.airtalkee.entity.ImageItem;

public class AdapterPhoto extends BaseAdapter
{
	final String TAG = getClass().getSimpleName();
	private static final int TYPE_REPORT = 1;
	private static final int TYPE_IM = 2;
	Context mContext;
	private int type = TYPE_REPORT;
	List<ImageItem> dataList;// 图片列表
	public Map<String, String> map = new HashMap<String, String>();
	BitmapCache bitampCache;
	private Handler mHandler;
	private int selectTotal = 0;
	private int lastImagePosition = -1;// 记录上一次图片标志位，-1为空
	private Holder lastImageHolder;// 记录上一次图片标志holder
	private TextCallback textcallback = null;

	public static interface TextCallback
	{
		public void onTextListen(int count);
	}

	ImageCallback callback = new ImageCallback()
	{
		@Override
		public void imageLoad(ImageView imageView, Bitmap bitmap, Object... params)
		{
			if (imageView != null && bitmap != null)
			{
				String url = (String) params[0];
				if (url != null && url.equals((String) imageView.getTag()))
				{
					((ImageView) imageView).setImageBitmap(bitmap);
				}
				else
				{
					Log.e(TAG, "callback, bmp not match");
				}
			}
			else
			{
				Log.e(TAG, "callback, bmp null");
			}
		}
	};

	public void setTextCallback(TextCallback listener)
	{
		textcallback = listener;
	}

	public AdapterPhoto(Context mContext, List<ImageItem> list, Handler mHandler)
	{
		this.mContext = mContext;
		dataList = list;
		bitampCache = new BitmapCache();
		this.mHandler = mHandler;
	}

	public AdapterPhoto(Context mContext, List<ImageItem> list, int type, Handler mHandler)
	{
		this(mContext, list, mHandler);
		this.type = type;
	}

	@Override
	public int getCount()
	{
		int count = 0;
		if (null != dataList)
		{
			count = dataList.size();
		}
		return count;
	}

	@Override
	public Object getItem(int position)
	{
		return dataList.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		// TODO Auto-generated method stub
		return position;
	}

	@SuppressWarnings("null")
	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		final Holder holder;
		if (convertView == null)
		{
			holder = new Holder();
			convertView = View.inflate(mContext, R.layout.listitem_photo, null);
			// convertView =View.inflate(mContext,R.layout.listitem_photo,null);
			holder.ivPhoto = (ImageView) convertView.findViewById(R.id.iv_album_picture);
			holder.cbSelected = (CheckBox) convertView.findViewById(R.id.cb_album_picture);
			holder.cbSelected.setClickable(false);
			convertView.setTag(holder);
		}
		else
		{
			holder = (Holder) convertView.getTag();
		}
		final ImageItem item = (ImageItem) getItem(position);
		holder.ivPhoto.setTag(item.imagePath);
		bitampCache.displayBmp(holder.ivPhoto, item.thumbnailPath, item.imagePath, callback);
		switch (type)
		{
			case TYPE_IM:
			{
				holder.cbSelected.setVisibility(View.VISIBLE);
				break;
			}
			case TYPE_REPORT:
			{
				holder.cbSelected.setVisibility(View.GONE);
				break;
			}
		}
		holder.ivPhoto.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				imageClick(position, holder, item);
			}
		});
		return convertView;
	}

	class Holder
	{
		private ImageView ivPhoto;
		private CheckBox cbSelected;
	}

	private void imageClick(final int position, final Holder holder, final ImageItem item)
	{
		String path = dataList.get(position).imagePath;
		item.isSelected = !item.isSelected;
		if (type == TYPE_IM)
		{
			// 若果是IM消息，则可以多选图片，最多9张。
			if ((Bimp.bmp.size() + selectTotal) < 9)
			{
				if (item.isSelected)
				{
					holder.cbSelected.setChecked(true);
					selectTotal++;
					if (textcallback != null)
						textcallback.onTextListen(selectTotal);
					map.put(path, path);

				}
				else if (!item.isSelected)
				{
					holder.cbSelected.setChecked(false);
					selectTotal--;
					if (textcallback != null)
						textcallback.onTextListen(selectTotal);
					map.remove(path);
				}
			}
			else if ((Bimp.bmp.size() + selectTotal) >= 9)
			{
				if (item.isSelected == true)
				{
					item.isSelected = !item.isSelected;
					selectTotal--;
					map.remove(path);

				}
				else
				{
					Message message = Message.obtain(mHandler, 0);
					message.sendToTarget();
				}
			}
		}
		else if (type == TYPE_REPORT)
		{
			if (item.isSelected) // 当前为选中状态
			{
				if (lastImagePosition != -1)
				{
					// TODO:如果不是-1，则先获取上一个holder，并将checkbox置为false
					lastImageHolder.cbSelected.setChecked(false);
					lastImageHolder.cbSelected.setVisibility(View.GONE);
					ImageItem lastItem = (ImageItem) getItem(lastImagePosition);
					lastItem.isSelected = !lastItem.isSelected;
					selectTotal = 0;
					map.clear();
				}
				holder.cbSelected.setVisibility(View.VISIBLE);
				holder.cbSelected.setChecked(true);
				selectTotal++;
				map.put(path, path);
				// 记录新的图片标记
				lastImageHolder = holder;
				lastImagePosition = position;
			}
			else
			{
				holder.cbSelected.setVisibility(View.GONE);
				holder.cbSelected.setChecked(false);
				selectTotal = 0;
				map.clear();
				// 图片标记置空
				lastImageHolder = null;
				lastImagePosition = -1;
			}
		}
	}

}
