package cn.lemon.jcourse.module.java;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import cn.alien95.util.Utils;
import cn.lemon.common.base.ToolbarActivity;
import cn.lemon.jcourse.R;
import cn.lemon.jcourse.config.Config;
import cn.lemon.jcourse.model.AccountModel;
import cn.lemon.jcourse.model.JavaCourseModel;
import cn.lemon.jcourse.model.bean.Info;
import cn.lemon.jcourse.model.bean.JavaCourse;
import cn.lemon.jcourse.module.LoginActivity;
import cn.lemon.jcourse.module.ServiceResponse;

import static cn.lemon.jcourse.R.id.star;

/**
 * Created by linlongxin on 2016/8/7.
 */

public class JavaTextDetailActivity extends ToolbarActivity implements View.OnTouchListener, GestureDetector.OnGestureListener, View.OnClickListener {

    private ImageView mCover;
    private TextView mTitle;
    private TextView mSubtitle;
    private TextView mContent;
    private ScrollView mScrollView;
    private FloatingActionButton mStar;

    private JavaCourse mData;

    private float mStartY;
    private float mCurrentY;

    private GestureDetector mGestureDetector;

    private boolean hasVisible = true;
    private boolean isStar = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mData = (JavaCourse) getIntent().getSerializableExtra(Config.JAVA_COURSE_DETAIL);
        setContentView(R.layout.java_activity_text_detail);
        setToolbarHomeBack(true);

        mCover = (ImageView) findViewById(R.id.cover);
        mTitle = (TextView) findViewById(R.id.title);
        mSubtitle = (TextView) findViewById(R.id.subtitle);
        mContent = (TextView) findViewById(R.id.content);
        mScrollView = (ScrollView) findViewById(R.id.scroll_view);
        mStar = (FloatingActionButton) findViewById(star);
        mStar.setOnClickListener(this);
        mGestureDetector = new GestureDetector(this, this);
        mScrollView.setOnTouchListener(this);

        setData(mData);
        getIsStar(mData.id);
    }

    public void setData(JavaCourse data) {
        Glide.with(this)
                .load(data.cover)
                .placeholder(R.drawable.ic_place_holder)
                .error(R.drawable.ic_load_error)
                .into(mCover);
        mTitle.setText(data.title);
        mSubtitle.setText(data.subtitle);
        mContent.setText(data.content);
    }

    public void getIsStar(int id) {
        if (AccountModel.getInstance().getAccount() != null) {
            JavaCourseModel.getInstance().getIsStar(id, new ServiceResponse<Info>() {
                @Override
                public void onNext(Info s) {
                    super.onNext(s);
                    if(s.star){
                        mStar.setImageResource(R.drawable.ic_star);
                        isStar = true;
                    }
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case star:
                starJCourse(mData.id);
                break;
        }
    }

    public void starJCourse(int id) {
        if (AccountModel.getInstance().getAccount() == null) {
            Utils.Toast("请先登录");
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }
        if (isStar) {
            JavaCourseModel.getInstance().unstarJCourse(id, new ServiceResponse<Info>() {
                @Override
                public void onNext(Info info) {
                    super.onNext(info);
                    Utils.Toast("取消收藏");
                    mStar.setImageResource(R.drawable.ic_unstar);
                    isStar = false;
                }
            });
        } else
            JavaCourseModel.getInstance().starJCourse(id, new ServiceResponse<Info>() {
                @Override
                public void onNext(Info info) {
                    super.onNext(info);
                    Utils.Toast("收藏成功");
                    mStar.setImageResource(R.drawable.ic_star);
                    isStar = true;
                }
            });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        mStartY = e.getY();
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        mCurrentY = e2.getY();
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    // e1：第1个ACTION_DOWN MotionEvent
    // e2：最后一个ACTION_MOVE MotionEvent
    // velocityX：X轴上的移动速度（像素/秒）
    // velocityY：Y轴上的移动速度（像素/秒）
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        //向上划
        ObjectAnimator mAlphaAnimator;
        ObjectAnimator mTranslationYAnimator;
        ObjectAnimator mTranslationXAnimator;
        AnimatorSet mAnimatorSet;
        if (mCurrentY - mStartY < 0 && velocityY < 2000 && hasVisible && Math.abs(velocityY) > Math.abs(velocityX)) {
            mAlphaAnimator = ObjectAnimator.ofFloat(mStar, "alpha", 1f, 0f);
            mTranslationXAnimator = ObjectAnimator.ofFloat(mStar, "scaleX", 1f, 0f);
            mTranslationYAnimator = ObjectAnimator.ofFloat(mStar, "scaleY", 1f, 0f);
            mAnimatorSet = new AnimatorSet();
            mAnimatorSet.play(mTranslationYAnimator).with(mAlphaAnimator).with(mTranslationXAnimator);
            mAnimatorSet.setDuration(200);
            mAnimatorSet.start();
            mStar.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mStar.setVisibility(View.GONE);
                }
            }, 200);
            hasVisible = false;
            //向下划
        } else if (mCurrentY - mStartY > 0 && velocityY > 1500 && !hasVisible && Math.abs(velocityY) > Math.abs(velocityX)) {
            mStar.setVisibility(View.VISIBLE);
            mAlphaAnimator = ObjectAnimator.ofFloat(mStar, "alpha", 0f, 1f);
            mTranslationXAnimator = ObjectAnimator.ofFloat(mStar, "scaleX", 0f, 1f);
            mTranslationYAnimator = ObjectAnimator.ofFloat(mStar, "scaleY", 0f, 1f);
            mAnimatorSet = new AnimatorSet();
            mAnimatorSet.play(mTranslationYAnimator).with(mAlphaAnimator).with(mTranslationXAnimator);
            mAnimatorSet.setDuration(200);
            mAnimatorSet.start();
            hasVisible = true;
        }
        return false;
    }
}
