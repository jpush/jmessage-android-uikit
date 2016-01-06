package cn.jmessage.android.uikit.multiselectphotos;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.jmessage.android.uikit.R;

public class PickPictureView extends LinearLayout {

    private Button mSendPictureBtn;
    private ImageButton mReturnBtn;
    private GridView mGridView;

    public PickPictureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void initModule(String titleTxt) {
        TextView title;
        mSendPictureBtn = (Button) findViewById(R.id.pick_picture_send_btn);
        mReturnBtn = (ImageButton) findViewById(R.id.pick_picture_detail_return_btn);
        mGridView = (GridView) findViewById(R.id.child_grid);
        title = (TextView) findViewById(R.id.title);
        title.setText(titleTxt);
    }

    public void setListeners(OnClickListener listener) {
        mSendPictureBtn.setOnClickListener(listener);
        mReturnBtn.setOnClickListener(listener);
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        mGridView.setOnItemClickListener(listener);
    }

    public void setAdapter(PickPictureAdapter adapter) {
        mGridView.setAdapter(adapter);
    }

    public GridView getGridView() {
        return mGridView;
    }

    public void setSendBtnText(String sendText) {
        mSendPictureBtn.setText(sendText);
    }
}
