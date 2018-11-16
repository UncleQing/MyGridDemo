package com.example.mygriddemo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mygriddemo.bean.MyGridBean;
import com.example.mygriddemo.widget.MyGridView;

import java.util.ArrayList;
import java.util.List;

public class MyGridActivity extends Activity {


    private MyGridView mMyGridView;
    private MyGridAdapter mAdapter;
    private List<MyGridBean> mBeans;

    private static final int MAX_ITEM = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_grid);
        mMyGridView = (MyGridView) findViewById(R.id.mgv);
        mMyGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mBeans = new ArrayList<>();

        //假数据填充
        for (int i = 0; i < MAX_ITEM; i++) {
            MyGridBean bean = new MyGridBean();
            if (i % 4 == 0) {
                bean.setDrableRes(R.mipmap.icon_one);
            } else if (i % 4 == 1) {
                bean.setDrableRes(R.mipmap.icon_two);
            } else if (i % 4 == 2) {
                bean.setDrableRes(R.mipmap.icon_three);
            } else if (i % 4 == 3) {
                bean.setDrableRes(R.mipmap.icon_four);
            }
            bean.setTitle("按钮" + i);
            mBeans.add(bean);
        }

        mAdapter = new MyGridAdapter(this);
        mMyGridView.setAdapter(mAdapter);
        mMyGridView.setOnChangeListenner(new MyGridView.onChangeListener() {
            @Override
            public void onChange(int start, int end, int drag) {
                mAdapter.changeItem(start, end, drag);
            }
        });
    }

    class MyGridAdapter extends BaseAdapter {

        Context context;

        public MyGridAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return mBeans.size();
        }

        @Override
        public Object getItem(int position) {
            return mBeans.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            convertView = View.inflate(context, R.layout.layout_my_grid, null);
            viewHolder = new ViewHolder();
            viewHolder.iv = (ImageView) convertView.findViewById(R.id.iv_layout_mg);
            viewHolder.tv = (TextView) convertView.findViewById(R.id.tv_layout_mg);
            MyGridBean bean = mBeans.get(position);
            viewHolder.iv.setImageResource(bean.getDrableRes());
            viewHolder.tv.setText(bean.getTitle());
            convertView.setTag(viewHolder);
            return convertView;
             
        }

        /**
         * 刷新交换item数据，代替notifyDataSetChanged
         * @param start
         * @param end
         * @param drag
         */
        public void changeItem(int start, int end, int drag) {
            int fristVisible = mMyGridView.getFirstVisiblePosition();
            ViewHolder viewHolderStart = null;
            ViewHolder viewHolderEnd = null;


            MyGridBean tempBean = mBeans.get(start);
            mBeans.set(start, mBeans.get(end));
            mBeans.set(end, tempBean);


            MyGridBean beanStart = mBeans.get(start);
            MyGridBean beanEnd = mBeans.get(end);
            View viewStart = mMyGridView.getChildAt(start - fristVisible);
            View viewEnd = mMyGridView.getChildAt(end - fristVisible);
            /**
             * 因为拖拽过程可能伴随着滑屏，导致需要换位的item不可见，则getChildAt获取为null，因此以下有关viewStart、viewEnd处理均需加入非null判断
             * 但只要在数据源mBeans中交换了，再次显示会重新调用getView，显示还是交换后的数据
             */

            if (viewStart != null) {
                viewHolderStart = (ViewHolder) viewStart.getTag();
                viewHolderStart.iv.setImageResource(beanStart.getDrableRes());
                viewHolderStart.tv.setText(beanStart.getTitle());
            }

            if (viewEnd != null) {
                viewHolderEnd = (ViewHolder) viewEnd.getTag();
                viewHolderEnd.iv.setImageResource(beanEnd.getDrableRes());
                viewHolderEnd.tv.setText(beanEnd.getTitle());
            }


            if (mMyGridView.getCurrentPosition() == start) {
                viewStart.setVisibility(View.INVISIBLE);
                if (viewEnd != null) {
                    viewEnd.setVisibility(View.VISIBLE);
                }
            } else if (mMyGridView.getCurrentPosition() == end) {
                if (viewStart != null) {
                    viewStart.setVisibility(View.VISIBLE);
                }
                viewEnd.setVisibility(View.INVISIBLE);
            }

            //二次交换
            if (start == drag || end == drag)
                return;
            tempBean = mBeans.get(start);
            mBeans.set(start, mBeans.get(drag));
            mBeans.set(drag, tempBean);

            beanStart = mBeans.get(start);
            beanEnd = mBeans.get(drag);
            viewStart = mMyGridView.getChildAt(start - fristVisible);
            viewEnd = mMyGridView.getChildAt(drag - fristVisible);

            if (viewStart != null) {
                viewHolderStart = (ViewHolder) viewStart.getTag();
                viewHolderStart.iv.setImageResource(beanStart.getDrableRes());
                viewHolderStart.tv.setText(beanStart.getTitle());
            }

            if (viewEnd != null) {
                viewHolderEnd = (ViewHolder) viewEnd.getTag();
                viewHolderEnd.iv.setImageResource(beanEnd.getDrableRes());
                viewHolderEnd.tv.setText(beanEnd.getTitle());
            }

            if (mMyGridView.getCurrentPosition() == start) {
                if (viewStart != null) {
                    viewStart.setVisibility(View.INVISIBLE);
                }
                if (viewEnd != null) {
                    viewEnd.setVisibility(View.VISIBLE);
                }
            } else if (mMyGridView.getCurrentPosition() == drag) {
                if (viewStart != null) {
                    viewStart.setVisibility(View.VISIBLE);
                }
                if (viewEnd != null) {
                    viewEnd.setVisibility(View.INVISIBLE);
                }
            }
        }

        class ViewHolder {
            ImageView iv;
            TextView tv;
        }
    }
}
