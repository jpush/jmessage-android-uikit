package cn.jmessage.android.uikit;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.sample.application.R;

import cn.jmessage.android.uikit.chatting.ChatActivity;
import cn.jmessage.android.uikit.tools.DialogCreator;
import cn.jmessage.android.uikit.tools.HandleResponseCode;
import cn.jmessage.android.uikit.tools.UserConfig;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.api.BasicCallback;

public class MainActivity extends Activity {

    private final String TARGET_APP_KEY = "4f7aef34fb361292c566a1cd";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        JMessageClient.init(this);
        JMessageClient.setNotificationMode(JMessageClient.NOTI_MODE_NO_NOTIFICATION);
        LinearLayout mCrossAppChatLl;
        LinearLayout mSingleChatLl;
        LinearLayout mGroupChatLl;
        Button mAboutBtn;
        mCrossAppChatLl = (LinearLayout) findViewById(R.id.cross_app_chat_ll);
        mSingleChatLl = (LinearLayout) findViewById(R.id.sing_chat_ll);
        mGroupChatLl = (LinearLayout) findViewById(R.id.group_chat_ll);
        mAboutBtn = (Button) findViewById(R.id.about_btn);

        mCrossAppChatLl.setOnClickListener(listener);
        mSingleChatLl.setOnClickListener(listener);
        mGroupChatLl.setOnClickListener(listener);
        mAboutBtn.setOnClickListener(listener);

        //设置用户信息聊天对象及群聊Id
        UserConfig userConfig = UserConfig.getInstance();
        userConfig.setMyInfo("kkkkk", "kkkkk");
        userConfig.setTargetInfo("0002", "1111");
        userConfig.setGroupId(10049741);


        final Dialog loadingDialog = DialogCreator.createLoadingDialog(this, this.getString(R.string.login));
        loadingDialog.show();
//        JMessageClient.register("user001", "1111", new BasicCallback() {
//            @Override
//            public void gotResult(int status, String desc) {
//                if (status == 0) {
//                    Log.d(TAG, "Register user1 success !");
//                } else {
//                    HandleResponseCode.onHandle(mContext, status, false);
//                }
//            }
//        });
//        JMessageClient.register("user002", "1111", new BasicCallback() {
//            @Override
//            public void gotResult(int status, String desc) {
//                if (status == 0) {
//                    Log.d(TAG, "Register user2 success !");
//                } else {
//                    HandleResponseCode.onHandle(mContext, status, false);
//                }
//            }
//        });
        JMessageClient.login(userConfig.getMyUsername(), userConfig.getMyPassword(), new BasicCallback() {
            @Override
            public void gotResult(int status, String desc) {
                loadingDialog.dismiss();
                if (status == 0) {
                    Log.d("MainActivity", "Login success");
                } else {
                    HandleResponseCode.onHandle(MainActivity.this, status, false);
                }
            }
        });
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            switch (v.getId()) {
                case R.id.cross_app_chat_ll:
                    intent.putExtra("appKey", TARGET_APP_KEY);
                    intent.setClass(MainActivity.this, ChatActivity.class);
                    startActivity(intent);
                    break;
                case R.id.sing_chat_ll:
                    intent.putExtra("isSingle", true);
                    intent.setClass(MainActivity.this, ChatActivity.class);
                    startActivity(intent);
                    break;
                case R.id.group_chat_ll:
                    intent.putExtra("isSingle", false);
                    intent.setClass(MainActivity.this, ChatActivity.class);
                    startActivity(intent);
                    break;
                case R.id.about_btn:
                    intent.setClass(MainActivity.this, AboutActivity.class);
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