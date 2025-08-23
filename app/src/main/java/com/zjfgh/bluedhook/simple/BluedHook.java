package com.zjfgh.bluedhook.simple;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.XModuleResources;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.java_websocket.WebSocket;
import org.json.JSONObject;

import java.util.Objects;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class BluedHook implements IXposedHookLoadPackage, IXposedHookInitPackageResources, IXposedHookZygoteInit {
    public static WSServerManager wsServerManager;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam param) {
        if (param.packageName.equals("com.soft.blued")) {
            XposedHelpers.findAndHookMethod("com.soft.blued.StubWrapperProxyApplication", param.classLoader, "initProxyApplication", Context.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Context bluedContext = (Context) param.args[0];
                    XModuleResources modRes = AppContainer.getInstance().getModuleRes();
                    ClassLoader classLoader = bluedContext.getClassLoader();
                    AppContainer.getInstance().setBluedContext(bluedContext);
                    AppContainer.getInstance().setClassLoader(classLoader);
                    initializeSettings();
                    NetworkManager.getInstance();
                    UserInfoFragmentNewHook.getInstance(bluedContext, modRes);
                    LiveHook.getInstance(bluedContext);
                    PlayingOnLiveBaseModeFragmentHook.getInstance(bluedContext, modRes);
                    FragmentMineNewBindingHook.getInstance(bluedContext, modRes);
                    LiveMultiBoyItemViewHook.getInstance();
                    ChatHook.getInstance(bluedContext, modRes);
                    NearbyPeopleFragment_ViewBindingHook.getInstance(bluedContext, classLoader);
                    HornViewNewHook.autoHornViewNew();
                    LikeFollowModel.getInstance(bluedContext, modRes);
                    LiveMsgSendManagerHook.getInstance();
                    LiveMultiPKItemViewHook.getInstance(bluedContext, modRes);
                    LiveRankHook.getInstance(bluedContext, modRes);
                    LiveRankHook.getInstance(bluedContext, modRes);
                    LiveGuestFinishDlgFragmentHook.getInstance(bluedContext, modRes);
                    Toast.makeText(bluedContext, "外挂成功！", Toast.LENGTH_LONG).show();
                    try {
                        VoiceTTS.getInstance(bluedContext);
                    } catch (Exception e) {
                        Log.e("BluedHook", "语音合成模块异常：\n" +
                                e);
                    }
                    // Hook ZegoAvConfig 构造函数（修改分辨率等级）
                    XposedHelpers.findAndHookConstructor(
                            "com.zego.zegoliveroom.constants.ZegoAvConfig",
                            bluedContext.getClassLoader(),
                            int.class, new XC_MethodHook() {
                                @Override
                                protected void beforeHookedMethod(MethodHookParam param) {
                                    param.args[0] = 4; // 720P
                                    Log.d("BluedHook", "设置Zego分辨率");
                                }
                            });
                    // Hook setVideoFPS（修改帧率）
                    XposedHelpers.findAndHookMethod(
                            "com.zego.zegoliveroom.constants.ZegoAvConfig",
                            bluedContext.getClassLoader(),
                            "setVideoFPS",
                            int.class,
                            new XC_MethodHook() {
                                @Override
                                protected void beforeHookedMethod(MethodHookParam param) {
                                    //param.args[0] = 60; // 60fps
                                    Log.d("BluedHook", "设置Zego帧率");
                                }
                            }
                    );

                    // Hook setVideoBitrate（修改码率）
                    XposedHelpers.findAndHookMethod(
                            "com.zego.zegoliveroom.constants.ZegoAvConfig",
                            bluedContext.getClassLoader(),
                            "setVideoBitrate",
                            int.class,
                            new XC_MethodHook() {
                                @Override
                                protected void beforeHookedMethod(MethodHookParam param) {
                                    //param.args[0] = 1500000; // 8000kbps
                                    Log.d("BluedHook", "设置Zego比特率");
                                }
                            }
                    );
                    String jinShanApiStr = FileStorageHelper.readFileFromInternalStorage(
                            AppContainer.getInstance().getBluedContext(),
                            "JinShanApi.txt");
                    if (!jinShanApiStr.isEmpty()) {
                        String[] jinShanApi = jinShanApiStr.split("\n");
                        if (jinShanApi.length == 2) {
                            NetworkManager.jinShanAirScriptSrc = jinShanApi[0];
                            AuthManager.jinShanAirScriptKey = jinShanApi[1];
                        } else {
                            ModuleTools.showBluedToast("金山云文档接口配置读取失败\n开播提醒云端数据无法保存");
                        }
                    }
                    wsServerManager = new WSServerManager(new WSServerManager.WSServerListener() {
                        @Override
                        public void onServerStarted(int port) {
                            ModuleTools.showBluedToast("WS服务已启动在" + port + "端口上");
                        }

                        @Override
                        public void onServerStopped() {
                            ModuleTools.showBluedToast("WS服务已停止");
                        }

                        @Override
                        public void onServerError(String error) {
                            ModuleTools.showBluedToast("WS服务发生了错误：" + error);
                        }

                        @Override
                        public void onClientConnected(String address) {

                        }

                        @Override
                        public void onClientDisconnected(String address) {

                        }

                        @Override
                        public void onMessageReceived(WebSocket conn, String message) {
                            if (message.equals("同步数据")) {
                                try {
                                    // 1. 构建基础响应结构
                                    JSONObject response = new JSONObject();
                                    response.put("msgType", 1995);

                                    // 2. 构建msgExtra部分
                                    JSONObject msgExtra = new JSONObject();
                                    msgExtra.put("msgType", "lotteryRecords");

                                    // 3. 获取并转换文件数据为JSON
                                    JSONObject recordsData = new FileToJsonConverter().convertFilesToJson();
                                    msgExtra.put("msgExtra", recordsData);

                                    // 4. 将msgExtra放入主响应
                                    response.put("msgExtra", msgExtra);
                                    // 5. 广播消息
                                    String jsonResponse = response.toString();
                                    Log.d("WebSocketServer", "Broadcasting records: " + jsonResponse);
                                    wsServerManager.broadcastMessage(jsonResponse);
                                } catch (Exception e) {
                                    Log.e("WebSocketServer", "Error processing sync request", e);
                                }
                            }
                        }
                    });
                }
            });
        }
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resParam) {
        if (resParam.packageName.equals("com.soft.blued")) {
            String modulePath = AppContainer.getInstance().getModulePath();
            XModuleResources moduleRes = XModuleResources.createInstance(modulePath, resParam.res);
            AppContainer.getInstance().setModuleRes(moduleRes);
            resParam.res.hookLayout("com.soft.blued", "layout", "fragment_settings", new XC_LayoutInflated() {
                @SuppressLint({"ResourceType", "SetTextI18n"})
                @Override
                public void handleLayoutInflated(LayoutInflatedParam liParam) {
                    LayoutInflater inflater = (LayoutInflater) liParam.view.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    Context bluedContext = AppContainer.getInstance().getBluedContext();
                    int scrollView1ID = bluedContext.getResources().getIdentifier("scrollView1", "id", bluedContext.getPackageName());
                    ScrollView scrollView = liParam.view.findViewById(scrollView1ID);
                    LinearLayout scrollLinearLayout = (LinearLayout) scrollView.getChildAt(0);
                    LinearLayout mySettingsLayoutAu = (LinearLayout) inflater.inflate(moduleRes.getLayout(R.layout.module_settings_layout), null, false);
                    TextView auCopyTitleTv = mySettingsLayoutAu.findViewById(R.id.settings_name);
                    auCopyTitleTv.setText("复制授权信息(请勿随意泄漏)");
                    mySettingsLayoutAu.setOnClickListener(v -> AuthManager.auHook(true, AppContainer.getInstance().getClassLoader(), bluedContext));
                    LinearLayout moduleSettingsLayout = (LinearLayout) inflater.inflate(moduleRes.getLayout(R.layout.module_settings_layout), null, false);
                    TextView moduleSettingsTitleTv = moduleSettingsLayout.findViewById(R.id.settings_name);
                    moduleSettingsTitleTv.setText("外挂模块设置");
                    moduleSettingsLayout.setOnClickListener(view -> {
                        AlertDialog dialog = getAlertDialog(liParam);
                        Objects.requireNonNull(dialog.getWindow()).setGravity(Gravity.CENTER);
                        dialog.getWindow().setLayout(100, 300);
                        dialog.setOnShowListener(dialogInterface -> {
                            View parentView = dialog.getWindow().getDecorView();
                            parentView.setBackgroundColor(Color.parseColor("#F7F6F7")); // 自定义背景色
                        });
                        dialog.show();

                    });
                    scrollLinearLayout.addView(mySettingsLayoutAu, 0);
                    scrollLinearLayout.addView(moduleSettingsLayout, 1);
                }

                private AlertDialog getAlertDialog(LayoutInflatedParam liParam) {
                    SettingsViewCreator creator = new SettingsViewCreator(liParam.view.getContext());
                    View settingsView = creator.createSettingsView();
                    creator.setOnSwitchCheckedChangeListener((functionId, isChecked) -> {
                        if (functionId == SettingsViewCreator.ANCHOR_MONITOR_LIVE_HOOK) {
                            LiveHook.getInstance(AppContainer.getInstance().getBluedContext()).setAnchorMonitorIvVisibility(isChecked);
                        }
                    });
                    AlertDialog.Builder builder = new AlertDialog.Builder(liParam.view.getContext());
                    builder.setView(settingsView);
                    return builder.create();
                }
            });
        }
    }


    @Override
    public void initZygote(StartupParam startupParam) {
        AppContainer.getInstance().setModulePath(startupParam.modulePath);
    }

    private void initializeSettings() {
        SQLiteManagement dbManager = SQLiteManagement.getInstance();
        dbManager.addOrUpdateSetting(new SettingItem(SettingsViewCreator.USER_INFO_FRAGMENT_NEW_HOOK,
                "个人主页信息扩展",
                true,
                "启用后个人主页将显示额外信息。",
                "",
                ""
        ));

        dbManager.addOrUpdateSetting(new SettingItem(SettingsViewCreator.ANCHOR_MONITOR_LIVE_HOOK,
                "主播开播提醒监听",
                true,
                "开启后直播页右上角将会有\"检\"字图标，可进入开播提醒用户列表页面；注：如果需要使用此功能，请先打开\"个人主页信息扩展\"功能，方可看到主播主页的\"特别关注\"按钮，点击\"特别关注\"按钮即可将需要提醒的主播添加到主播监听列表。",
                "",
                ""
        ));
        dbManager.addOrUpdateSetting(new SettingItem(SettingsViewCreator.PLAYING_ON_LIVE_BASE_MODE_FRAGMENT_HOOK,
                "直播间信息扩展",
                true,
                "开启后直播间将显示额外信息，例如：显示主播的总豆，显示其他用户隐藏的资料信息等功能。",
                "",
                ""
        ));
        dbManager.addOrUpdateSetting(new SettingItem(SettingsViewCreator.LIVE_JOIN_HIDE_HOOK,
                "进入直播间隐身",
                true,
                "开启后进入直播间将会隐身；注：直播间送礼物后可能会看见你的头像，但每次进入直播间不会有任何提示。",
                "",
                ""
        ));
        dbManager.addOrUpdateSetting(new SettingItem(SettingsViewCreator.WS_SERVER,
                "开启WS实时通讯",
                false,
                "需要配合ws客户端",
                "7890",
                "请输入端口号"
        ));
        dbManager.addOrUpdateSetting(new SettingItem(SettingsViewCreator.REC_HEW_HORN,
                "记录飘屏",
                false,
                "记录抽奖飘屏",
                "",
                ""
        ));
        dbManager.addOrUpdateSetting(new SettingItem(SettingsViewCreator.SHIELD_LIKE,
                "屏蔽点赞",
                false,
                "屏蔽直播间自己的点赞，以免误触导致主播看到你。\n" +
                        "注：仅屏蔽发送过程，不会屏蔽本地点赞特效或震动",
                "",
                ""
        ));
        dbManager.addOrUpdateSetting(new SettingItem(SettingsViewCreator.AUTO_LIKE,
                "直播间自动点赞",
                false,
                "进入直播间手动触发一次点赞后，会持续发送点赞消息。\n" +
                        "注：使用此功能需先关闭屏蔽点赞开关，如需停止自动点赞，请退出直播间或关闭小窗。",
                "",
                ""
        ));
        dbManager.addOrUpdateSetting(new SettingItem(SettingsViewCreator.LIVE_AUTO_SEND_MSG,
                "直播间定时发送消息",
                false,
                "在直播间自动发送消息",
                "欢迎各位进入直播间",
                "请输入需要发送的消息内容。"
        ));
        dbManager.addOrUpdateSetting(new SettingItem(SettingsViewCreator.AUTO_ENTER_LIVE,
                "直播结束自动跳转推荐直播间",
                false,
                "开启后当直播结束时，默认会自动进入其他直播间（官方功能）。\n关闭后，将不会自动跳转（适合夜间边听边挂机）",
                "",
                ""
        ));
    }
}