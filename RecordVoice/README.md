# jmessage-android-ui-components
IM SDK UI 组件

一个聊天工具中常用的录音组件

###用法

- 复制recordvoice文件夹下的文件到你的项目（尤其是RecordVoiceButton，是必需的）
- 在XML中加入添加引用
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

- 初始化RecordVoiceButton后,要使RecordVoiceButton持有adapter对象的引用,即调用方法mRecordBtn.initAdapter(mAdapter);
这样可以在录音完成后,将录音消息添加到消息列表,即在RecordVoiceButton的finishRecord()方法中将语音消息添加到Adapter,如:
```
    VoiceMessage voiceMessage = new VoiceMessage(duration, myRecAudioFile.getAbsolutePath());
    mAdapter.addToMsgList(voiceMessage);

```
