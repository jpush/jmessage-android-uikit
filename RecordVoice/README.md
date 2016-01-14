# jmessage-android-ui-components
IM SDK UI 组件

一个聊天工具中常用的录音组件

###用法

- 复制RecordVoiceButton,IdHelper到你的项目
- 在XML中加入添加引用,比如:
```
    <cn.jmessage.android.uikit.recordvoice.RecordVoiceButton
            android:id="@+id/voice_btn"
            android:layout_width="match_parent"
            android:layout_height="34dp"
            android:background="@drawable/voice_bg"
            android:gravity="center"
            android:padding="5dp"
            android:layout_marginLeft="20dp"
            android:layout_marginRight="20dp"
            android:text="@string/record_voice_hint"
            android:textColor="@android:color/white"
            android:textSize="14sp" />

```
注意将引用路径修改为你当前的路径

- 在Activity中要实现RecordVoiceButton的接口:OnRecordVoiceListener,然后初始化RecordVoiceButton,如下所示:
```
    RecordVoiceButton mRecordBtn = (RecordVoiceButton) findViewById(R.id.voice_btn);
 
```

之后setListener:
```
    mRecordBtn.setRecordListener(this);
    
```

并且设置录音文件的存放位置, 比如:
```
        //设置录音文件存放位置
        File rootDir = this.getFilesDir();
        String fileDir = rootDir.getAbsolutePath() + "/voice";
        mRecordBtn.setFilePath(fileDir);
        
```

最后实现onRecordFinished方法:

```
    /**
     * 录音完成时调用
     * @param duration 时长
     * @param path 录音文件路径
     */
    @Override
    public void onRecordFinished(int duration, String path) {
        VoiceMessage voiceMessage = new VoiceMessage(duration, path);
        mAdapter.addToMsgList(voiceMessage);
    }
    
```
