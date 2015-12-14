package com.sample.recordvoicedemo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private ListView mListView;
    private RecordVoiceButton mRecordBtn;
    private MyReceiver mReceiver;
    private ListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListView = (ListView) findViewById(R.id.list_view);
        mRecordBtn = (RecordVoiceButton) findViewById(R.id.voice_btn);

        mAdapter = new ListAdapter(this);
        mListView.setAdapter(mAdapter);
        mRecordBtn.initAdapter(mAdapter);
        mAdapter.initMediaPlayer();
        initReceiver();
    }

    // 监听耳机插入
    private void initReceiver() {
        mReceiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(mReceiver, filter);
    }

    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent data) {
            if (data != null) {
                //插入了耳机
                if (data.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                    mAdapter.setAudioPlayByEarPhone(data.getIntExtra("state", 0));
                }
            }
        }

    }

    @Override
    protected void onPause() {
        RecordVoiceButton.mIsPressed = false;
        super.onPause();
    }

    @Override
    protected void onStop() {
        mAdapter.stopMediaPlayer();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        mAdapter.releaseMediaPlayer();
        mRecordBtn.releaseRecorder();
        super.onDestroy();
    }
}
