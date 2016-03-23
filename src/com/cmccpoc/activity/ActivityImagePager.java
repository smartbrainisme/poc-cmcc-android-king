package com.cmccpoc.activity;

import java.util.List;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import com.cmccpoc.R;
import com.cmccpoc.adapter.AdapterImagePager;



public class ActivityImagePager extends Activity 
{
	ViewPager pager;

	
	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setContentView( R.layout.activity_image_pager);
		pager = (ViewPager) findViewById(R.id.pager);
		bundle = this.getIntent().getExtras();
		if (bundle != null)
		{
			List<String> imageUrls = bundle.getStringArrayList("images");
			int pagerPosition = bundle.getInt("position", 0);
			pager.setAdapter(new AdapterImagePager(imageUrls, this));
			pager.setCurrentItem(pagerPosition);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		// TODO Auto-generated method stub
		this.finish();
		return super.onTouchEvent(event);
	}

}
