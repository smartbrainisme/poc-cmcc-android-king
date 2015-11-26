package com.airtalkee.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class AudioVisualizerView extends View
{
	private byte[] mBytes = null;
	private float[] mPoints;
	private Rect mRect = new Rect();

	private Paint mForePaint = new Paint();

	private int mVisualizerSpectrumNum = 10;

	public AudioVisualizerView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		mForePaint.setAntiAlias(true);
		mForePaint.setColor(Color.rgb(255, 189, 0));
	}

	public void setSpectrumNum(int SpectrumNum)
	{
		mVisualizerSpectrumNum = SpectrumNum;
	}

	public void updateVisualizer(byte[] fft)
	{
		mBytes = fft;
		mForePaint.setStrokeWidth((float)getWidth() / (float)(mVisualizerSpectrumNum * 2));
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);

		if (mBytes == null)
		{
			return;
		}

		if (mPoints == null || mPoints.length < mBytes.length * 4)
		{
			mPoints = new float[mBytes.length * 4];
		}

		mRect.set(0, 0, getWidth(), getHeight());

		final int baseX = mRect.width() / mVisualizerSpectrumNum;
		final int height = mRect.height();

		for (int i = 0; i < mVisualizerSpectrumNum; i++)
		{
			if (mBytes[i] < 0)
			{
				mBytes[i] = 127;
			}

			final int xi = baseX * i + baseX / 2;

			mPoints[i * 4] = xi;
			mPoints[i * 4 + 1] = height;

			mPoints[i * 4 + 2] = xi;
			mPoints[i * 4 + 3] = height - mBytes[i];
		}

		canvas.drawLines(mPoints, mForePaint);
	}

}
