package com.xagu.xxb.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.xagu.xxb.R;
import com.xagu.xxb.base.BaseApplication;
import com.xagu.xxb.bean.Course;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by XAGU on 2020/3/15
 * Email:xagu_qc@foxmail.com
 * Describe: TODO
 */
public class CourseListAdapter extends RecyclerView.Adapter<CourseListAdapter.InnerHolder> {
    private List<Course> mData = new ArrayList<>();
    private OnItemClickListener mItemClickListener = null;

    @NonNull
    @Override
    public CourseListAdapter.InnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(BaseApplication.getAppContext()).inflate(R.layout.item_course_list, parent, false);
        return new InnerHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseListAdapter.InnerHolder holder, int position) {
        ImageView ivCourseCover =  holder.itemView.findViewById(R.id.iv_active_cover);
        TextView tvCourseName =  holder.itemView.findViewById(R.id.tv_course_name);
        TextView tvCourseTeacher =  holder.itemView.findViewById(R.id.tv_course_teacher);

        Course course = mData.get(position);
        if (ivCourseCover != null) {
            Glide.with(BaseApplication.getAppContext()).load(course.getImageUrl()).into(ivCourseCover);
        }
        if (tvCourseName != null) {
            tvCourseName.setText(course.getName());
        }
        if (tvCourseTeacher != null){
            tvCourseTeacher.setText(course.getTeacher());
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onClick(position,mData.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setData(List<Course> course) {
        mData.clear();
        mData.addAll(course);
        notifyDataSetChanged();
    }

    public class InnerHolder extends RecyclerView.ViewHolder {
        public InnerHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.mItemClickListener = listener;
    }

    public interface OnItemClickListener{
        void onClick(int position,Course course);
    }
}
