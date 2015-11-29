package com.jikexueyuan.mobile.address.widget;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.Keep;
import android.support.v7.internal.widget.TintTypedArray;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.jikexueyuan.mobile.address.R;


/**
 * Simple progress bar with infinite rotating animation;<br>
 * Support tint color;
 * <ul>
 * <li> <b>progress_src</b>: the simplest Drawable, a PNG or JPEG image.
 * <li> <b>progress_tint</b>: the custom color for tint, default to {@link Color#WHITE}
 * </ul>
 * Created by Chaobin Wu on 15/9/14.
 */
public class TintProgressBar extends View {
    Drawable mDrawable;
    Bitmap mBitmap;
    Matrix mMatrix = new Matrix();
    int mDx;
    int mDy;
    @Keep
    float mRotatedDegree = 0;
    Paint mPaint;
    int mDrawableWidth;
    int mDrawableHeight;
    ObjectAnimator mAnimator;
    boolean mRefresh = false;

    public TintProgressBar(Context context) {
        this(context, null);
    }

    public TintProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TintProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TintTypedArray array = TintTypedArray.obtainStyledAttributes(context, attrs, R.styleable
                .TintProgressBar, defStyleAttr, 0);
        mDrawable = array.getDrawable(R.styleable.TintProgressBar_progress_src);
        int tint = array.getColor(R.styleable.TintProgressBar_progress_tint, Color.WHITE);
        mDrawable.mutate().setColorFilter(tint, PorterDuff.Mode.SRC_IN);
        array.recycle();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setFilterBitmap(true);
        mAnimator = ObjectAnimator.ofFloat(this, mRotationProperty, 0, 720);
        mAnimator.setDuration(3000);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAnimator.setInterpolator(new LinearInterpolator());
    }

    public void setRefreshing(boolean refresh) {
        mRefresh = refresh;
        if (refresh) {
            mAnimator.removeAllUpdateListeners();
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (getVisibility() == VISIBLE && mRefresh) {
                        invalidate();
                    }
                }
            });
            mAnimator.start();
        } else {
            mAnimator.end();
            mAnimator.removeAllUpdateListeners();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mRefresh) {
            setRefreshing(true);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        canvas.translate(mDx, mDy);
        mMatrix.setRotate(mRotatedDegree, getWidth() / 2, getHeight() / 2);
        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap, mMatrix, mPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        prepareBitmap();
    }

    private void prepareBitmap() {
        mDrawableWidth = mDrawable.getIntrinsicWidth();
        mDrawableHeight = mDrawable.getIntrinsicHeight();
        if (mBitmap == null) {
            final int width = mDrawableWidth > 0 ? mDrawableWidth : getMeasuredWidth();
            final int height = mDrawableHeight > 0 ? mDrawableHeight : getMeasuredHeight();
            mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
            Canvas bitmapCanvas = new Canvas(mBitmap);
            mDrawable.setBounds(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
            mDrawable.draw(bitmapCanvas);
            mDx = Math.abs((getMeasuredWidth() - mDrawableWidth > 0 ? mDrawableWidth : 0) / 2);
            mDy = Math.abs((getMeasuredHeight() - mDrawableHeight > 0 ? mDrawableHeight : 0) / 2);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAnimator.end();
        mAnimator.removeAllUpdateListeners();
    }

    public float getRotatedDegree() {
        return mRotatedDegree;
    }

    public void setRotatedDegree(float mRotatedDegree) {
        this.mRotatedDegree = mRotatedDegree;
    }

    Property<TintProgressBar, Float> mRotationProperty = new Property<TintProgressBar, Float>(Float.class,
            "mRotatedDegree") {
        @Override
        public Float get(TintProgressBar object) {
            return object.getRotatedDegree();
        }

        @Override
        public void set(TintProgressBar object, Float value) {
            object.setRotatedDegree(value);
        }
    };
}