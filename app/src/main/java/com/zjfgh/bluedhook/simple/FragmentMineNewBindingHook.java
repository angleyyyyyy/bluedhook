package com.zjfgh.bluedhook.simple;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.XModuleResources;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.helper.widget.Grid;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.zjfgh.bluedhook.simple.module.VisitorUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class FragmentMineNewBindingHook {
    private static FragmentMineNewBindingHook instance;
    private final ClassLoader classLoader;
    private final XModuleResources modRes;
    private final WeakReference<Context> contextRef; // 使用 WeakReference

    private FragmentMineNewBindingHook(Context context, XModuleResources modRes) {
        this.classLoader = context.getClassLoader();
        this.contextRef = new WeakReference<>(context); // 弱引用
        this.modRes = modRes;
        hook();
        testHook();
    }

    public static synchronized FragmentMineNewBindingHook getInstance(Context context, XModuleResources modRes) {
        if (instance == null) {
            instance = new FragmentMineNewBindingHook(context, modRes);
        }
        return instance;
    }

    // 使用时检查 Context 是否还存在
    public Context getSafeContext() {
        Context context = contextRef.get();
        if (context == null) {
            throw new IllegalStateException("Context was garbage collected");
        }
        return context;
    }

    public void hook() {
        XposedHelpers.findAndHookMethod("com.soft.blued.databinding.FragmentMineNewBinding", classLoader, "a", View.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);

            }

            @SuppressLint({"NotifyDataSetChanged", "UseCompatLoadingForDrawables"})
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                View view = (View) param.args[0];
                @SuppressLint("DiscouragedApi")
                int ll_liveID = getSafeContext().getResources().getIdentifier("ll_live", "id", getSafeContext().getPackageName());
                LinearLayout ll_live = view.findViewById(ll_liveID);
                LayoutInflater inflater = (LayoutInflater) ll_live.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                XmlResourceParser anchorFansOpenLayoutRes = modRes.getLayout(R.layout.anchor_fans_open_layout);
                LinearLayout anchorFansOpenLayout = (LinearLayout) inflater.inflate(anchorFansOpenLayoutRes, null, false);
                // 创建一个GradientDrawable对象
                LinearLayout ll_ygb_give = anchorFansOpenLayout.findViewById(R.id.ll_ygb_give);
                ll_ygb_give.setBackground(modRes.getDrawable(R.drawable.anchor_fans_open_item_bg, null));
                LinearLayout ll_data_analyzer = anchorFansOpenLayout.findViewById(R.id.ll_data_analyzer);
                ll_data_analyzer.setBackground(modRes.getDrawable(R.drawable.anchor_fans_open_item_bg, null));
                ll_live.addView(anchorFansOpenLayout, 1);
                ll_ygb_give.setOnClickListener(setToastTgbListener());
                ll_data_analyzer.setOnClickListener(openDataAnalyzerView());
            }
        });
    }

    private void testHook() {
        XposedHelpers.findAndHookMethod("com.soft.blued.ui.find.fragment.MyVisitorFragmentNew", classLoader, "m", new XC_MethodHook() {
            @SuppressLint({"ResourceType", "SetTextI18n"})
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                View n = (View) XposedHelpers.getObjectField(param.thisObject, "n");
                LinearLayout w = n.findViewById(getSafeContext().getResources().getIdentifier("layout_chart_cover", "id", getSafeContext().getPackageName()));
                //15天来访趋势文本
                TextView tv = w.findViewById(getSafeContext().getResources().getIdentifier("tv", "id", getSafeContext().getPackageName()));
                tv.setText(tv.getText() + "(点击查看已记录的来访)");
                tv.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("UseCompatLoadingForDrawables")
                    @Override
                    public void onClick(View v) {
                        Activity activity = (Activity) v.getContext();
                        View view = LayoutInflater.from(activity).inflate(modRes.getLayout(R.layout.my_visitor_layout), null, true);
                        LinearLayout llMyVisitorListView = view.findViewById(R.id.ll_my_visitor_listview);
                        SuperRecyclerView superRecyclerView = getSuperRecyclerView(v);
                        llMyVisitorListView.addView(superRecyclerView);
                        CustomPopupWindow customPopupWindow = new CustomPopupWindow(activity, view, Color.parseColor("#FF0A121F"));
                        customPopupWindow.setBackgroundDrawable(modRes.getDrawable(R.drawable.bg_tech_space, null));
                        customPopupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
                        customPopupWindow.showAtCenter();
                        // 在 Activity 或 Fragment 中使用
                        List<VisitorUser> visitorUsers = new ArrayList<>();
                        NetworkManager.getInstance().getAsync(NetworkManager.getVisitorsAPI(), AuthManager.auHook(false, classLoader, getSafeContext()), new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {

                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                byte[] bytes = AppContainer.getInstance().getBytes();
                                try {
                                    JSONObject jsonObject = new JSONObject(response.body().string());
                                    String enData = jsonObject.getString("en_data");
                                    enData = ModuleTools.enDataDecrypt(enData, bytes);
                                    jsonObject = new JSONObject(enData);
                                    JSONArray data = jsonObject.getJSONArray("data");
                                    for (int i = 0; i < data.length(); i++) {
                                        JSONObject userObj = data.getJSONObject(i);
                                        long uid = userObj.optLong("uid", 0);

                                        if (uid != 0) {
                                            // 创建VisitorUser对象并设置所有字段
                                            VisitorUser visitorUser = new VisitorUser();
                                            // 设置所有字段
                                            visitorUser.setUid(uid);
                                            visitorUser.setAge(userObj.optInt("age", 0));
                                            visitorUser.setAvatar(userObj.optString("avatar", ""));
                                            visitorUser.setName(userObj.optString("name", ""));
                                            visitorUser.setHeight(userObj.optInt("height", 0));
                                            visitorUser.setWeight(userObj.optInt("weight", 0));
                                            visitorUser.setVbadge(userObj.optInt("vbadge", 0));
                                            visitorUser.setRole(userObj.optString("role", ""));
                                            visitorUser.setDistance(userObj.optDouble("distance", 0.0));
                                            visitorUser.setLatitude(userObj.optDouble("latitude", 0.0));
                                            visitorUser.setLongitude(userObj.optDouble("longitude", 0.0));
                                            visitorUser.setLocation(userObj.optInt("location", 0));
                                            visitorUser.setVipGrade(userObj.optInt("vip_grade", 0));
                                            visitorUser.setIsShadow(userObj.optInt("is_shadow", 0));
                                            visitorUser.setIsVipAnnual(userObj.optInt("is_vip_annual", 0));
                                            visitorUser.setVipExpLvl(userObj.optInt("vip_exp_lvl", 0));
                                            visitorUser.setIsCall(userObj.optInt("is_call", 0));
                                            visitorUser.setDescription(userObj.optString("description", ""));
                                            visitorUser.setOnlineState(userObj.optInt("online_state", 0));
                                            visitorUser.setVisitorsTime(userObj.optLong("visitors_time", 0L));
                                            visitorUser.setVisitorsCnt(userObj.optInt("visitors_cnt", 0));
                                            visitorUser.setIsHideDistance(userObj.optInt("is_hide_distance", 0));
                                            visitorUser.setIsHideVipLook(userObj.optInt("is_hide_vip_look", 0));
                                            visitorUser.setIsHideLastOperate(userObj.optInt("is_hide_last_operate", 0));
                                            visitorUser.setLastOperate(userObj.optLong("last_operate", 0L));

                                            // 添加到数据列表
                                            visitorUsers.add(visitorUser);

                                            // 打印调试信息
                                            Log.d("BluedHook", "添加访客: " + visitorUser.getName() + ", UID: " + visitorUser.getUid());
                                        } else {
                                            Log.w("BluedHook", "跳过无效用户数据(UID为0): " + userObj.toString());
                                        }
                                    }
                                    v.post(() -> superRecyclerView.setItems(
                                            visitorUsers,
                                            (parent, viewType) -> {
                                                // 直接创建并返回视图
                                                XmlResourceParser itemMyVisitorRes = modRes.getLayout(R.layout.item_my_visitor);
                                                return LayoutInflater.from(activity).inflate(itemMyVisitorRes, null);
                                            },
                                            (view, user, position) -> {
                                                view.setBackground(modRes.getDrawable(R.drawable.bg_tech_item, null));
                                                LinearLayout item_view = view.findViewById(R.id.item_view);
                                                item_view.setBackground(modRes.getDrawable(R.drawable.bg_tech_item_inner, null));
                                                LinearLayout user_info = view.findViewById(R.id.user_info);
                                                user_info.setBackground(modRes.getDrawable(R.drawable.bg_tech_tag, null));
                                                ImageView ivAvatar = view.findViewById(R.id.iv_avatar);
                                                Glide.with(getSafeContext())
                                                        .load(user.getAvatar())
                                                        .circleCrop()
                                                        .into(ivAvatar);
                                                TextView tvUserName = view.findViewById(R.id.tv_username);
                                                tvUserName.setText(user.getName());
                                                TextView tvAge = view.findViewById(R.id.tv_age);
                                                tvAge.setText(String.valueOf(user.getAge()));
                                                TextView tv_height = view.findViewById(R.id.tv_height);
                                                tv_height.setText(user.getHeight() + "cm");
                                                TextView tv_weight = view.findViewById(R.id.tv_weight);
                                                tv_weight.setText(user.getWeight() + "kg");
                                                String role = user.getRole();
                                                if (!role.equals("0") && !role.equals("1")) {
                                                    role = "其他";
                                                }
                                                TextView tv_role = view.findViewById(R.id.tv_role);
                                                tv_role.setText(role);
                                                long visitors_time = user.getVisitorsTime() * 1000L;
                                                Log.w("BluedHook", "visitors_time" + visitors_time);
                                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                                sdf.setTimeZone(TimeZone.getDefault()); // 设置为系统时区
                                                String timeStr = sdf.format(new Date(visitors_time));
                                                TextView tv_time = view.findViewById(R.id.tv_time);
                                                tv_time.setText("来访时间：" + timeStr);
                                                String location = user.getDistance() + "km";
                                                if (user.getIsHideDistance() == 1) {
                                                    location = "保密";
                                                }
                                                TextView tv_distance = view.findViewById(R.id.tv_distance);
                                                tv_distance.setText(location);
                                            }
                                    ));

                                } catch (JSONException e) {
                                    Log.w("BluedHook", "解析来访数据异常" + e.getMessage());
                                }

                            }
                        });
                    }
                });
            }

            @NonNull
            private SuperRecyclerView getSuperRecyclerView(View v) {
                SuperRecyclerView superRecyclerView = new SuperRecyclerView(v.getContext());
                superRecyclerView.setLayoutManager(new LinearLayoutManager(v.getContext()));
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,  // 宽度铺满
                        LinearLayout.LayoutParams.MATCH_PARENT   // 高度根据内容自适应
                        // 或者使用 LayoutParams.MATCH_PARENT 如果也需要高度铺满
                );
                superRecyclerView.setLayoutParams(layoutParams);
                return superRecyclerView;
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private View.OnClickListener openDataAnalyzerView() {
        return v -> {
            Activity activity = (Activity) v.getContext();
            DataAnalyzerView dataAnalyzerView = new DataAnalyzerView(activity);
            CustomPopupWindow customPopupWindow = new CustomPopupWindow(activity, dataAnalyzerView, Color.parseColor("#FF0A121F"));
            customPopupWindow.setBackgroundDrawable(modRes.getDrawable(R.drawable.bg_tech_space, null));
            customPopupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
            customPopupWindow.showAtCenter();
            customPopupWindow.setOnDismissListener(dataAnalyzerView::onDestroy);
        };
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
    private View.OnClickListener setToastTgbListener() {
        return v -> {
            Activity activity = (Activity) v.getContext();
            LayoutInflater inflater = (LayoutInflater) v.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            XmlResourceParser anchorFansListLayoutRes = modRes.getLayout(R.layout.anchor_fans_list_layout);
            LinearLayout anchorFansListLayout = (LinearLayout) inflater.inflate(anchorFansListLayoutRes, null, false);
            anchorFansListLayout.setBackground(modRes.getDrawable(R.drawable.bg_tech_space, null));
            CustomPopupWindow customPopupWindow = new CustomPopupWindow(activity, anchorFansListLayout, Color.parseColor("#FF0A121F"));
            customPopupWindow.setBackgroundDrawable(modRes.getDrawable(R.drawable.bg_tech_space, null));
            customPopupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
            customPopupWindow.showAtCenter();
            Button giveYbgButton = anchorFansListLayout.findViewById(R.id.give_ybg_button);
            giveYbgButton.setBackground(modRes.getDrawable(R.drawable.button_state, null));
            TextView anchorFansJoinCount = anchorFansListLayout.findViewById(R.id.anchor_fans_join_count);
            GradientDrawable getYbgButtonDrawable = new GradientDrawable();
            getYbgButtonDrawable.setCornerRadius(20f);
            giveYbgButton.setText("加载粉丝团列表...");
            giveYbgButton.setEnabled(false);

            List<AnchorFansListAdapter.AnchorFansListBean> anchorFansList = new ArrayList<>();
            RecyclerView recyclerView = new RecyclerView(v.getContext());
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(v.getContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setBackgroundColor(Color.parseColor("#FF041451"));
            recyclerView.setLayoutManager(linearLayoutManager);
            AnchorFansListAdapter adapter = new AnchorFansListAdapter(v.getContext(), anchorFansList, modRes);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
            // 创建线程池（建议使用单例模式管理线程池）
            ExecutorService executor = Executors.newCachedThreadPool(); // 或者使用固定大小的线程池

            executor.execute(() -> {
                final int[] page = {1};
                final int[] hasMore = {1};
                final int[] joinFansCount = {0, 0};
                final int[] itemCount = {0};
                while (hasMore[0] != 0) {
                    try {
                        Response response = NetworkManager.getInstance().get(NetworkManager.getBluedAnchorFansAPI(page[0]), AuthManager.fetchAuthHeaders(classLoader));
                        assert response.body() != null;
                        String resultStr = response.body().string();
                        JSONObject rootObj = new JSONObject(resultStr);
                        JSONArray dataArr = rootObj.getJSONArray("data");
                        int dataCount = dataArr.length();
                        hasMore[0] = dataCount;

                        for (int i = 0; i < dataCount; i++) {
                            JSONObject dataI = (JSONObject) dataArr.get(i);
                            AnchorFansListAdapter.AnchorFansListBean anchorFansListBean = new AnchorFansListAdapter.AnchorFansListBean();
                            anchorFansListBean.anchor = dataI.getLong("anchor");
                            anchorFansListBean.relation = dataI.getInt("relation");
                            anchorFansListBean.anchor_name = dataI.getString("anchor_name");
                            anchorFansListBean.name = dataI.getString("name");
                            anchorFansListBean.level = dataI.getInt("level");
                            anchorFansListBean.relation_level = dataI.getInt("relation_level");
                            anchorFansListBean.level_next = dataI.getInt("level_next");
                            anchorFansListBean.next_level_relation = dataI.getInt("next_level_relation");
                            anchorFansListBean.relation_limit = dataI.getInt("relation_limit");
                            anchorFansListBean.relation_today = dataI.getInt("relation_today");
                            anchorFansListBean.gift_count = dataI.getInt("gift_count");

                            anchorFansList.add(anchorFansListBean);
                            joinFansCount[0]++;
                            if (page[0] == 1 && i == 0) {
                                joinFansCount[1] = anchorFansListBean.gift_count;
                            }
                            itemCount[0]++;
                        }

                        page[0]++;
                        v.post(() -> {
                            adapter.notifyItemChanged(itemCount[0]);
                            v.post(() -> anchorFansJoinCount.setText("已加入粉丝团" + joinFansCount[0] + "个，预计可领取" + (joinFansCount[1] * joinFansCount[0]) + "根荧光棒"));
                        });
                        TimeUnit.MILLISECONDS.sleep(200);

                    } catch (IOException | JSONException | InterruptedException e) {
                        v.post(() -> Toast.makeText(getSafeContext(), "JSONException = " + e, Toast.LENGTH_LONG).show());
                    }
                }

                v.post(() -> {
                    giveYbgButton.setText("领取荧光棒");
                    giveYbgButton.setEnabled(true);
                });
            });

            giveYbgButton.setOnClickListener(view2 -> {
                String giveYbgButtonStr = giveYbgButton.getText().toString();
                if (giveYbgButtonStr.equals("领取荧光棒")) {
                    giveYbgButton.setText("领取中...");
                    giveYbgButton.setEnabled(false);
                    // 使用线程池而不是直接创建线程
                    ExecutorService executor1 = Executors.newSingleThreadExecutor();
                    executor1.execute(() -> {
                        try {
                            for (int i = 0; i < anchorFansList.size(); i++) {
                                AnchorFansListAdapter.AnchorFansListBean anchorFansListBean = anchorFansList.get(i);

                                // 执行网络请求
                                String json = new JSONObject()
                                        .put("anchor", String.valueOf(anchorFansListBean.anchor))
                                        .toString();

                                Response response = NetworkManager.getInstance().post(
                                        NetworkManager.getAnchorFansFreeGoodsAPI(),
                                        json,
                                        AuthManager.fetchAuthHeaders(classLoader)
                                );

                                assert response.body() != null;
                                String responseStr = response.body().string();
                                JSONObject jsonObject = new JSONObject(responseStr);
                                JSONArray data = jsonObject.getJSONArray("data");
                                JSONObject object = data.getJSONObject(0);

                                int giftCount = 0;
                                if (object.has("gift_count")) {
                                    giftCount = object.getInt("gift_count");
                                }
                                String message = object.getString("message");

                                anchorFansListBean.status = 1;
                                anchorFansListBean.gift_count = giftCount;
                                anchorFansListBean.message = message;

                                // 更新UI
                                int finalI = i;
                                v.post(() -> adapter.notifyItemChanged(finalI));

                                // 使用更合理的延迟方式 - 只在需要时延迟
                                if (i < anchorFansList.size() - 1) {
                                    // 使用ScheduledExecutorService进行精确延迟
                                    try {
                                        TimeUnit.MILLISECONDS.sleep(100);
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                        break;
                                    }
                                }
                            }

                            v.post(() -> {
                                giveYbgButton.setText("领取荧光棒");
                                giveYbgButton.setEnabled(true);
                            });

                        } catch (Exception e) {
                            v.post(() -> {
                                giveYbgButton.setText("领取荧光棒");
                                giveYbgButton.setEnabled(true);
                            });
                        } finally {
                            executor.shutdown();
                        }
                    });
                }
            });
            LinearLayout anchorFansListview = anchorFansListLayout.findViewById(R.id.anchor_fans_listview);
            anchorFansListview.addView(recyclerView);
        };
    }
}