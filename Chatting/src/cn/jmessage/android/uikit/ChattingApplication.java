package cn.jmessage.android.uikit;

import android.app.Application;
import android.util.Log;

import cn.jmessage.android.uikit.tools.SharePreferenceManager;

import cn.jpush.im.android.api.JMessageClient;

public class ChattingApplication extends Application {

    public static final int REQUEST_CODE_TAKE_PHOTO = 4;
    public static final int RESULT_CODE_SELECT_PICTURE = 8;
    public static final int PAGE_MESSAGE_COUNT = 18;

    private static final String JCHAT_CONFIGS = "JChat_configs";

    public static final String TARGET_ID = "targetId";
    public static final String NAME = "name";
    public static final String NICKNAME = "nickname";
    public static final String GROUP_ID = "groupId";
    public static final String IS_GROUP = "isGroup";
    public static final String GROUP_NAME = "groupName";
    public static final String STATUS = "status";
    public static final String POSITION = "position";
    public static final String MsgIDs = "msgIDs";
    public static final String DRAFT = "draft";
    public static final String DELETE_MODE = "deleteMode";
    public static final String MEMBERS_COUNT = "membersCount";
    public static final String PICTURE_DIR = "sdcard/JChatDemo/pictures/";


    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("JpushDemoApplication", "init");
        JMessageClient.init(getApplicationContext());
        SharePreferenceManager.init(getApplicationContext(), JCHAT_CONFIGS);
        JMessageClient.setNotificationMode(JMessageClient.NOTI_MODE_DEFAULT);
    }

}
