# jmessage-android-ui-components
IM SDK UI 组件

####一个聊天工具中常用的录音组件

#####用法

- 复制recordvoice文件夹下的文件到你的项目（尤其是RecordVoiceButton，是必需的）
- 在XML中加入添加引用
- 

This demo is for recording voice. Copy the files in the recordvoice folder(especially the file

RecordVoiceButton, is needed), and add usage in your

XML file, like this:

        <cn.jmessage.android.uicomponents.recordvoice.RecordVoiceButton
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

Don't forget change the package name. That's all.

