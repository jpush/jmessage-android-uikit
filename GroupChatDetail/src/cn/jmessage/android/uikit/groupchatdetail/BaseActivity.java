package cn.jmessage.android.uikit.groupchatdetail;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.event.UserLogoutEvent;

/**
 * Created by Ken on 2015/3/13.
 */

/**
 * 主要用于一些初始化的动作
 */
public class BaseActivity extends Activity {
    private static final String TAG = "BaseActivity";

    protected BaseHandler mHandler;
    protected float mDensity;
    protected int mDensityDpi;
    protected int mAvatarSize;
    protected int mWidth;
    protected int mHeight;
    private Dialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //初始化JMessage-sdk
        JMessageClient.init(this);
        //订阅接收消息 这里主要是添加或删除群成员的event
        JMessageClient.registerEventReceiver(this);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mDensity = dm.density;
        mDensityDpi = dm.densityDpi;
        mWidth = dm.widthPixels;
        mHeight = dm.heightPixels;
        mAvatarSize = (int) (50 * mDensity);
    }

    @Override
    protected void onDestroy() {
        JMessageClient.unRegisterEventReceiver(this);
        super.onDestroy();
    }

    public class BaseHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            handleMsg(msg);
        }
    }

    public void handleMsg(Message message) {
    }

    public void onEventMainThread(UserLogoutEvent event) {
        Context context = BaseActivity.this;
        String title = context.getString(IdHelper.getString(context, "jmui_user_logout_dialog_title"));
        String msg = context.getString(IdHelper.getString(context, "jmui_user_logout_dialog_message"));
        mDialog = DialogCreator.createBaseCustomDialog(context, title, msg, onClickListener);
        mDialog.getWindow().setLayout((int) (0.8 * mWidth), WindowManager.LayoutParams.WRAP_CONTENT);
        mDialog.show();
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mDialog.dismiss();
        }
    };

}
