package cn.jmessage.android.uikit.tools;

public class UserConfig {

    private String mUser1;
    private String mUser2;
    private String mMyPassword;
    private String mPassword;
    private long mGroupId;
    private static UserConfig mInstance = new UserConfig();

    public static UserConfig getInstance() {
        return mInstance;
    }

    public void setMyInfo(String username, String password) {
        this.mUser1 = username;
        this.mMyPassword = password;
    }

    public String  getMyUsername() {
        return mUser1;
    }

    public String getMyPassword() {
        return mMyPassword;
    }

    public void setTargetInfo(String username, String password) {
        this.mUser2 = username;
        this.mPassword = password;
    }

    public String getTargetId() {
        return mUser2;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setGroupId(long groupId) {
        this.mGroupId = groupId;
    }

    public long getGroupId() {
        return mGroupId;
    }
}
