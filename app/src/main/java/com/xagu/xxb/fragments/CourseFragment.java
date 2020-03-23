package com.xagu.xxb.fragments;

import android.content.Intent;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.xagu.xxb.CourseDetailActivity;
import com.xagu.xxb.R;
import com.xagu.xxb.adapter.CourseListAdapter;
import com.xagu.xxb.base.BaseFragment;
import com.xagu.xxb.bean.Course;
import com.xagu.xxb.interfaces.ICourseCallback;
import com.xagu.xxb.presenter.ActivePresenter;
import com.xagu.xxb.presenter.CoursePresenter;
import com.xagu.xxb.utils.UIUtil;
import com.xagu.xxb.views.UILoader;

import java.util.List;

/**
 * Created by XAGU on 2020/3/14
 * Email:xagu_qc@foxmail.com
 * Describe: TODO
 */
public class CourseFragment extends BaseFragment implements ICourseCallback {

    private View mView;
    private CourseListAdapter mCourseListAdapter;
    private RecyclerView mRvCourse;
    private CoursePresenter mCoursePresenter;
    private TwinklingRefreshLayout mTwRefresh;
    private UILoader mUiLoader = null;

    @Override
    protected View onSubViewLoaded(LayoutInflater layoutInflater, ViewGroup container) {
        mUiLoader = new UILoader(getActivity()) {
            @Override
            protected View getSuccessView(ViewGroup container) {
                return createSuccessView(layoutInflater, container);
            }
        };
        if (mUiLoader.getParent() instanceof ViewGroup) {
            ((ViewGroup) mUiLoader.getParent()).removeView(mUiLoader);
        }
        initPresenter();
        initEvent();
        return mUiLoader;
    }

    private View createSuccessView(LayoutInflater layoutInflater, ViewGroup container) {
        mView = layoutInflater.inflate(R.layout.fragment_course, container, false);
        mRvCourse = mView.findViewById(R.id.rv_course);
        mTwRefresh = mView.findViewById(R.id.twink_refresh);
        //关闭加载更多
        mTwRefresh.setEnableLoadmore(false);
        mRvCourse.setLayoutManager(new LinearLayoutManager(getContext()));
        mRvCourse.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.top = UIUtil.dip2px(view.getContext(), 8);
                outRect.bottom = UIUtil.dip2px(view.getContext(), 8);
                outRect.left = UIUtil.dip2px(view.getContext(), 8);
                outRect.right = UIUtil.dip2px(view.getContext(), 8);
                //super.getItemOffsets(outRect, view, parent, state);
            }
        });
        mCourseListAdapter = new CourseListAdapter();
        mRvCourse.setAdapter(mCourseListAdapter);
        return mView;
    }

    private void initPresenter() {
        mCoursePresenter = CoursePresenter.getInstance();
        mCoursePresenter.registerViewCallback(this);
        mUiLoader.updateStatus(UILoader.UIStatus.LOADING);
        mCoursePresenter.getCourseInfo();
    }

    private void initEvent() {
        mUiLoader.setOnRetryClickListener(new UILoader.OnRetryClickListener() {
            @Override
            public void onRetryClick() {
                //刷新
                mCoursePresenter.getCourseInfo();
                mUiLoader.updateStatus(UILoader.UIStatus.LOADING);
            }
        });
        mCourseListAdapter.setOnItemClickListener(new CourseListAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position, Course course) {
                ActivePresenter.getInstance().setTargetCourse(course);
                startActivity(new Intent(getActivity(), CourseDetailActivity.class));
            }
        });
        mTwRefresh.setOnRefreshListener(new RefreshListenerAdapter() {
            @Override
            public void onRefresh(TwinklingRefreshLayout refreshLayout) {
                super.onRefresh(refreshLayout);
                mCoursePresenter.getCourseInfo();
            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCoursePresenter != null) {
            mCoursePresenter.unRegisterViewCallback(this);
        }
    }

    @Override
    public void onGetCourseSuccess(List<Course> course) {
        if (course == null || course.size() == 0) {
            mUiLoader.updateStatus(UILoader.UIStatus.EMPTY);
        } else {
            mUiLoader.updateStatus(UILoader.UIStatus.SUCCESS);
        }
        //取到数据，更新UI
        mCourseListAdapter.setData(course);
        if (mTwRefresh != null) {
            mTwRefresh.onFinishRefresh();
        }
    }

    @Override
    public void onGetCourseFailed() {
        mUiLoader.updateStatus(UILoader.UIStatus.NETWORK_ERROR);
    }

    @Override
    public void onGetCourseNetworkError() {
        mUiLoader.updateStatus(UILoader.UIStatus.NETWORK_ERROR);
    }
}
