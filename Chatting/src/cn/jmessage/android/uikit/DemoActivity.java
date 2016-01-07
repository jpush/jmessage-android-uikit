package cn.jmessage.android.uikit;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import cn.jmessage.android.uikit.chatting.ChatActivity;
import cn.jmessage.android.uikit.chatting.utils.DialogCreator;
import cn.jmessage.android.uikit.chatting.utils.HandleResponseCode;
import cn.jmessage.android.uikit.chatting.utils.SharePreferenceManager;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.api.BasicCallback;

/**
 * Chatting入口Activity, 初始化JMessage-sdk, 可以选择单聊或群聊,并且设置聊天相关的用户信息(通过Intent的方式)
 */

public class DemoActivity extends Activity {

    private static final String TARGET_ID = "targetId";
    private static final String GROUP_ID = "groupId";
    private static final String JCHAT_CONFIGS = "JChat_configs";
    private String mTargetId;
    private long mGroupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.jmui_activity_main);
        //初始化JMessage-sdk
        JMessageClient.init(this);
        //设置Notification的模式
        JMessageClient.setNotificationMode(JMessageClient.NOTI_MODE_NO_NOTIFICATION);
        //初始化SharePreference
        SharePreferenceManager.init(this, JCHAT_CONFIGS);
        LinearLayout mSingleChatLl;
        LinearLayout mGroupChatLl;
        Button mAboutBtn;
        mSingleChatLl = (LinearLayout) findViewById(R.id.sing_chat_ll);
        mGroupChatLl = (LinearLayout) findViewById(R.id.group_chat_ll);
        mAboutBtn = (Button) findViewById(R.id.about_btn);

        mSingleChatLl.setOnClickListener(listener);
        mGroupChatLl.setOnClickListener(listener);
        mAboutBtn.setOnClickListener(listener);

        //设置用户信息聊天对象及群聊Id, 此处使用了此AppKey下提前注册的两个用户和一个群组,关于注册用户在ReadMe中有提到
        String myName = "user001";
        String myPassword = "1111";
        mTargetId = "user002";
        mGroupId = 10049741;

        final Dialog loadingDialog = DialogCreator.createLoadingDialog(this, this.getString(R.string.login));
        loadingDialog.show();
        JMessageClient.login(myName, myPassword, new BasicCallback() {
            @Override
            public void gotResult(int status, String desc) {
                loadingDialog.dismiss();
                if (status == 0) {
                    Log.d("DemoActivity", "Login success");
                } else {
                    HandleResponseCode.onHandle(DemoActivity.this, status, false);
                }
            }
        });
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            switch (v.getId()) {
                case R.id.sing_chat_ll:
                    intent.putExtra("isSingle", true);
                    intent.putExtra(TARGET_ID, mTargetId);
                    intent.setClass(DemoActivity.this, ChatActivity.class);
                    startActivity(intent);
                    break;
                case R.id.group_chat_ll:
                    intent.putExtra("isSingle", false);
                    intent.putExtra(GROUP_ID, mGroupId);
                    intent.setClass(DemoActivity.this, ChatActivity.class);
                    startActivity(intent);
                    break;
                case R.id.about_btn:
                    intent.setClass(DemoActivity.this, AboutActivity.class);
                    startActivity(intent);
                    break;
            }
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
}