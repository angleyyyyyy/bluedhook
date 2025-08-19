package com.zjfgh.bluedhook.simple;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.XModuleResources;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChatHook {
    private final ClassLoader classLoader;
    private static ChatHook instance;
    private final WeakReference<Context> contextRef;
    private final XModuleResources modRes;

    // 使用 WeakReference 持有 TextView，避免内存泄漏
    private WeakReference<TextView> tvRecallMsgRef;
    private WeakReference<TextView> tvScreenshotProtectionRef;
    private WeakReference<TextView> tvChatReadMsgRef;

    private static final String TAG = "BluedHook-ChatHook";

    private ChatHook(Context context, XModuleResources modRes) {
        this.contextRef = new WeakReference<>(context);
        this.classLoader = context.getClassLoader();
        this.modRes = modRes;
        messageRecallHook();
        hookMsgChattingTitle();
        snapChatHook();
        chatHelperV4MdHook();
        chatReadHook();
        chatProtectScreenshotHook();
        testHook();
    }

    private void testHook() {
        XposedHelpers.findAndHookMethod("com.soft.blued.ui.setting.fragment.CollectionListFragment", classLoader, "a", "android.view.View", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);

            }

            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Object b = param.args[0];
                @SuppressLint("DiscouragedApi") int tv_collection_numId = Objects.requireNonNull(getSafeContext()).getResources().getIdentifier("tv_collection_num", "id", getSafeContext().getPackageName());
                Log.e("BluedHook", "tv_collection_numId" + tv_collection_numId);
                TextView textView = (TextView) XposedHelpers.callMethod(b, "findViewById", tv_collection_numId);
                LinearLayout linearLayout = (LinearLayout) textView.getParent();
                TextView textView1 = new TextView(getSafeContext().getApplicationContext());
                textView1.setPadding(ModuleTools.dpToPx(30), ModuleTools.dpToPx(10), ModuleTools.dpToPx(30), ModuleTools.dpToPx(10));
                // 将dp值转换为像素值
                int marginLeft = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        20,
                        getSafeContext().getResources().getDisplayMetrics()
                );

                ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(marginLeft, 0, marginLeft, 0);
                textView1.setLayoutParams(params);
                textView1.setText("查看收藏用户更多信息");
                textView1.setTextColor(Color.parseColor("#00FFAA"));
                textView1.setBackground(modRes.getDrawable(R.drawable.tech_level_bg, null));
                linearLayout.addView(textView1);
                textView1.setOnClickListener(v -> {
                    Activity activity = (Activity) textView.getContext();
                    LayoutInflater inflater = (LayoutInflater) textView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    XmlResourceParser userMoreInfoListLayoutRes = modRes.getLayout(R.layout.user_more_info_tech_list_layout);
                    LinearLayout userMoreInfoListLayout = (LinearLayout) inflater.inflate(userMoreInfoListLayoutRes, null, false);
                    userMoreInfoListLayout.setBackground(modRes.getDrawable(R.drawable.bg_tech_space, null));
                    CustomPopupWindow customPopupWindow = new CustomPopupWindow(activity, userMoreInfoListLayout, Color.parseColor("#FF0A121F"));
                    customPopupWindow.setBackgroundDrawable(modRes.getDrawable(R.drawable.bg_tech_space, null));
                    customPopupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
                    customPopupWindow.showAtCenter();
                    LinearLayout recyclerViewLayout = userMoreInfoListLayout.findViewById(R.id.recyclerViewLayout);
                    RecyclerView recyclerView = new RecyclerView(textView.getContext());
                    recyclerViewLayout.addView(recyclerView);
                    recyclerView.setLayoutManager(new LinearLayoutManager(textView.getContext()));
                    TechUserAdapter adapter = new TechUserAdapter(new JSONArray(), textView.getContext(), modRes);
                    recyclerView.setAdapter(adapter);
                    NetworkManager.getInstance().getAsync(NetworkManager.getUserCollectApi(), AuthManager.auHook(false, classLoader, contextRef.get()), new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {

                        }

                        @SuppressLint("UseCompatLoadingForDrawables")
                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            try {
                                JSONObject root = new JSONObject(response.body().string());
                                String en_data = root.getString("en_data");
                                JSONObject jsonObject = new JSONObject(ModuleTools.enDataDecrypt(en_data, AppContainer.getInstance().getBytes()));
                                JSONArray dataArr = jsonObject.getJSONArray("data");
                                if (dataArr != null) {
                                    v.post(() -> adapter.updateData(dataArr));
                                }

                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                });
            }
        });

        XposedHelpers.findAndHookMethod("com.blued.android.http.encode.utils.c", classLoader, "I111I1lI1I1", "java.lang.String", "byte[]", "java.lang.String", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                AppContainer.getInstance().setBytes((byte[]) param.args[1]);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });
        Class<?> splashFragment = XposedHelpers.findClass(
                "com.soft.blued.ui.welcome.SerialSplashFragment",
                classLoader);
        XposedHelpers.findAndHookMethod(splashFragment, "d", String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                XposedHelpers.callMethod(param.thisObject, "w");
                Log.w(TAG, "广告拜拜之直接跳转");
                ModuleTools.showToast("尝试跳过广告", Toast.LENGTH_LONG);
                param.setResult(null);
            }
        });

        // 方法二：清空广告列表
        XposedHelpers.findAndHookMethod(splashFragment, "z", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                List<?> adList = (List<?>) XposedHelpers.getObjectField(param.thisObject, "z");
                adList.clear();
                XposedHelpers.callMethod(param.thisObject, "w");
                Log.w(TAG, "广告拜拜之清空广告列表");
                ModuleTools.showToast("尝试跳过广告", Toast.LENGTH_LONG);
            }
        });

        // 方法三：拦截广告平台
        XposedHelpers.findAndHookMethod(splashFragment, "g",
                XposedHelpers.findClass("com.blued.android.module.common.login.model.BluedADExtra", classLoader),
                XposedHelpers.findClass("com.soft.blued.ui.welcome.SerialSplashFragment$SplashAdListener", classLoader),
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Object listener = param.args[1];
                        XposedHelpers.callMethod(listener, "onAdLoaded");
                        Log.w(TAG, "广告拜拜载入完成就拜拜");
                        ModuleTools.showToast("尝试跳过广告", Toast.LENGTH_LONG);
                        param.setResult(null);
                    }
                });
    }

    // 获取单例实例（仍然保留单例，但内部使用 WeakReference）
    public static synchronized ChatHook getInstance(Context context, XModuleResources modRes) {
        if (instance == null) {
            instance = new ChatHook(context, modRes);
        }
        return instance;
    }

    public void messageRecallHook() {
        Class<?> PushMsgPackage = XposedHelpers.findClass("com.blued.android.chat.core.pack.PushMsgPackage", classLoader);
        XposedHelpers.findAndHookMethod("com.blued.android.chat.core.worker.chat.Chat", classLoader, "receiveOrderMessage",
                PushMsgPackage, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        Object pushMsgPackage = param.args[0];
                        short msgType = XposedHelpers.getShortField(pushMsgPackage, "msgType");
                        if (msgType == 55) {
                            // 获取原始消息ID和会话信息
                            long msgId = XposedHelpers.getLongField(pushMsgPackage, "msgId");
                            short sessionType = XposedHelpers.getShortField(pushMsgPackage, "sessionType");
                            long sessionId = XposedHelpers.getLongField(pushMsgPackage, "sessionId");
                            Class<?> ChatManager = XposedHelpers.findClass("com.blued.android.chat.ChatManager", classLoader);
                            //noinspection SpellCheckingInspection
                            Object dbOperationImpl = XposedHelpers.getStaticObjectField(ChatManager, "dbOperImpl");
                            // 获取原始消息对象
                            Object originalMsg = XposedHelpers.callMethod(
                                    dbOperationImpl,
                                    "findMsgData",
                                    sessionType,
                                    sessionId,
                                    msgId,
                                    0L
                            );
                            if (originalMsg != null) {
                                // 获取原始消息类型
                                short originalType = XposedHelpers.getShortField(originalMsg, "msgType");
                                // 获取原始消息发送者昵称
                                String originalNickName = (String) XposedHelpers.getObjectField(originalMsg, "fromNickName");
                                // 获取原始消息内容
                                String originalContent = (String) XposedHelpers.getObjectField(originalMsg, "msgContent");
                                if (originalType == 55) {
                                    Log.e("BluedHook", "原始消息已被成功撤回，无法恢复。");
                                    return;
                                }
                                // 同时修改内存和数据库中的消息类型
                                XposedHelpers.setShortField(pushMsgPackage, "msgType", originalType);
                                XposedHelpers.setShortField(originalMsg, "msgType", originalType);
                                XposedHelpers.callMethod(dbOperationImpl, "updateChattingModel", originalMsg);
                                // 处理不同类型的闪消息
                                switch (originalType) {
                                    case 1:
                                        ModuleTools.showBluedToast("[" + originalNickName + "]撤回了消息：\n" + originalContent);
                                        break;
                                    case 2:
                                        ModuleTools.showBluedToast("[" + originalNickName + "]撤回了图片");
                                        break;
                                    case 5:
                                        ModuleTools.showBluedToast("[" + originalNickName + "]撤回了视频");
                                        break;
                                    case 24: // 闪照
                                        ModuleTools.showBluedToast("[" + originalNickName + "]撤回了闪照");
                                        break;
                                    case 25: // 闪照视频
                                        ModuleTools.showBluedToast("[" + originalNickName + "]撤回了闪照视频");
                                        break;
                                }
                            }
                        }
                        param.setResult(null);
                    }
                });
    }

    public void chatHelperV4MdHook() {
        Class<?> ChattingModel = XposedHelpers.findClass("com.blued.android.chat.model.ChattingModel", classLoader);
        XposedHelpers.findAndHookMethod("com.soft.blued.ui.msg.controller.tools.ChatHelperV4", classLoader, "b", android.content.Context.class, ChattingModel, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                handleChatMessage(param, "b");
            }

            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });
        XposedHelpers.findAndHookMethod("com.soft.blued.ui.msg.controller.tools.ChatHelperV4", classLoader, "c", android.content.Context.class, ChattingModel, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                handleChatMessage(param, "c");
            }
        });
        XposedHelpers.findAndHookMethod("com.soft.blued.ui.msg.controller.tools.ChatHelperV4", classLoader, "d", android.content.Context.class, ChattingModel, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                handleChatMessage(param, "d");
            }

            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });
        XposedHelpers.findAndHookMethod("com.soft.blued.ui.msg.controller.tools.ChatHelperV4", classLoader, "e", android.content.Context.class, ChattingModel, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                handleChatMessage(param, "e");
            }

            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });
        XposedHelpers.findAndHookMethod("com.soft.blued.ui.msg.controller.tools.ChatHelperV4", classLoader, "f", android.content.Context.class, ChattingModel, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                handleChatMessage(param, "f");
            }

            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });

        XposedHelpers.findAndHookMethod("com.soft.blued.ui.msg.presenter.MsgChattingPresent", classLoader, "c", ChattingModel, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Log.i("BluedHook", "开始发送消息" + param.args[0]);
            }

            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });
    }

    public void snapChatHook() {
        try {
            XposedHelpers.findAndHookMethod(
                    "com.soft.blued.ui.msg.presenter.MsgChattingPresent",
                    classLoader,
                    "F",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            handleFlashMessages(param);
                            getTvRecallMsg().setVisibility(View.VISIBLE);
                        }
                    });
        } catch (Throwable e) {
            Log.e("BluedHook", "Hook MsgChattingPresent.E()方法失败", e);
        }
    }

    /**
     * 处理闪照和闪照视频消息
     */
    private void handleFlashMessages(XC_MethodHook.MethodHookParam param) {
        try {
            Object thisObject = param.thisObject;
            Object iMsgChattingView = XposedHelpers.getObjectField(thisObject, "u");
            Object msgChattingAdapter = XposedHelpers.callMethod(iMsgChattingView, "E");
            Object a = XposedHelpers.callMethod(msgChattingAdapter, "a");
            if (!(a instanceof List)) {
                return;
            }

            for (Object msg : (List<?>) a) {
                processSingleMessage(msg);
            }
        } catch (Throwable e) {
            Log.e("BluedHook", "处理闪照消息时出错", e);
        }
    }

    /**
     * 处理单条消息
     */
    private void processSingleMessage(Object msgObj) {
        try {
            // 获取消息类型和内容
            short msgType = XposedHelpers.getShortField(msgObj, "msgType");
            String msgContent = (String) XposedHelpers.getObjectField(msgObj, "msgContent");
            Log.i("BluedHook", "processSingleMessage: \n" + msgType + ", 原始内容: " + msgContent);
            Class<?> fieldType = XposedHelpers.findField(msgObj.getClass(), "msgType").getType();
            // 检查字段类型是否为short
            if (!(fieldType == short.class || fieldType == Short.class)) {
                return;
            }
            boolean isFromSelf = (boolean) XposedHelpers.callMethod(msgObj, "isFromSelf");
            if (isFromSelf) {
                Log.i("BluedHook", "消息来自自己，跳过处理");
                return;
            }
            // 处理不同类型的闪图消息
            switch (msgType) {
                case 24: // 闪照
                    convertFlashMessage(msgObj, (short) 2, msgContent, "照片");
                    break;
                case 25: // 闪照视频
                    convertFlashMessage(msgObj, (short) 5, msgContent, "视频");
                    break;
                case 55: // 如果已经被撤回，但本地已收到消息，那么处理为闪照普通消息
                    convertFlashMessage(msgObj, (short) 1, msgContent + "!o.png", "普通消息");
                    break;
            }
        } catch (Throwable e) {
            Log.e("BluedHook", "处理单条消息时出错", e);
        }
    }

    /**
     * 转换闪消息为普通消息
     *
     * @param msgObj           消息对象s
     * @param newType          转换后的消息类型
     * @param encryptedContent 加密的内容
     * @param typeName         类型名称(用于日志)
     */
    private void convertFlashMessage(Object msgObj, short newType, String encryptedContent, String typeName) {
        try {
            // 转换消息类型
            short msgType = XposedHelpers.getShortField(msgObj, "msgType");

            if (msgType == 55) {
                XposedHelpers.setShortField(msgObj, "msgType", newType);
                XposedHelpers.setAdditionalInstanceField(msgObj, "oldMsgType", msgType);
            } else {
                XposedHelpers.setShortField(msgObj, "msgType", newType);
                // 新增字段以存储原始消息类型
                XposedHelpers.setAdditionalInstanceField(msgObj, "oldMsgType", msgType);
                // 解密内容
                String decryptedContent = ModuleTools.AesDecrypt(encryptedContent);
                XposedHelpers.setObjectField(msgObj, "msgContent", decryptedContent);
                Log.i("BluedHook", "已转换闪" + typeName + "为普通" + typeName + ": " + decryptedContent);
            }

        } catch (Throwable e) {
            Log.e("BluedHook", "转换闪" + typeName + "失败", e);
        }
    }

    public static class ChatContent {
        public int msgType;
        public String fromNickName;
        public String extraMsg;
        public String msgContent;
        public long sessionId;
        public long fromId;
        public String fromAvatar;
    }

    // 创建统一的处理方法
    private void handleChatMessage(XC_MethodHook.MethodHookParam param, String methodTag) {
        // 参数校验
        if (param.args == null || param.args.length < 2) {
            Log.e("BluedHook", methodTag + "-参数无效");
            return;
        }
        try {
            Object chattingModel = param.args[1];
            // 使用一次性反射获取所有字段
            ChatContent chatContent = new ChatContent();
            chatContent.msgType = XposedHelpers.getIntField(chattingModel, "msgType");
            chatContent.fromNickName = (String) XposedHelpers.getObjectField(chattingModel, "fromNickName");
            chatContent.extraMsg = (String) XposedHelpers.callMethod(chattingModel, "getMsgExtra");
            chatContent.msgContent = (String) XposedHelpers.getObjectField(chattingModel, "msgContent");
            chatContent.sessionId = XposedHelpers.getLongField(chattingModel, "sessionId");
            chatContent.fromId = XposedHelpers.getLongField(chattingModel, "fromId");
            chatContent.fromAvatar = (String) XposedHelpers.getObjectField(chattingModel, "fromAvatar");
            if (!chatContent.extraMsg.isEmpty()) {
                try {
                    JSONObject json = new JSONObject(chatContent.extraMsg);
                    // 检查字段是否存在
                    boolean hasCustomPushContent = json.has("custom_push_content");
                    boolean hasLid = json.has("lid");
                    // 如果字段存在，获取值
                    if (hasCustomPushContent) {
                        String customPushContent = json.getString("custom_push_content");
                        if (hasLid) {
                            int lid = json.getInt("lid");
                            if (lid > 0) {
                                VoiceTTS.getInstance(getSafeContext()).speakAdd(customPushContent);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("JSON 解析错误: " + e.getMessage());
                }
            }
            // 记录日志
            Log.i("BluedHook", methodTag + "-消息类型:" + chatContent.msgType);
            Log.i("BluedHook", methodTag + "-附加消息:" + chatContent.extraMsg);
            Log.i("BluedHook", methodTag + "-发送方昵称:" + chatContent.fromNickName);
            Log.i("BluedHook", methodTag + "-消息内容:" + chatContent.msgContent);
            Log.i("BluedHook", methodTag + "-会话ID:" + chatContent.sessionId);
            Log.i("BluedHook", methodTag + "-发送方ID:" + chatContent.fromId);
            // 消息类型处理
            String toastMsg;
            switch (chatContent.msgType) {
                case 2:
                    toastMsg = "收到[" + chatContent.fromNickName + "]的私信图片";
                    break;
                case 5:
                    toastMsg = "收到[" + chatContent.fromNickName + "]的私信视频";
                    break;
                case 24:
                    toastMsg = "收到[" + chatContent.fromNickName + "]的私信闪图\n" +
                            "已改为普通图片";
                    XposedHelpers.setIntField(chattingModel, "msgType", 2);
                    break;
                case 25:
                    XposedHelpers.setIntField(chattingModel, "msgType", 5);
                    XposedHelpers.setObjectField(chattingModel, "msgContent", ModuleTools.AesDecrypt(chatContent.msgContent));
                    toastMsg = "收到[" + chatContent.fromNickName + "]的私信视频闪图\n" +
                            "已改为普通视频";
                    break;
                default:
                    toastMsg = "收到[" + chatContent.fromNickName + "]的私信：" + chatContent.msgContent;
            }

            if (toastMsg != null) {
                ModuleTools.showToast(toastMsg, Toast.LENGTH_LONG);
            }

        } catch (Exception e) {
            Log.e("BluedHook", methodTag + "-处理消息异常", e);
        }
    }

    public void chatReadHook() {
        XposedHelpers.findAndHookMethod("io.grpc.MethodDescriptor", classLoader, "generateFullMethodName", String.class, String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                String serviceName = (String) param.args[0];
                String methodName = (String) param.args[1];
                if (serviceName.equals("com.blued.im.private_chat.Receipt") && methodName.equals("Read")) {
                    param.args[0] = "";
                    param.args[1] = "";
                    //ModuleTools.showBluedToast("已开启悄悄查看消息功能");
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });
    }

    public void chatProtectScreenshotHook() {
        XposedHelpers.findAndHookMethod("com.soft.blued.ui.msg.MsgChattingFragment", classLoader, "c", boolean.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                if ((boolean) param.args[0]) {
                    param.args[0] = false;
                    getTvScreenshotProtection().setVisibility(View.VISIBLE);
                    //ModuleTools.showBluedToast("对方已开启聊天截屏保护功能(无法截图聊天界面)，已关闭此功能");
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });
    }

    public TextView getTvScreenshotProtection() {
        return tvScreenshotProtectionRef != null ? tvScreenshotProtectionRef.get() : null;
    }

    // 获取 TextView（如果已被回收则返回 null）
    public TextView getTvRecallMsg() {
        return tvRecallMsgRef != null ? tvRecallMsgRef.get() : null;
    }

    public TextView getTvChatReadMsg() {
        return tvChatReadMsgRef != null ? tvChatReadMsgRef.get() : null;
    }

    private void hookMsgChattingTitle() {
        XposedHelpers.findAndHookMethod("com.soft.blued.ui.msg.MsgChattingFragment", classLoader, "ah", new XC_MethodHook() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                View n = (View) XposedHelpers.getObjectField(param.thisObject, "o");
                @SuppressLint("DiscouragedApi") int msg_chatting_titleId = getSafeContext().getResources().getIdentifier("msg_chatting_title", "id", getSafeContext().getPackageName());
                View findViewById = n.findViewById(msg_chatting_titleId);
                @SuppressLint("DiscouragedApi") int ll_center_distanceId = getSafeContext().getResources().getIdentifier("ll_center_distance", "id", getSafeContext().getPackageName());
                LinearLayout ll_center_distance = findViewById.findViewById(ll_center_distanceId);

                TagLayout tlTitle = new TagLayout(n.getContext());

                // 使用 WeakReference 存储 TextView
                TextView tvChatReadMsg = tlTitle.addTextView("悄悄查看", 9, modRes.getDrawable(R.drawable.bg_orange, null));
                tvChatReadMsgRef = new WeakReference<>(tvChatReadMsg);
                getTvChatReadMsg().setText("悄悄查看");
                TextView tvRecallMsg = tlTitle.addTextView("防撤回", 9, modRes.getDrawable(R.drawable.bg_gradient_orange, null));
                tvRecallMsgRef = new WeakReference<>(tvRecallMsg);
                tvRecallMsg.setVisibility(View.GONE);

                TextView tvScreenshotProtection = tlTitle.addTextView("私信截图", 9, modRes.getDrawable(R.drawable.bg_rounded, null));
                tvScreenshotProtectionRef = new WeakReference<>(tvScreenshotProtection);
                tvScreenshotProtection.setVisibility(View.GONE);

                ll_center_distance.addView(tlTitle);
            }
        });
    }

    // 使用 WeakReference 获取 Context
    private Context getSafeContext() {
        return contextRef != null ? contextRef.get() : null;
    }
}
