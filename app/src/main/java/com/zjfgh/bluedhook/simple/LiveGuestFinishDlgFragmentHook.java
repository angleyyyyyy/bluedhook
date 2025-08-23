package com.zjfgh.bluedhook.simple;

import android.content.Context;
import android.content.res.XModuleResources;
import android.graphics.Color;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class LiveGuestFinishDlgFragmentHook {
    private final ClassLoader classLoader;
    private static LiveGuestFinishDlgFragmentHook instance;
    private final WeakReference<Context> contextRef;
    private final XModuleResources modRes;

    private static final String TAG = "BluedHook-LiveGuestFinishDlgFragmentHook";

    private LiveGuestFinishDlgFragmentHook(Context context, XModuleResources modRes) {
        this.contextRef = new WeakReference<>(context);
        this.classLoader = context.getClassLoader();
        this.modRes = modRes;
        AutoEnterLiveRoomHook();
    }

    public static LiveGuestFinishDlgFragmentHook getInstance(Context context, XModuleResources modRes) {
        if (instance == null) {
            instance = new LiveGuestFinishDlgFragmentHook(context, modRes);
        }
        return instance;
    }

    public void AutoEnterLiveRoomHook() {
        Class<?> finishFragmentClass = XposedHelpers.findClass(
                "com.blued.android.module.live_china.fragment.LiveGuestFinishDlgFragment",
                classLoader
        );

        // Hook b() 方法（初始化方法）
        XposedHelpers.findAndHookMethod(finishFragmentClass, "b", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                SettingItem autoEnterLiveRoom = SQLiteManagement.getInstance().getSettingByFunctionId(SettingsViewCreator.AUTO_ENTER_LIVE);
                if (autoEnterLiveRoom.isSwitchOn()) {
                    return;
                }
                Object instance = param.thisObject;
                TextView tvAutoNextTipsTime = (TextView) XposedHelpers.getObjectField(instance, "z");
                tvAutoNextTipsTime.setText("已关闭自动跳转直播间功能");
                tvAutoNextTipsTime.setTextColor(Color.RED);
                // 获取关闭按钮 k
                ImageView closeButton = (ImageView) XposedHelpers.getObjectField(instance, "k");
                if (closeButton != null) {
                    // 移除原有的点击监听器
                    closeButton.setOnClickListener(null);
                    // 设置新的点击监听器，只执行关闭操作
                    closeButton.setOnClickListener(v -> {
                        // 只执行事件跟踪和关闭操作，不执行跳转
                        try {
                            // 直接调用n()方法关闭窗口
                            XposedHelpers.callMethod(instance, "n");
                        } catch (Exception e) {
                            // 如果出错，直接调用n()方法
                            XposedHelpers.callMethod(instance, "n");
                        }
                    });
                }
            }
        });
        XposedHelpers.findAndHookMethod("com.blued.android.module.live_china.fragment.LiveGuestFinishDlgFragment", classLoader, "k", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                SettingItem autoEnterLiveRoom = SQLiteManagement.getInstance().getSettingByFunctionId(SettingsViewCreator.AUTO_ENTER_LIVE);
                if (autoEnterLiveRoom.isSwitchOn()) {
                    return;
                }
                Log.d(TAG, "屏蔽直播间自动跳转倒计时方法");
                param.setResult(null);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });
        XposedHelpers.findAndHookMethod("com.blued.android.module.live_china.fragment.LiveGuestFinishDlgFragment", classLoader, "m", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                SettingItem autoEnterLiveRoom = SQLiteManagement.getInstance().getSettingByFunctionId(SettingsViewCreator.AUTO_ENTER_LIVE);
                if (autoEnterLiveRoom.isSwitchOn()) {
                    return;
                }
                //直接屏蔽直播结束自动跳转随机直播间的方法
                Log.d(TAG, "屏蔽直播间自动跳转方法");
                param.setResult(null);
            }
        });
    }

    public XModuleResources getModRes() {
        return modRes;
    }

    public WeakReference<Context> getContextRef() {
        return contextRef;
    }
}
