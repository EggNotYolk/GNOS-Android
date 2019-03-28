package com.gnos.myui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

import com.example.dawn.gnos.R;
import com.gnos.model.IWordButtonClickListener;
import com.gnos.model.WordButton;
import com.gnos.myutil.Util;

import java.util.ArrayList;

public class MyGridView extends GridView {
    public final static int COUNTS_WORDS = 24;

    private ArrayList<WordButton> mArrayList = new ArrayList<WordButton>();

    private MyGridAdapter myAdapter;

    private Context mContext;

    private Animation mScaleAnimation;

    private IWordButtonClickListener mWordButtonListener;

    public MyGridView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        mContext = context;

        myAdapter = new MyGridAdapter();
        this.setAdapter(myAdapter);
    }

    public void updateData(ArrayList<WordButton> list) {
        mArrayList = list;

        //重新设置数据源（刷新文字框文字）
        setAdapter(myAdapter);
    }

    class MyGridAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mArrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return mArrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View v, ViewGroup parent) {
            final WordButton holder;

            if(null == v) {
                v = Util.getView(mContext, R.layout.myui_gridview);

                holder = mArrayList.get(position);

                // 加载动画
                mScaleAnimation = AnimationUtils.loadAnimation(mContext,R.anim.scale);

                // 设置动画的延时时间
                mScaleAnimation.setStartOffset(position * 100);

                holder.setIndex(position);
                holder.setViewButton((Button)v.findViewById(R.id.item_btn));
                holder.getViewButton().setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        mWordButtonListener.onWordButtonClick(holder);
                    }
                });

                v.setTag(holder);
            }
            else {
                holder = (WordButton)v.getTag();
            }

            holder.getViewButton().setText(holder.getWordString());

            //播放动画
            v.startAnimation(mScaleAnimation);

            return v;
        }
    }

    /**
     * 注册监听接口
     * @param listener
     */
    public void registerOnWordButtonClick(IWordButtonClickListener listener) {
        mWordButtonListener = listener;
    }
}
