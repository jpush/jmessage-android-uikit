package cn.jmessage.android.uikit.groupchatdetail;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import cn.jpush.im.android.api.JMessageClient;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mHandler = new BaseHandler();
        //初始化JMessage-sdk
        JMessageClient.init(this);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mDensity = dm.density;
        mDensityDpi = dm.densityDpi;
        mWidth = dm.widthPixels;
        mHeight = dm.heightPixels;
        mAvatarSize = (int) (50 * mDensity);
    }

    public class BaseHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            handleMsg(msg);
        }
    }

    public void handleMsg(Message message) {
    }

}
