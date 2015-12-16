package cn.jmessage.android.uicomponents;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class PickPictureActivity extends BaseActivity {

    private GridView mGridView;
    //此相册下所有图片的路径集合
    private List<String> mList;
    //选中图片的路径集合
    private List<String> mPickedList;
    private Button mSendPictureBtn;
    private ImageButton mReturnBtn;
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

        TextView title;
        setContentView(R.layout.activity_pick_picture_detail);
        mSendPictureBtn = (Button) findViewById(R.id.pick_picture_send_btn);
        mReturnBtn = (ImageButton) findViewById(R.id.pick_picture_detail_return_btn);
        mGridView = (GridView) findViewById(R.id.child_grid);
        title = (TextView) findViewById(R.id.title);

        Intent intent = this.getIntent();
        mList = intent.getStringArrayListExtra("data");
        mAdapter = new PickPictureAdapter(this, mList, mGridView, mDensity);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(onItemListener);
        mSendPictureBtn.setOnClickListener(listener);
        mReturnBtn.setOnClickListener(listener);
        title.setText(intent.getStringExtra("albumName"));
    }

    private OnItemClickListener onItemListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> viewAdapter, View view, int position, long id) {
            Intent intent = new Intent();
            intent.putStringArrayListExtra("pathList", (ArrayList<String>) mList);
            intent.putExtra("position", position);
            intent.putExtra("pathArray", mAdapter.getSelectedArray());
            intent.setClass(PickPictureActivity.this, BrowserViewPagerActivity.class);
            startActivityForResult(intent, REQUEST_CODE_BROWSER_PICTURE);
        }
    };

    private OnClickListener listener = new OnClickListener() {
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

    };


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
                    mSendPictureBtn.setText(sendText);
                } else {
                    mSendPictureBtn.setText(PickPictureActivity.this.getString(R.string.send));
                }
                mAdapter.refresh(selectedArray);
            }

        }
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
