package com.example.mygriddemo.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;


public class MyGridView extends GridView {

    //坐标
    private int mDownX, mDownY;
    private int mPointTop, mPointLeft;
    private int mOffsetX, mOffsetY;
    private int mUpBorder, mDownBorder;
    private int mTouchX, mTouchY;
    private int mStatusHeight;
    //当前view
    private int mSelectIndex;
    private int mCurrentPosition;
    private int mDragStartPosition;
    private View mSelctView;
    //事件
    private Runnable mLongRunnable;
    private Runnable mScrollRunnable;
    private Handler mHandler;
    private boolean isDrag;
    private onChangeListener mOnChangeListener;
    //镜像
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowLayoutParams;
    private ImageView mDragImageView;
    private Bitmap mDragView;
    //其他常量
    private static final long LONG_TOUCH_TIME = 800;
    private static final int SCROLL_SPEED = 20;


    public MyGridView(Context context) {
        this(context, null);
    }

    public MyGridView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyGridView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public int getCurrentPosition() {
        return mCurrentPosition;
    }

    private void init() {
        mHandler = new Handler();

        mLongRunnable = new Runnable() {
            @Override
            public void run() {
                isDrag = true;
                mSelctView.setVisibility(View.INVISIBLE);
                creatDragImage(mDragView, mDownX, mDownY);
            }
        };

        mScrollRunnable = new Runnable() {
            @Override
            public void run() {
                mHandler.removeCallbacks(mLongRunnable);
                int scrollY;
                if (mTouchY > mUpBorder) {
                    scrollY = SCROLL_SPEED;
                    mHandler.postDelayed(mScrollRunnable, 25);
                } else if (mTouchY < mDownBorder) {
                    scrollY = -SCROLL_SPEED;
                    mHandler.postDelayed(mScrollRunnable, 25);
                } else {
                    scrollY = 0;
                    mHandler.removeCallbacks(mScrollRunnable);
                }

                onSwapItem(mTouchX, mTouchY);

                smoothScrollBy(scrollY, 10);
            }
        };

        mStatusHeight = getStatusHeight(getContext());

        mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);

        //为区别position = 0情况，设置缺省值为-1
        mCurrentPosition = -1;
    }

    /**
     * 状态栏高度
     *
     * @param context
     * @return
     */
    private int getStatusHeight(Context context) {
        int statusHeight = 0;
        Rect localRect = new Rect();
        ((Activity) context).getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);
        statusHeight = localRect.top;
        if (0 == statusHeight) {
            Class<?> localClass;
            try {
                localClass = Class.forName("com.android.internal.R$dimen");
                Object localObject = localClass.newInstance();
                int i5 = Integer.parseInt(localClass.getField("status_bar_height").get(localObject).toString());
                statusHeight = context.getResources().getDimensionPixelSize(i5);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return statusHeight;
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = (int) ev.getX();
                mDownY = (int) ev.getY();

                mSelectIndex = pointToPosition(mDownX, mDownY);
                if (mSelectIndex != AdapterView.INVALID_POSITION) {
                    mHandler.postDelayed(mLongRunnable, LONG_TOUCH_TIME);
                    mSelctView = getChildAt(mSelectIndex - getFirstVisiblePosition());
                    //记录一次拖拽流程中真正交换的起始item position
                    mDragStartPosition = mSelectIndex;

                    mPointTop = mDownY - mSelctView.getTop();
                    mPointLeft = mDownX - mSelctView.getLeft();

                    mOffsetX = (int) (ev.getRawX() - mDownX);
                    mOffsetY = (int) (ev.getRawY() - mDownY);

                    mUpBorder = getHeight() * 3 / 4;
                    mDownBorder = getHeight() / 4;

                    mSelctView.setDrawingCacheEnabled(true);
                    mDragView = Bitmap.createBitmap(mSelctView.getDrawingCache());
                    mSelctView.destroyDrawingCache();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int moveX = (int) ev.getX();
                int moveY = (int) ev.getY();

                //若手指移除当前view区域或gridview滚动则取消长按事件
                if (!isTouchInItem(mSelctView, moveX, moveY) || Math.abs(moveY - mDownY) > 100) {
                    mHandler.removeCallbacks(mLongRunnable);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mHandler.removeCallbacks(mLongRunnable);
                mHandler.removeCallbacks(mScrollRunnable);
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 判断手指是否在dragView区域内
     * @param dragView
     * @param x
     * @param y
     * @return
     */
    private boolean isTouchInItem(View dragView, int x, int y) {
        if (dragView == null) {
            return false;
        }

        int left = dragView.getLeft();
        int top = dragView.getTop();
        if (x < left || x > left + dragView.getWidth()) {
            return false;
        }

        return !(y < top || y > top + dragView.getHeight());
    }

    private void creatDragImage(Bitmap bitmap, int downX, int downY) {
        mWindowLayoutParams = new WindowManager.LayoutParams();
        //图片之外的其他地方透明
        mWindowLayoutParams.format = PixelFormat.TRANSLUCENT;

        mWindowLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        mWindowLayoutParams.x = downX - mPointLeft + mOffsetX;
        mWindowLayoutParams.y = downY - mPointTop + mOffsetY - mStatusHeight;
        mWindowLayoutParams.alpha = 0.55f; //透明度
        mWindowLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

        mDragImageView = new ImageView(getContext());
        mDragImageView.setImageBitmap(bitmap);
        mWindowManager.addView(mDragImageView, mWindowLayoutParams);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isDrag && mDragImageView != null) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    mTouchX = (int) ev.getX();
                    mTouchY = (int) ev.getY();
                    //拖动item
                    onDragItem(mTouchX, mTouchY);
                    break;
                case MotionEvent.ACTION_UP:
                    onStopDrag();
                    isDrag = false;
                    mCurrentPosition = AdapterView.INVALID_POSITION;
                    break;
            }
            return true;
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 拖拽更新
     *
     * @param moveX
     * @param moveY
     */
    private void onDragItem(int moveX, int moveY) {
        mWindowLayoutParams.x = moveX - mPointLeft + mOffsetX;
        mWindowLayoutParams.y = moveY - mPointTop + mOffsetY - mStatusHeight;
        mWindowManager.updateViewLayout(mDragImageView, mWindowLayoutParams); //更新镜像的位置
        onSwapItem(moveX, moveY);

        //拖拽时到边界自动滚动
        mHandler.post(mScrollRunnable);
    }

    /**
     * item换位
     *
     * @param moveX
     * @param moveY
     */
    private void onSwapItem(int moveX, int moveY) {
        mCurrentPosition = pointToPosition(moveX, moveY);

        if (mCurrentPosition != mSelectIndex && mCurrentPosition != AdapterView.INVALID_POSITION) {
            if (mOnChangeListener != null) {
                mOnChangeListener.onChange(mSelectIndex, mCurrentPosition, mDragStartPosition);
            }

            mSelectIndex = mCurrentPosition;
        }
    }

    /**
     * 停止拖拽
     */
    private void onStopDrag() {
        View view = getChildAt(mSelectIndex - getFirstVisiblePosition());
        if (view != null) {
            view.setVisibility(View.VISIBLE);
        }

        if (mDragImageView != null) {
            mWindowManager.removeView(mDragImageView);
            mDragImageView = null;

        }
    }

    public void setOnChangeListenner(onChangeListener onChangeListener) {
        mOnChangeListener = onChangeListener;
    }

    public interface onChangeListener {
        void onChange(int start, int end, int drag);
    }

}
