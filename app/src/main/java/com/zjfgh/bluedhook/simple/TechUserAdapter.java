package com.zjfgh.bluedhook.simple;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.XModuleResources;
import android.content.res.XmlResourceParser;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import de.robv.android.xposed.XposedHelpers;

public class TechUserAdapter extends RecyclerView.Adapter<TechUserAdapter.TechUserViewHolder> {

    private JSONArray dataArr;
    private final Context context;
    private final LayoutInflater inflater;
    private final XModuleResources modRes;
    private static final String TAG = "BluedHook-TechUserAdapter";

    public TechUserAdapter(JSONArray dataArr, Context context, XModuleResources modRes) {
        this.dataArr = dataArr;
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.modRes = modRes;
    }

    @NonNull
    @Override
    public TechUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        XmlResourceParser userMoreInfoListItemLayout = modRes.getLayout(R.layout.item_user_more_info);
        View view = inflater.inflate(userMoreInfoListItemLayout, parent, false);
        return new TechUserViewHolder(view);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull TechUserViewHolder holder, int position) {
        try {
            JSONObject item = dataArr.getJSONObject(position);

            String avatarUrl = item.getString("avatar");
            Glide.with(context)
                    .load(avatarUrl)
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(holder.ivAvatar);
            holder.llItemRoot.setBackground(modRes.getDrawable(R.drawable.bg_tech_item_dark_gray_round, null));
            holder.tvName.setText(item.getString("name"));
            String platform = item.getString("platform");
            if (platform.equals("ios")) {
                platform = "苹果";
            } else if (platform.equals("android")) {
                platform = "安卓";
            }
            holder.tvPlatform.setText(String.format(Locale.getDefault(), "登录设备 %s", platform));
            holder.tvDescription.setText(item.getString("description"));
            holder.tvLevel.setText(String.format(Locale.getDefault(), "财富等级 Lv. %d", item.getLong("rich_level")));
            holder.tvLevel.setBackground(modRes.getDrawable(R.drawable.tech_level_bg, null));
            holder.tvBeans.setText(String.format(Locale.getDefault(), "%s 豆", ModuleTools.formatBeans(item.getDouble("rich_beans"))));
            holder.tvVersion.setText(item.getString("version"));
        } catch (JSONException e) {
            Log.e(TAG, "解析json数据异常" + e);
        }
    }

    @Override
    public int getItemCount() {
        return dataArr.length();
    }

    public void updateData(JSONArray newData) {
        this.dataArr = newData;
        notifyDataSetChanged();
    }

    static class TechUserViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llItemRoot;
        ImageView ivAvatar;
        TextView tvName;
        TextView tvPlatform;
        TextView tvDescription;
        TextView tvLevel;
        TextView tvBeans;
        TextView tvVersion;

        public TechUserViewHolder(@NonNull View itemView) {
            super(itemView);
            llItemRoot = itemView.findViewById(R.id.ll_item_root);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvPlatform = itemView.findViewById(R.id.tvPlatform);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvLevel = itemView.findViewById(R.id.tvLevel);
            tvBeans = itemView.findViewById(R.id.tvBeans);
            tvVersion = itemView.findViewById(R.id.tvVersion);
        }
    }


}
