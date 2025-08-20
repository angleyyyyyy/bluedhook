package com.zjfgh.bluedhook.simple;

import android.content.Context;
import android.content.res.XModuleResources;
import android.util.Log;

import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class LiveRankHook {
    private static LiveRankHook instance;
    private final ClassLoader classLoader;
    private final XModuleResources modRes;
    private static final String TAG = "BluedHook-LiveRankHook";

    public LiveRankHook(Context context, XModuleResources modRes) {
        this.classLoader = context.getClassLoader();
        this.modRes = modRes;
        // 1. Hook构造函数，用于查看榜单用户点赞数量
        hookRankConstructors();
        // 2. Hook初始化方法，用于解除隐身
        hookInitMethods();
    }

    public static synchronized LiveRankHook getInstance(Context context, XModuleResources modRes) {
        if (instance == null) {
            instance = new LiveRankHook(context, modRes);
        }
        return instance;
    }

    private void hookRankConstructors() {
        XposedHelpers.findAndHookConstructor("com.blued.android.module.live_china.fitem.FitemLiveRankFirstThree",
                classLoader, "java.util.ArrayList", boolean.class, int.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        param.args[1] = true; // 设置 isUserAnchor 为真
                    }
                });

        XposedHelpers.findAndHookConstructor("com.blued.android.module.live_china.rank.FitemLiveRankNormal",
                classLoader, "com.blued.android.module.live_china.model.BluedLiveRankListData", boolean.class, int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        param.args[1] = true; // 设置 isUserAnchor 为真
                        processPrivilegeAndName(param.args[0]); // 处理权限和名称
                    }
                });
    }

    private void hookInitMethods() {
        String[] initMethods = {"initFirstOne", "initSecondOne", "initThirdOne"};

        for (String method : initMethods) {
            XposedHelpers.findAndHookMethod("com.blued.android.module.live_china.fitem.FitemLiveRankFirstThree",
                    classLoader, method, "android.content.Context",
                    "com.blued.android.module.common.utils.freedom.BaseViewHolder",
                    "com.blued.android.module.live_china.model.BluedLiveRankListData",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            processPrivilegeAndName(param.args[2]); // 处理第三个参数（BluedLiveRankListData）
                        }
                    });
        }
    }

    /**
     * 处理权限和名称的通用方法
     */
    private void processPrivilegeAndName(Object rankData) {
        try {
            List<?> privilege = (List<?>) XposedHelpers.getObjectField(rankData, "privilege");
            if (privilege == null) return;

            for (Object privilegeItem : privilege) {
                int privilegeType = XposedHelpers.getIntField(privilegeItem, "privilege_type");
                int privilegeStatus = XposedHelpers.getIntField(privilegeItem, "privilege_status");
                int isQualified = XposedHelpers.getIntField(privilegeItem, "is_qualified");

                if (privilegeType == 2 && isQualified == 1 && privilegeStatus == 1) {
                    // 修改权限状态
                    XposedHelpers.setIntField(privilegeItem, "privilege_status", 0);
                    // 修改名称
                    String originalName = (String) XposedHelpers.getObjectField(rankData, "name");
                    if (originalName != null && !originalName.startsWith("[隐]")) {
                        XposedHelpers.setObjectField(rankData, "name", "[隐]" + originalName);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "处理权限和名称时出错: " + e.getMessage());
        }
    }

}
