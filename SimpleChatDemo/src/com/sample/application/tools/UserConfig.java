package com.sample.application.tools;

public class UserConfig {

    private boolean mIsUser1;
    private String mUser1;
    private String mUser2;
    private String mPassword = "1111";

    public UserConfig(boolean isUser1) {
        mIsUser1 = isUser1;
        if (mIsUser1) {
            mUser1 = "user001";
            mUser2 = "user002";
        } else {
            mUser1 = "user002";
            mUser2 = "user001";
        }
    }

    public String getTargetId() {
        return mUser2;
    }

    public String getMyUsername() {
        return mUser1;
    }

    public String getPassword() {
        return mPassword;
    }
}
