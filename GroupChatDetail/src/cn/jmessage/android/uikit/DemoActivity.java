package cn.jmessage.android.uikit;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import cn.jmessage.android.uicomponents.R;
import cn.jmessage.android.uikit.groupchatdetail.BaseActivity;
import cn.jmessage.android.uikit.groupchatdetail.DialogCreator;
import cn.jmessage.android.uikit.groupchatdetail.GroupDetailActivity;
import cn.jmessage.android.uikit.groupchatdetail.HandleResponseCode;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.event.UserLogoutEvent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.api.BasicCallback;

/**
 * 入口Activity,主要是配置用户信息和群组Id(通过Intent传递到GroupDetailActivity)
 */

public class DemoActivity extends BaseActivity {

    private static final String TAG = "DemoActivity";
    private static final String GROUP_ID = "groupId";
    private static final String MY_USERNAME = "myUsername";

    private Dialog mLoadingDialog;
    private Button mGroupDetailBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jmui_activity_main);
        JMessageClient.registerEventReceiver(this);
        mGroupDetailBtn = (Button) findViewById(R.id.group_detail_btn);

        mLoadingDialog = DialogCreator.createLoadingDialog(this, this.getString(R.string.jmui_logging));
        mLoadingDialog.show();
        //设置用户信息及群聊Id, 此处使用了此AppKey下提前注册的用户和群组,关于注册用户在ReadMe中有提到
        final String myName = "user001";
        String myPassword = "1111";
        final long groupId = 10053517;
        //登录
        JMessageClient.login(myName, myPassword, new BasicCallback() {
            @Override
            public void gotResult(int status, String desc) {
                mLoadingDialog.dismiss();
                if (status == 0) {
                    Log.d(TAG, "Login success");

                } else {
                    HandleResponseCode.onHandle(DemoActivity.this, status, false);
                }
            }
        });

        mGroupDetailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (JMessageClient.getMyInfo() != null) {
                    //创建会话,模拟从会话跳转到群聊详情
                    Conversation conv = JMessageClient.getGroupConversation(groupId);
                    if (null == conv) {
                        conv = Conversation.createGroupConversation(groupId);
                    }
                    //由于群组是在服务端创建,所以在进入群聊详情之前要先拿一次GroupInfo,这样sdk会在后台拿GroupInfo,
                    //因为conv是本地创建的会话.在实际运用中不必如此(实际运用中,都是先创建会话,再加人)
                    GroupInfo groupInfo = (GroupInfo) conv.getTargetInfo();
                    Intent intent = new Intent();
                    //传递groupId, username
                    intent.putExtra(GROUP_ID, groupId);
                    intent.putExtra(MY_USERNAME, myName);
                    intent.setClass(DemoActivity.this, GroupDetailActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    public void onEventMainThread(UserLogoutEvent event) {

    }

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
}
