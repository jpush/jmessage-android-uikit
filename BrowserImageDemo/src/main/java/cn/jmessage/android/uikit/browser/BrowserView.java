package cn.jmessage.android.uikit.browser;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.jmessage.android.uikit.BrowserViewPagerActivity;
import cn.jmessage.android.uikit.R;

/**
 * Created by huminios on 15/12/23.
 */
public class BrowserView extends RelativeLayout {

    private ImgBrowserViewPager mViewPager;
    private ImageButton mReturnBtn;
    private TextView mNumberTv;
    private Button mSendBtn;
    private CheckBox mOriginPictureCb;
    private TextView mTotalSizeTv;
    private CheckBox mPictureSelectedCb;
    private Button mLoadBtn;

    public BrowserView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void initModule() {
        mViewPager = (ImgBrowserViewPager) findViewById(R.id.img_browser_viewpager);
        mReturnBtn = (ImageButton) findViewById(R.id.return_btn);
        mNumberTv = (TextView) findViewById(R.id.number_tv);
        mSendBtn = (Button) findViewById(R.id.pick_picture_send_btn);
        mOriginPictureCb = (CheckBox) findViewById(R.id.origin_picture_cb);
        mTotalSizeTv = (TextView) findViewById(R.id.total_size_tv);
        mPictureSelectedCb = (CheckBox) findViewById(R.id.picture_selected_cb);
        mLoadBtn = (Button) findViewById(R.id.load_image_btn);
    }

    public ImgBrowserViewPager getViewPager() {
        return mViewPager;
    }

    public void setAdapter(PagerAdapter pagerAdapter) {
        mViewPager.setAdapter(pagerAdapter);
    }

    public void setOnPageChangeListener(ViewPager.OnPageChangeListener onPageChangeListener) {
        mViewPager.setOnPageChangeListener(onPageChangeListener);
    }


    public void setListeners(OnClickListener listener) {
        mReturnBtn.setOnClickListener(listener);
        mSendBtn.setOnClickListener(listener);
        mLoadBtn.setOnClickListener(listener);
    }

    public void setCurrentItem(int position) {
        mViewPager.setCurrentItem(position);
    }

    public void setPickedText(String numberText) {
        mNumberTv.setText(numberText);
    }

    public int getCurrentItem() {
        return mViewPager.getCurrentItem();
    }

    public void setPictureSelected(boolean flag) {
        mPictureSelectedCb.setChecked(flag);
    }

    public void setSendText(String sendText) {
        mSendBtn.setText(sendText);
    }

    public void setTotalText(String totalText) {
        mTotalSizeTv.setText(totalText);
    }

    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
        mPictureSelectedCb.setOnCheckedChangeListener(listener);
        mOriginPictureCb.setOnCheckedChangeListener(listener);
    }
}
