package cn.jmessage.android.uikit;

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
import cn.jmessage.android.uikit.browser.PickPictureView;
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
    private static final int RESULT_CODE_SELECT_PICTURE = 10;
    private static final int REQUEST_CODE_BROWSER_PICTURE = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pick_picture_detail);
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
                        if (activity.mDialog != null) {
                            activity.mDialog.dismiss();
                        }
                        break;
                }
            }
        }
    }
}
