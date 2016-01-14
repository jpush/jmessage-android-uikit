package cn.jmessage.android.uikit.chatting;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;

import cn.jmessage.android.uikit.chatting.utils.SharePreferenceManager;
import cn.jpush.im.android.api.JMessageClient;

/**
 * Created by Ken on 2015/3/13.
 */

/**
 * 主要用于一些初始化的动作
 */
public class BaseActivity extends Activity {
    private static final String TAG = "BaseActivity";
    private static final String JCHAT_CONFIGS = "JChat_configs";
    protected float mDensity;
    protected int mDensityDpi;
    protected int mAvatarSize;
    protected int mWidth;
    protected int mHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //初始化JMessage-sdk
        JMessageClient.init(this);
        //设置Notification的模式
        JMessageClient.setNotificationMode(JMessageClient.NOTI_MODE_NO_NOTIFICATION);
        //初始化SharePreference
        SharePreferenceManager.init(this, JCHAT_CONFIGS);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mDensity = dm.density;
        mDensityDpi = dm.densityDpi;
        mWidth = dm.widthPixels;
        mHeight = dm.heightPixels;
        mAvatarSize = (int) (50 * mDensity);
    }

}
