package com.eim.utilities;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class NoScrollGridView extends GridView {
	public NoScrollGridView(Context context) {
		super(context);
	}

	public NoScrollGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NoScrollGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		if (getLayoutParams().width == LayoutParams.WRAP_CONTENT)
			widthMeasureSpec = MeasureSpec.makeMeasureSpec(
					Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);

		if (getLayoutParams().height == LayoutParams.WRAP_CONTENT)
			heightMeasureSpec = MeasureSpec.makeMeasureSpec(
					Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
		super.setOnItemClickListener(mOnItemClickListener);
	}
}
