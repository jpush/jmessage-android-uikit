package cn.jmessage.android.uikit;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Random;

import cn.jmessage.android.uikit.chatting.BaseActivity;
import cn.jmessage.android.uikit.chatting.ChatActivity;
import cn.jmessage.android.uikit.chatting.utils.DialogCreator;
import cn.jmessage.android.uikit.chatting.utils.HandleResponseCode;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.event.UserLogoutEvent;
import cn.jpush.im.api.BasicCallback;

/**
 * Chatting入口Activity, 可以选择单聊或群聊,并且设置聊天相关的用户信息(通过Intent的方式)
 */

public class DemoActivity extends BaseActivity {

    private static final String TARGET_ID = "targetId";
    private static final String GROUP_ID = "groupId";
    private String mTargetId;
    private long mGroupId;
    private Dialog mDialog;
    private String mMyName;
    private String mMyPassword;
    private final MyHandler myHandler = new MyHandler(this);
    private static final int REGISTER = 200;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jmui_activity_main);
        mContext = this;
        //注册接收消息(成为订阅者), 注册后可以直接重写onEvent方法接收消息
        JMessageClient.registerEventReceiver(this);
        LinearLayout mSingleChatLl;
        LinearLayout mGroupChatLl;
        Button mLoginBtn;
        Button mAboutBtn;
        mSingleChatLl = (LinearLayout) findViewById(R.id.jmui_single_chat_ll);
        mGroupChatLl = (LinearLayout) findViewById(R.id.jmui_group_chat_ll);
        mLoginBtn = (Button) findViewById(R.id.jmui_login_btn);
        mAboutBtn = (Button) findViewById(R.id.jmui_about_btn);

        mSingleChatLl.setOnClickListener(listener);
        mGroupChatLl.setOnClickListener(listener);
        mLoginBtn.setOnClickListener(listener);
        mAboutBtn.setOnClickListener(listener);

        //设置用户信息聊天对象及群聊Id, 此处使用了前缀加上随机生成的4个字符组成用户名, 而聊天对象是提前注册好的
        //关于注册在ReadMe中也有提到
        mMyPassword = "1111";
        mTargetId = "user002";
        mGroupId = 10049741;
        if (JMessageClient.getMyInfo() == null) {
            String username = getUsername(4);
            mMyName = "uikit_demo_" + username;

            final Dialog dialog = DialogCreator.createLoadingDialog(this, this.getString(R.string.jmui_registering));
            dialog.show();
            JMessageClient.register(mMyName, mMyPassword, new BasicCallback() {
                @Override
                public void gotResult(int status, String desc) {
                    dialog.dismiss();
                    if (status == 0) {
                        myHandler.sendEmptyMessage(REGISTER);
                        Toast.makeText(mContext, mContext.getString(R.string.jmui_username) + " " + mMyName
                                + mContext.getString(R.string.jmui_register_success), Toast.LENGTH_SHORT).show();
                    } else {
                        HandleResponseCode.onHandle(mContext, status, false);
                    }
                }
            });
        }
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final Intent intent = new Intent();
            switch (v.getId()) {
                case R.id.jmui_single_chat_ll:
                    if (JMessageClient.getMyInfo() != null) {
                        intent.putExtra(TARGET_ID, mTargetId);
                        intent.setClass(mContext, ChatActivity.class);
                        startActivity(intent);
                    } else {
                        final Dialog dialog = DialogCreator.createLoadingDialog(mContext,
                                DemoActivity.this.getString(R.string.jmui_logging));
                        dialog.show();
                        JMessageClient.login(mMyName, mMyPassword, new BasicCallback() {
                            @Override
                            public void gotResult(int status, String desc) {
                                dialog.dismiss();
                                if (status == 0) {
                                    intent.putExtra(TARGET_ID, mTargetId);
                                    intent.setClass(mContext, ChatActivity.class);
                                    startActivity(intent);
                                } else {
                                    HandleResponseCode.onHandle(mContext, status, false);
                                }
                            }
                        });
                    }
                    break;
                case R.id.jmui_group_chat_ll:
                    if (JMessageClient.getMyInfo() != null) {
                        intent.putExtra(GROUP_ID, mGroupId);
                        intent.setClass(mContext, ChatActivity.class);
                        startActivity(intent);
                    } else {
                        final Dialog dialog = DialogCreator.createLoadingDialog(mContext,
                                mContext.getString(R.string.jmui_logging));
                        dialog.show();
                        JMessageClient.login(mMyName, mMyPassword, new BasicCallback() {
                            @Override
                            public void gotResult(int status, String desc) {
                                dialog.dismiss();
                                if (status == 0) {
                                    intent.putExtra(GROUP_ID, mGroupId);
                                    intent.setClass(mContext, ChatActivity.class);
                                    startActivity(intent);
                                } else {
                                    HandleResponseCode.onHandle(mContext, status, false);
                                }
                            }
                        });

                    }
                    break;
                case R.id.jmui_login_btn:
                    if (JMessageClient.getMyInfo() != null) {
                        mMyName = JMessageClient.getMyInfo().getUserName();
                        final Dialog dialog = DialogCreator.createLoadingDialog(mContext,
                                mContext.getString(R.string.jmui_logging));
                        dialog.show();
                        JMessageClient.login(mMyName, mMyPassword, new BasicCallback() {
                            @Override
                            public void gotResult(int status, String desc) {
                                dialog.dismiss();
                                if (status == 0) {
                                    Toast.makeText(mContext, mMyName + " " + mContext.getString(R.string
                                            .jmui_login_success), Toast.LENGTH_SHORT).show();
                                } else {
                                    HandleResponseCode.onHandle(mContext, status, false);
                                }
                            }
                        });
                    }
                    break;
                case R.id.jmui_about_btn:
                    intent.setClass(mContext, AboutActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    };

    /** 产生一个随机的字符串*/
    public static String getUsername(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int num = random.nextInt(62);
            buf.append(str.charAt(num));
        }
        return buf.toString();
    }

    public void login() {
        final Dialog loadingDialog = DialogCreator.createLoadingDialog(this, this.getString(R.string.jmui_logging));
        loadingDialog.show();
        JMessageClient.login(mMyName, mMyPassword, new BasicCallback() {
            @Override
            public void gotResult(int status, String desc) {
                loadingDialog.dismiss();
                if (status == 0) {
                    Toast.makeText(mContext, mContext.getString(R.string.jmui_login_success), Toast.LENGTH_SHORT).show();
                    Log.d("DemoActivity", "Login success");
                } else {
                    HandleResponseCode.onHandle(mContext, status, false);
                }
            }
        });
    }

    public void onEventMainThread(UserLogoutEvent event) {
        String title = mContext.getString(R.string.jmui_user_logout_dialog_title);
        String msg = mContext.getString(R.string.jmui_user_logout_dialog_message);
        mDialog = DialogCreator.createBaseCustomDialog(mContext, title, msg, onClickListener);
        mDialog.getWindow().setLayout((int) (0.8 * mWidth), WindowManager.LayoutParams.WRAP_CONTENT);
        mDialog.show();
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mDialog.dismiss();
        }
    };


    @Override
    protected void onPause() {
        JPushInterface.onPause(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        JPushInterface.onResume(this);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        JMessageClient.unRegisterEventReceiver(this);
        super.onDestroy();
    }

    private static class MyHandler extends Handler {

        private WeakReference<DemoActivity> mActivity;

        public MyHandler(DemoActivity activity) {
            mActivity = new WeakReference<DemoActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            DemoActivity demoActivity = mActivity.get();
            if (demoActivity != null) {
                switch (msg.what) {
                    case REGISTER:
                        demoActivity.login();
                        break;
                }
            }
        }
    }
}