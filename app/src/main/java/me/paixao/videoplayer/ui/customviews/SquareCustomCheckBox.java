package me.paixao.videoplayer.ui.customviews;

import android.content.Context;
import android.util.AttributeSet;

public class SquareCustomCheckBox extends android.support.v7.widget.AppCompatCheckBox {
    public SquareCustomCheckBox(Context context) {
        super(context);
    }

    public SquareCustomCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareCustomCheckBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth()); //Snap to width
    }
}
