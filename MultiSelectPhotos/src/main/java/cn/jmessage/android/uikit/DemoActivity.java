package cn.jmessage.android.uikit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import cn.jmessage.android.uikit.multiselectphotos.AlbumListActivity;
import cn.jmessage.android.uikit.multiselectphotos.PickPictureActivity;

public class DemoActivity extends Activity implements PickPictureActivity.OnSelectedListener {

    public static final int RESULT_CODE_SELECT_PICTURE = 8;
    public static final int REQUEST_CODE_SELECT_ALBUM = 10;
    private static final String PICTURE_PATH = "picturePath";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jmui_activity_main);

        Button startBtn = (Button) findViewById(R.id.start_btn);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(DemoActivity.this, AlbumListActivity.class);
                startActivityForResult(intent, REQUEST_CODE_SELECT_ALBUM);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //得到从选择图片返回的Intent
        if (resultCode == RESULT_CODE_SELECT_PICTURE) {
            //得到图片路径
            ArrayList<String> pathList = data.getStringArrayListExtra(PICTURE_PATH);
            for (String path : pathList) {
                Log.d("DemoActivity", "path : " + path);
            }
        }
    }

    @Override
    public void onSelectedPictures(List<String> list) {
        Log.d("DemoActivity", list.toString());
    }
}
