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
import com.xagu.xxb.bean.Active;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by XAGU on 2020/3/15
 * Email:xagu_qc@foxmail.com
 * Describe: TODO
 */
public class ActiveListAdapter extends RecyclerView.Adapter<ActiveListAdapter.InnerHolder> {
    private List<Active> mData = new ArrayList<>();
    private OnItemClickListener mItemClickListener = null;

    @NonNull
    @Override
    public ActiveListAdapter.InnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(BaseApplication.getAppContext()).inflate(R.layout.item_active_list, parent, false);
        return new InnerHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ActiveListAdapter.InnerHolder holder, int position) {
        ImageView ivActiveCover = holder.itemView.findViewById(R.id.iv_active_cover);
        TextView tvActiveName = holder.itemView.findViewById(R.id.tv_active_name);
        TextView tvActiveDate = holder.itemView.findViewById(R.id.tv_active_date);

        Active active = mData.get(position);
        if (ivActiveCover != null) {
            Glide.with(BaseApplication.getAppContext()).load(active.getCover_url()).into(ivActiveCover);
        }
        if (tvActiveName != null) {
            tvActiveName.setText(active.getName());
        }
        if (tvActiveDate != null) {
            tvActiveDate.setText(active.getTime());
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onClick(v, position, mData.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setData(List<Active> actives) {
        mData.clear();
        //目前只支持签到
        mData.addAll(actives);
        notifyDataSetChanged();
    }

    public class InnerHolder extends RecyclerView.ViewHolder {
        public InnerHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onClick(View v, int position, Active active);
    }
}
