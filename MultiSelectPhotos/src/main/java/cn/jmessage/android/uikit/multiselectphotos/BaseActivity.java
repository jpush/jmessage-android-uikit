package cn.jmessage.android.uikit.multiselectphotos;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;

public class BaseActivity extends Activity {

    protected float mDensity;
    protected int mWidth;
    protected int mHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mDensity = dm.density;
        mWidth = dm.widthPixels;
        mHeight = dm.heightPixels;
    }
}
