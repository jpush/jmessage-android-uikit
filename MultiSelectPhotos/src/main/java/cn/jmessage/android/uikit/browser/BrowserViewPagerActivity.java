package cn.jmessage.android.uikit.browser;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CompoundButton;
import android.widget.Toast;

import cn.jmessage.android.uikit.R;
import cn.jmessage.android.uikit.browser.photoview.PhotoView;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

//用于浏览图片
public class BrowserViewPagerActivity extends BaseActivity implements OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static String TAG = BrowserViewPagerActivity.class.getSimpleName();
    private BrowserView mBrowserView;
    private PhotoView photoView;
    private ProgressDialog mProgressDialog;
    //存放所有图片的路径
    private List<String> mPathList = new ArrayList<String>();
    private int mPosition;
    private Context mContext;
    private final MyHandler myHandler = new MyHandler(this);
    private final static int DOWNLOAD_ORIGIN_IMAGE_SUCCEED = 1;
    private final static int DOWNLOAD_PROGRESS = 2;
    private final static int DOWNLOAD_COMPLETED = 3;
    private final static int SEND_PICTURE = 5;
    private final static int DOWNLOAD_ORIGIN_PROGRESS = 6;
    private final static int DOWNLOAD_ORIGIN_COMPLETED = 7;
    private static final int RESULT_CODE_SELECT_PICTURE = 10;

    /**
     * 用来存储图片的选中情况
     */
    private SparseBooleanArray mSelectMap = new SparseBooleanArray();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
        setContentView(R.layout.jmui_activity_image_browser);
        mBrowserView = (BrowserView) findViewById(R.id.image_browser_view);
        mBrowserView.initModule();


        final Intent intent = this.getIntent();
        mPosition = intent.getIntExtra("position", 0);

        PagerAdapter pagerAdapter = new PagerAdapter() {

            @Override
            public int getCount() {
                return mPathList.size();
            }

            /**
             * 点击某张图片预览时，系统自动调用此方法加载这张图片左右视图（如果有的话）
             */
            @Override
            public View instantiateItem(ViewGroup container, int position) {
                photoView = new PhotoView(container.getContext());
                photoView.setTag(position);
                String path = mPathList.get(position);
                if (path != null) {
                    File file = new File(path);
                    if (file.exists()) {
                        Bitmap bitmap = BitmapLoader.getBitmapFromFile(path, mWidth, mHeight);
                        if (bitmap != null) {
                            photoView.setImageBitmap(bitmap);
                        } else {
                            photoView.setImageResource(R.mipmap.jmui_picture_not_found);
                        }
                    } else {
                        Bitmap bitmap = NativeImageLoader.getInstance().getBitmapFromMemCache(path);
                        if (bitmap != null) {
                            photoView.setImageBitmap(bitmap);
                        } else {
                            photoView.setImageResource(R.mipmap.jmui_picture_not_found);
                        }
                    }
                } else {
                    photoView.setImageResource(R.mipmap.jmui_picture_not_found);
                }
                container.addView(photoView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                return photoView;
            }

            @Override
            public int getItemPosition(Object object) {
                View view = (View) object;
                int currentPage = mBrowserView.getViewPager().getCurrentItem();
                if (currentPage == (Integer) view.getTag()) {
                    return POSITION_NONE;
                } else {
                    return POSITION_UNCHANGED;
                }
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

        };
        mBrowserView.setAdapter(pagerAdapter);
        mBrowserView.setOnPageChangeListener(onPageChangeListener);
        mBrowserView.setListeners(this);
        mBrowserView.setOnCheckedChangeListener(this);

        mPathList = intent.getStringArrayListExtra("pathList");
        int[] pathArray = intent.getIntArrayExtra("pathArray");
        //初始化选中了多少张图片
        for (int i = 0; i < pathArray.length; i++) {
            if (pathArray[i] == 1) {
                mSelectMap.put(i, true);
            }
        }
        showSelectedNum();
        mBrowserView.setCurrentItem(mPosition);
        String numberText = mPosition + 1 + "/" + mPathList.size();
        mBrowserView.setPickedText(numberText);
        int currentItem = mBrowserView.getCurrentItem();
//        checkPictureSelected(currentItem);
//        checkOriginPictureSelected();
        //第一张特殊处理
        mBrowserView.setPictureSelected(mSelectMap.get(currentItem));
        showTotalSize();
    }

//    /**
//     * 在图片预览中发送图片，点击选择CheckBox时，触发事件
//     *
//     * @param currentItem 当前图片索引
//     */
//    private void checkPictureSelected(final int currentItem) {
//        mPictureSelectedCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
//
//            }
//        });
//
//    }

//    /**
//     * 点击发送原图CheckBox，触发事件
//     *
//     */
//    private void checkOriginPictureSelected() {
//        mOriginPictureCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
//
//            }
//        });
//    }

    //显示选中的图片总的大小
    private void showTotalSize() {
        if (mSelectMap.size() > 0) {
            List<String> pathList = new ArrayList<String>();
            for (int i=0; i < mSelectMap.size(); i++) {
                pathList.add(mPathList.get(mSelectMap.keyAt(i)));
            }
            String totalSize = BitmapLoader.getPictureSize(pathList);
            String totalText = mContext.getString(R.string.origin_picture)
                    + String.format(mContext.getString(R.string.combine_title), totalSize);
            mBrowserView.setTotalText(totalText);
        } else {
            mBrowserView.setTotalText(mContext.getString(R.string.origin_picture));
        }
    }

    //显示选中了多少张图片
    private void showSelectedNum() {
        if (mSelectMap.size() > 0) {
            String sendText = mContext.getString(R.string.send) + "(" + mSelectMap.size() + "/" + "9)";
            mBrowserView.setSendText(sendText);
        } else {
            mBrowserView.setSendText(mContext.getString(R.string.send));
        }
    }

    private ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        //在滑动的时候更新CheckBox的状态
        @Override
        public void onPageScrolled(final int i, float v, int i2) {
//            checkPictureSelected(i);
//            checkOriginPictureSelected();
            mBrowserView.setPictureSelected(mSelectMap.get(i));
        }

        @Override
        public void onPageSelected(final int i) {
            Log.d(TAG, "onPageSelected current position: " + i);
            String numText = i + 1 + "/" + mPathList.size();
            mBrowserView.setPickedText(numText);
        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    };




    @Override
    protected void onDestroy() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        super.onDestroy();
    }

    /**
     * 返回时将所选的图片路径(此处通过一个int数组记录所选的图片)返回到上一个Activity
     */
    @Override
    public void onBackPressed() {
        int pathArray[] = new int[mPathList.size()];
        for (int i = 0; i < pathArray.length; i++) {
            pathArray[i] = 0;
        }
        for (int i = 0; i < mSelectMap.size(); i++) {
            pathArray[mSelectMap.keyAt(i)] = 1;
        }
        Intent intent = new Intent();
        intent.putExtra("pathArray", pathArray);
        setResult(RESULT_CODE_SELECT_PICTURE, intent);
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.return_btn:
                int pathArray[] = new int[mPathList.size()];
                for (int i = 0; i < pathArray.length; i++) {
                    pathArray[i] = 0;
                }
                for (int j = 0; j < mSelectMap.size(); j++) {
                    pathArray[mSelectMap.keyAt(j)] = 1;
                }
                Intent intent = new Intent();
                intent.putExtra("pathArray", pathArray);
                setResult(RESULT_CODE_SELECT_PICTURE, intent);
                finish();
                break;
            case R.id.pick_picture_send_btn:
                //TODO 发送图片
                mProgressDialog = new ProgressDialog(mContext);
                mProgressDialog.setMessage(mContext.getString(R.string.sending_hint));
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();
                mPosition = mBrowserView.getCurrentItem();
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int currentItem = mBrowserView.getCurrentItem();
        switch (buttonView.getId()) {
            case R.id.picture_selected_cb:
                if (mSelectMap.size() + 1 <= 9) {
                    if (isChecked) {
                        mSelectMap.put(currentItem, true);
                    } else {
                        mSelectMap.delete(currentItem);
                    }
                } else if (isChecked) {
                    Toast.makeText(mContext, mContext.getString(R.string.picture_num_limit_toast), Toast.LENGTH_SHORT).show();
                    mBrowserView.setPictureSelected(mSelectMap.get(currentItem));
                } else {
                    mSelectMap.delete(currentItem);
                }

                showSelectedNum();
                showTotalSize();
                break;
            case R.id.origin_picture_cb:
                if (isChecked) {
                    if (mSelectMap.size() < 1) {
                        mBrowserView.setPictureSelected(true);
                    }
                }
                break;
        }
    }


    private static class MyHandler extends Handler{
        private final WeakReference<BrowserViewPagerActivity> mActivity;

        public MyHandler(BrowserViewPagerActivity activity){
            mActivity = new WeakReference<BrowserViewPagerActivity>(activity);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            BrowserViewPagerActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case DOWNLOAD_PROGRESS:
                        activity.mProgressDialog.setProgress(msg.getData().getInt("progress"));
                        break;
                    case DOWNLOAD_COMPLETED:
                        activity.mProgressDialog.dismiss();
                        break;
                    case SEND_PICTURE:
                        Intent intent = new Intent();
                        activity.finish();
                        break;
                    //显示下载原图进度
                    case DOWNLOAD_ORIGIN_PROGRESS:
                        String progress = msg.getData().getInt("progress") + "%";
                        break;
                }
            }
        }
    }

}
