package cn.jmessage.android.uikit.multiselectphotos;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import cn.jmessage.android.uikit.R;

public class PickPictureActivity extends BaseActivity implements OnClickListener, OnItemClickListener {

    private PickPictureView mPickPictureView;

    //此相册下所有图片的路径集合
    private List<String> mList;
    //选中图片的路径集合
    private List<String> mPickedList;

    private PickPictureAdapter mAdapter;
    private ProgressDialog mDialog;
    private static final int SEND_PICTURE = 200;
    private final MyHandler myHandler = new MyHandler(this);
    private int mIndex = 0;
    public static final int RESULT_CODE_SELECT_PICTURE = 8;
    public static final int RESULT_CODE_BROWSER_PICTURE = 13;
    private static final int REQUEST_CODE_BROWSER_PICTURE = 11;
    private static final String PICTURE_PATH = "picturePath";
    private OnSelectedListener mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.jmui_activity_pick_picture_detail);
        Intent intent = this.getIntent();
        mPickPictureView = (PickPictureView) findViewById(R.id.pick_picture_view);
        mPickPictureView.initModule(intent.getStringExtra("albumName"));
        mPickPictureView.setListeners(this);
        mPickPictureView.setOnItemClickListener(this);

        mList = intent.getStringArrayListExtra("data");
        mAdapter = new PickPictureAdapter(this, mList, mPickPictureView.getGridView(), mDensity);
        mPickPictureView.setAdapter(mAdapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_CODE_SELECT_PICTURE) {
            if (data != null) {
                int[] selectedArray = data.getIntArrayExtra("pathArray");
                int sum = 0;
                for (int i : selectedArray) {
                    if (i > 0) {
                        ++sum;
                    }
                }
                if (sum > 0) {
                    String sendText = PickPictureActivity.this.getString(R.string.send) + "(" + sum + "/" + "9)";
                    mPickPictureView.setSendBtnText(sendText);
                } else {
                    mPickPictureView.setSendBtnText(PickPictureActivity.this.getString(R.string.send));
                }
                mAdapter.refresh(selectedArray);
            }
            //得到从BrowserViewPagerActivity返回的Intent,通过setResult返回上一个Activity
        } else if (resultCode == RESULT_CODE_BROWSER_PICTURE) {
            setResult(RESULT_CODE_SELECT_PICTURE, data);
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //点击发送按钮，发送选中的图片
            case R.id.pick_picture_send_btn:
                //存放选中图片的路径
                mPickedList = new ArrayList<String>();
                //存放选中的图片的position
                List<Integer> positionList;
                positionList = mAdapter.getSelectItems();
                //拿到选中图片的路径
                for (int i = 0; i < positionList.size(); i++) {
                    mPickedList.add(mList.get(positionList.get(i)));
                }
                if (mPickedList.size() < 1) {
                    return;
                } else {
                    mDialog = new ProgressDialog(PickPictureActivity.this);
                    mDialog.setCanceledOnTouchOutside(false);
                    mDialog.setCancelable(false);
                    mDialog.setMessage(PickPictureActivity.this.getString(R.string.sending_hint));
                    mDialog.show();
                    //TODO 发送图片
                    myHandler.sendEmptyMessageDelayed(SEND_PICTURE, 1000);
                }
                break;
            case R.id.pick_picture_detail_return_btn:
                finish();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        intent.putStringArrayListExtra("pathList", (ArrayList<String>) mList);
        intent.putExtra("position", position);
        intent.putExtra("pathArray", mAdapter.getSelectedArray());
        intent.setClass(PickPictureActivity.this, BrowserViewPagerActivity.class);
        startActivityForResult(intent, REQUEST_CODE_BROWSER_PICTURE);
    }

    private static class MyHandler extends Handler {
        private final WeakReference<PickPictureActivity> mActivity;

        public MyHandler(PickPictureActivity activity) {
            mActivity = new WeakReference<PickPictureActivity>(activity);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            PickPictureActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case SEND_PICTURE:
                        //发送图片时将要发送的图片路径List放在intent中,通过setResult返回AlbumListActivity
                        Intent intent = new Intent();
                        intent.putStringArrayListExtra(PICTURE_PATH, (ArrayList<String>) activity.mPickedList);
                        activity.setResult(RESULT_CODE_SELECT_PICTURE, intent);
                        if (activity.mDialog != null) {
                            activity.mDialog.dismiss();
                        }
                        activity.finish();
                        break;
                }
            }
        }
    }

    public void setOnSelectedListener(OnSelectedListener listener) {
        mListener = listener;
    }

    public interface OnSelectedListener {
        /**
         * 选择完图片,点击发送时得到选中的图片路径
         */
        public void onSelectedPictures(List<String> list);
    }
}
