package cn.jmessage.android.uikit;

public class VoiceMessage {

    private int mDuration;
    private String mPath;

    public VoiceMessage(int duration, String path) {
        this.mDuration = duration;
        this.mPath = path;
    }

    public int getDuration() {
        return mDuration;
    }

    public String getPath() {
        return mPath;
    }

}
