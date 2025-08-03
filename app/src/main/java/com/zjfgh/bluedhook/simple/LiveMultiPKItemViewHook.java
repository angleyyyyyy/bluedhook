package com.zjfgh.bluedhook.simple;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.XModuleResources;
import android.content.res.XmlResourceParser;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class LiveMultiPKItemViewHook {
    private final ClassLoader classLoader;
    private static LiveMultiPKItemViewHook instance;
    private final XModuleResources modRes;
    private final HashMap<String, MsgSenderAnchor> hmSendMsgAnchor;
    private boolean isMultiPkStart = false;

    private LiveMultiPKItemViewHook(Context context, XModuleResources modRes) {
        this.classLoader = context.getClassLoader();
        this.modRes = modRes;
        hmSendMsgAnchor = new HashMap<>();
        setDataHook();
        viewHook();
    }

    // 获取单例实例
    public static synchronized LiveMultiPKItemViewHook getInstance(Context context, XModuleResources modRes) {
        if (instance == null) {
            instance = new LiveMultiPKItemViewHook(context, modRes);
        }
        return instance;
    }

    public void setMultiPkStart(boolean multiPkStart) {
        isMultiPkStart = multiPkStart;
    }

    public void setDataHook() {
        XposedHelpers.findAndHookMethod(
                "com.blued.android.module.live_china.view.LiveMultiPKItemView",
                AppContainer.getInstance().getClassLoader(), "setData",
                "com.blued.android.module.live_china.model.LiveInviteUserModel",
                new XC_MethodHook() {
                    @SuppressLint("UseCompatLoadingForDrawables")
                    @Override
                    protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        Object liveInviteUserModel = param.args[0];
                        isMultiPkStart = true;
                        ModuleTools.showBluedToast("多人PK弹幕同步功能开启");
                        String name = (String) XposedHelpers.getObjectField(liveInviteUserModel, "name");
                        String uid = (String) XposedHelpers.getObjectField(liveInviteUserModel, "uid");
                        String lid = (String) XposedHelpers.getObjectField(liveInviteUserModel, "lid");
                        String avatar = (String) XposedHelpers.getObjectField(liveInviteUserModel, "avatar");
                        Object liveMultiConnectionItemViewBinding = XposedHelpers.callMethod(param.thisObject, "getViewBinding");
                        FrameLayout frameLayout = (FrameLayout) XposedHelpers.callMethod(liveMultiConnectionItemViewBinding, "getRoot", new Object[0]);
                        LayoutInflater inflater = (LayoutInflater) frameLayout.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        XmlResourceParser llBackRoundParser = modRes.getLayout(R.layout.live_room_sysn_msg);
                        LinearLayout llBackRound = (LinearLayout) inflater.inflate(llBackRoundParser, null, false);
                        FrameLayout flBackRound = llBackRound.findViewById(R.id.fl_live_room_bg);
                        flBackRound.setBackground(modRes.getDrawable(R.drawable.tech_bg_default, null));
                        TextView tvSyncTitle = llBackRound.findViewById(R.id.tv_live_room_sync_title);
                        frameLayout.addView(llBackRound);
                        MsgSenderAnchor msgSenderAnchor = new MsgSenderAnchor();
                        msgSenderAnchor.name = name;
                        msgSenderAnchor.lid = lid;
                        msgSenderAnchor.uid = uid;
                        msgSenderAnchor.avatar = avatar;
                        msgSenderAnchor.tvSyncTitle = tvSyncTitle;
                        msgSenderAnchor.flBackRound = flBackRound;
                        if (lid.equals(String.valueOf(LiveMsgSendManagerHook.getInstance().getMainLid()))) {
                            //当前lid和主lid相同即表示,列表要重新刷新了
                            cleanUser();
                            //刷新时 默认选中当前主播
                            msgSenderAnchor.isChecked = true;
                            flBackRound.setBackground(modRes.getDrawable(R.drawable.tech_bg_selected, null));
                            hmSendMsgAnchor.put(msgSenderAnchor.lid, msgSenderAnchor);
                            // 获取当前的LayoutParams
                            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) flBackRound.getLayoutParams();
                            // 设置外边距（left, top, right, bottom，单位是px）
                            params.setMargins(ModuleTools.dpToPx(2), ModuleTools.dpToPx(120), ModuleTools.dpToPx(2), ModuleTools.dpToPx(2));
                            // 重新应用LayoutParams
                            flBackRound.setLayoutParams(params);
                        }
                        flBackRound.setTag(msgSenderAnchor);
                        flBackRound.setOnClickListener(v -> {
                            MsgSenderAnchor tempSenderAnchor = (MsgSenderAnchor) v.getTag();
                            if (!tempSenderAnchor.isChecked) {
                                tempSenderAnchor.isChecked = true;
                                flBackRound.setBackground(modRes.getDrawable(R.drawable.tech_bg_selected, null));
                                hmSendMsgAnchor.put(tempSenderAnchor.lid, tempSenderAnchor);
                                Log.i("BluedHook", "添加用户:" + tempSenderAnchor.name + "-LID:" + tempSenderAnchor.lid);
                            } else {
                                tempSenderAnchor.isChecked = false;
                                removeUser(tempSenderAnchor.lid);
                                flBackRound.setBackground(modRes.getDrawable(R.drawable.tech_bg_default, null));
                            }
                        });
                    }
                });
    }

    public void removeUser(String lid) {
        hmSendMsgAnchor.remove(lid);
        Log.d("BluedHook", "移除用户LID：" + lid + "-列表数量:" + hmSendMsgAnchor.size());
    }

    public void cleanUser() {
        hmSendMsgAnchor.clear();
        Log.d("BluedHook", "清空用户：" + hmSendMsgAnchor.size());
    }

    public void viewHook() {
        XposedHelpers.findAndHookMethod("com.blued.android.module.live_china.msg.GrpcMsgSender",
                classLoader, "a",
                "java.lang.String",
                boolean.class,
                "com.blued.android.module.live_china.model.LiveZanExtraModel$EmojiModel",
                "com.blued.android.module.live_china.msg.SendMsgListener", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        String msg = (String) param.args[0];
                        // 判断是否正在发送消息，避免无限循环
                        Log.i("BluedHook", "isSenderLiveMsg->" + isSenderLiveMsg);
                        if (!isSenderLiveMsg) {
                            handleMultiRoomMessage(msg);
                            MsgSenderAnchor isSelf = hmSendMsgAnchor.get(String.valueOf(LiveMsgSendManagerHook.getInstance().getMainLid()));
                            Log.i("BluedHook", "isSelf->" + isSelf + "|isMultiPkStart" + isMultiPkStart);
                            if (isMultiPkStart) {
                                if (isSelf == null) {
                                    param.setResult(null);
                                }
                            }
                        }
                    }
                });
    }

    private boolean isSenderLiveMsg = false;

    // 独立出来的消息处理方法
    private void handleMultiRoomMessage(String msg) {
        new Thread(() -> {  // 在新线程中处理避免阻塞主线程
            try {
                isSenderLiveMsg = true;
                Log.e("BluedHook", "size:" + hmSendMsgAnchor.size());
                Object liveRoomData = LiveMsgSendManagerHook.getInstance().getLiveRoomData();
                Object liveRoomManager = LiveMsgSendManagerHook.getInstance().getLiveRoomManager();
                long originalLid = XposedHelpers.getLongField(liveRoomData, "lid");
                // 遍历所有房间发送消息
                for (Map.Entry<String, MsgSenderAnchor> entry : hmSendMsgAnchor.entrySet()) {
                    String key = entry.getKey();
                    MsgSenderAnchor msgSenderAnchor = entry.getValue();
                    if (msgSenderAnchor.lid.equals(String.valueOf(LiveMsgSendManagerHook.getInstance().getMainLid()))) {
                        Log.i("BluedHook", "跳过自身直播间");
                        continue;
                    }
                    // 切换到目标房间
                    XposedHelpers.setLongField(liveRoomData, "lid", Long.parseLong(msgSenderAnchor.lid));
                    XposedHelpers.callMethod(liveRoomManager, "a", liveRoomData);
                    Log.e("BluedHook", "发送到房间: " + key + ", 名称: " + msgSenderAnchor.name);
                    // 发送消息
                    LiveMsgSendManagerHook.getInstance().startSendMsg(msg);
                }
                // 恢复原始房间
                XposedHelpers.setLongField(liveRoomData, "lid", originalLid);
                XposedHelpers.callMethod(liveRoomManager, "a", liveRoomData);
                isSenderLiveMsg = false;
            } catch (Exception e) {
                Log.e("BluedHook", "处理多房间消息出错", e);
            }
        }).start();
    }

    public static class MsgSenderAnchor {
        public View flBackRound;
        public boolean isChecked;
        public View tvSyncTitle;
        public String uid = "";
        public String name = "";
        public String avatar = "";
        public String lid = "";
    }
}
