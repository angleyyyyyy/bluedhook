package com.zjfgh.bluedhook.simple;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

import de.robv.android.xposed.XposedHelpers;

public class SettingsViewCreator {
    private final SQLiteManagement dbManager;
    private final Context context;
    public static final int USER_INFO_FRAGMENT_NEW_HOOK = 0;
    public static final int ANCHOR_MONITOR_LIVE_HOOK = 1;
    public static final int PLAYING_ON_LIVE_BASE_MODE_FRAGMENT_HOOK = 2;
    public static final int LIVE_JOIN_HIDE_HOOK = 3;
    public static final int WS_SERVER = 4;
    public static final int REC_HEW_HORN = 5;
    public static final int SHIELD_LIKE = 6;
    public static final int AUTO_LIKE = 7;
    public static final int LIVE_AUTO_SEND_MSG = 8;
    public static final int AUTO_ENTER_LIVE = 9;

    public SettingsViewCreator(Context context) {
        this.context = context;
        this.dbManager = SQLiteManagement.getInstance();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public View createSettingsView() {
        // 获取所有设置项
        List<SettingItem> settingsList = dbManager.getAllSettings();

        // 创建滚动视图作为根布局
        ScrollView scrollView = new ScrollView(context);
        scrollView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        // 创建主线性布局
        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(mainLayout);

        // 获取布局填充器
        LayoutInflater inflater = LayoutInflater.from(context);

        // 为每个设置项创建视图并添加到主布局
        for (SettingItem setting : settingsList) {
            View settingItemView = inflater.inflate(
                    AppContainer.getInstance().getModuleRes().getLayout(R.layout.module_item_setting),
                    mainLayout,
                    false
            );

            // 初始化视图组件
            TextView functionName = settingItemView.findViewById(R.id.setting_function_name);
            TextView description = settingItemView.findViewById(R.id.setting_description);
            @SuppressLint("UseSwitchCompatOrMaterialCode")
            Switch switchButton = settingItemView.findViewById(R.id.setting_switch);
            EditText extraData = settingItemView.findViewById(R.id.setting_extra_data);
            // 设置初始值
            functionName.setText(setting.getFunctionName());
            description.setText(setting.getDescription());
            switchButton.setChecked(setting.isSwitchOn());
            if (setting.getFunctionId() == WS_SERVER) {
                if (BluedHook.wsServerManager != null) {
                    switchButton.setChecked(BluedHook.wsServerManager.isServerRunning());
                }
            }
            if (setting.getExtraDataHint().isEmpty()) {
                extraData.setVisibility(View.GONE);
            } else {
                extraData.setText(setting.getExtraData());
                extraData.setHint(setting.getExtraDataHint());
                // 根据开关状态设置额外数据的可见性
                extraData.setVisibility(setting.isSwitchOn() ? View.VISIBLE : View.GONE);
            }
            // 设置开关监听器
            switchButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                dbManager.updateSettingSwitchState(setting.getFunctionId(), isChecked);
                setting.setSwitchOn(isChecked);
                if (!setting.getExtraDataHint().isEmpty())
                    extraData.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                switchListener.onSwitchChanged(setting.getFunctionId(), isChecked);
                if (setting.getFunctionId() == WS_SERVER) {
                    if (BluedHook.wsServerManager != null) {
                        if (isChecked) {
                            BluedHook.wsServerManager.startServer(Integer.parseInt(setting.getExtraData()));
                        } else {
                            BluedHook.wsServerManager.stopServer();
                        }
                    }
                } else if (setting.getFunctionId() == REC_HEW_HORN) {
                    if (!setting.isSwitchOn()) {
                        PlayingOnLiveBaseModeFragmentHook.getInstance(AppContainer.getInstance().getBluedContext(), AppContainer.getInstance().getModuleRes()).stopConnectLive();
                        Log.e("BluedHook", "关闭了呗");
                    } else {
                        Log.e("BluedHook", "开启了呗");
                        PlayingOnLiveBaseModeFragmentHook.getInstance(AppContainer.getInstance().getBluedContext(), AppContainer.getInstance().getModuleRes()).startTimer();
                    }
                }
            });
            extraData.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // 更新数据库中的额外数据
                    dbManager.updateSettingExtraData(setting.getFunctionId(), s.toString());
                    setting.setExtraData(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            // 将设置项添加到主布局
            mainLayout.addView(settingItemView);
        }

        return scrollView;
    }


    // 定义Switch状态变化的回调接口
    public interface OnSwitchCheckedChangeListener {
        void onSwitchChanged(int functionId, boolean isChecked);
    }

    protected OnSwitchCheckedChangeListener switchListener;

    // 设置监听器的方法
    public void setOnSwitchCheckedChangeListener(OnSwitchCheckedChangeListener listener) {
        this.switchListener = listener;
    }
}