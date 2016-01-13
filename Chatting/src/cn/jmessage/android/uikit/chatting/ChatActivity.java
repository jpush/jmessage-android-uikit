package cn.jmessage.android.uikit.chatting;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.io.File;
import java.lang.ref.WeakReference;

import cn.jmessage.android.uikit.R;
import cn.jmessage.android.uikit.chatting.utils.BitmapLoader;
import cn.jmessage.android.uikit.chatting.utils.DialogCreator;
import cn.jmessage.android.uikit.chatting.utils.FileHelper;
import cn.jmessage.android.uikit.chatting.utils.IdHelper;
import cn.jmessage.android.uikit.chatting.utils.SharePreferenceManager;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetGroupInfoCallback;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.event.UserLogoutEvent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;


/**
 * 一个简单的聊天界面,可以发送文字,语音及拍照发送图片功能
 */
public class ChatActivity extends BaseActivity implements View.OnClickListener, View.OnTouchListener,
        ChatView.OnSizeChangedListener, ChatView.OnKeyBoardChangeListener {

    private static final String TAG = "ChatActivity";
    private static final String TARGET_ID = "targetId";
    private static final String GROUP_ID = "groupId";
    public static final String MsgIDs = "msgIDs";
    public static final String IS_GROUP = "isGroup";
    public static final int REQUEST_CODE_TAKE_PHOTO = 4;
    public static final int RESULT_CODE_SELECT_PICTURE = 8;
    private ChatView mChatView;
    private MsgListAdapter mChatAdapter;
    Conversation mConv;
    private boolean isInputByKeyBoard = true;
    private boolean mShowSoftInput = false;
    private static final int REFRESH_LAST_PAGE = 1023;
    private String mTargetId;
    private String mPhotoPath = null;
    private final MyHandler myHandler = new MyHandler(this);
    Window mWindow;
    InputMethodManager mImm;
    private MyReceiver mReceiver;
    private Context mContext;
    private long mGroupId;
    private boolean mIsSingle;
    private Dialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(IdHelper.getLayout(this, "jmui_activity_chat"));
        mChatView = (ChatView) findViewById(IdHelper.getViewID(this, "jmui_chat_view"));
        mChatView.initModule();
        mContext = this;
        //注册接收消息(成为订阅者), 注册后可以直接重写onEvent方法接收消息
        JMessageClient.registerEventReceiver(this);
        this.mWindow = getWindow();
        this.mImm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        mChatView.setListeners(this);
        mChatView.setOnTouchListener(this);
        mChatView.setOnSizeChangedListener(this);
        mChatView.setOnKbdStateListener(this);
        initReceiver();
        Intent intent = getIntent();
        mTargetId = intent.getStringExtra(TARGET_ID);
        if (!TextUtils.isEmpty(mTargetId)) {
            mIsSingle = true;
            JMessageClient.getUserInfo(mTargetId, new GetUserInfoCallback() {
                @Override
                public void gotResult(int status, String desc, UserInfo userInfo) {
                    if (status == 0) {
                        mChatView.setChatTitle(userInfo.getUserName());
                    }
                }
            });

            mConv = JMessageClient.getSingleConversation(mTargetId);
            if (mConv == null) {
                mConv = Conversation.createSingleConversation(mTargetId);
            }
            mChatAdapter = new MsgListAdapter(mContext, mTargetId);
        } else {
            mIsSingle = false;
            mGroupId = intent.getLongExtra(GROUP_ID, 0);
            Log.d(TAG, "GroupId : " + mGroupId);
            JMessageClient.getGroupInfo(mGroupId, new GetGroupInfoCallback() {
                @Override
                public void gotResult(int status, String desc, GroupInfo groupInfo) {
                    if (status == 0) {
                        mChatView.setChatTitle(groupInfo.getGroupName());
                    }
                }
            });
            mConv = JMessageClient.getGroupConversation(mGroupId);
            if (mConv == null) {
                mConv = Conversation.createGroupConversation(mGroupId);
            }
            mChatAdapter = new MsgListAdapter(mContext, mGroupId);
        }
        mChatView.setChatListAdapter(mChatAdapter);


        mChatAdapter.initMediaPlayer();


        //监听下拉刷新
        mChatView.getListView().setOnDropDownListener(new DropDownListView.OnDropDownListener() {
            @Override
            public void onDropDown() {
                myHandler.sendEmptyMessageDelayed(REFRESH_LAST_PAGE, 1000);
            }
        });
        // 滑动到底部
        mChatView.setToBottom();
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
                    mChatAdapter.setAudioPlayByEarPhone(data.getIntExtra("state", 0));
                }
            }
        }

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == IdHelper.getViewID(mContext, "jmui_return_btn")) {
            finish();
            // 切换输入
        } else if (v.getId() == IdHelper.getViewID(mContext, "jmui_switch_voice_ib")) {
            mChatView.dismissMoreMenu();
            isInputByKeyBoard = !isInputByKeyBoard;
            //当前为语音输入，点击后切换为文字输入，弹出软键盘
            if (isInputByKeyBoard) {
                mChatView.isKeyBoard();
                showSoftInputAndDismissMenu();
            } else {
                //否则切换到语音输入
                mChatView.notKeyBoard(mConv, mChatAdapter);
                if (mShowSoftInput) {
                    if (mImm != null) {
                        mImm.hideSoftInputFromWindow(mChatView.getInputView().getWindowToken(), 0); //强制隐藏键盘
                        mShowSoftInput = false;
                    }
                } else if (mChatView.getMoreMenu().getVisibility() == View.VISIBLE) {
                    mChatView.dismissMoreMenu();
                }
                Log.i("ChatController", "setConversation success");
            }
            // 发送文本消息
        } else if (v.getId() == IdHelper.getViewID(mContext, "jmui_send_msg_btn")) {
            String msgContent = mChatView.getChatInput();
            mChatView.clearInput();
            mChatView.setToBottom();
            if (msgContent.equals("")) {
                return;
            }
            TextContent content = new TextContent(msgContent);
            final Message msg = mConv.createSendMessage(content);
            mChatAdapter.addMsgToList(msg);
            JMessageClient.sendMessage(msg);
            // 点击添加按钮，弹出更多选项菜单
        } else if (v.getId() == IdHelper.getViewID(mContext, "jmui_add_file_btn")) {
            //如果在语音输入时点击了添加按钮，则显示菜单并切换到输入框
            if (!isInputByKeyBoard) {
                mChatView.isKeyBoard();
                isInputByKeyBoard = true;
                mChatView.showMoreMenu();
            } else {
                //如果弹出软键盘 则隐藏软键盘
                if (mChatView.getMoreMenu().getVisibility() != View.VISIBLE) {
                    dismissSoftInputAndShowMenu();
                    mChatView.focusToInput(false);
                    //如果弹出了更多选项菜单，则隐藏菜单并显示软键盘
                } else {
                    showSoftInputAndDismissMenu();
                }
            }
        } else if (v.getId() == IdHelper.getViewID(mContext, "jmui_pick_from_camera_btn")) {
            takePhoto();
            if (mChatView.getMoreMenu().getVisibility() == View.VISIBLE) {
                mChatView.dismissMoreMenu();
            }
        } else {
            if (mChatView.getMoreMenu().getVisibility() == View.VISIBLE) {
                mChatView.dismissMoreMenu();
            }
            Intent intent = new Intent();
            intent.putExtra(TARGET_ID, mTargetId);
            //TODO 发送本地图片
//                mContext.startPickPictureTotalActivity(intent);
        }
    }

    private void takePhoto() {
        if (FileHelper.isSdCardExist()) {
            mPhotoPath = FileHelper.createAvatarPath(null);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mPhotoPath)));
            try {
                startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO);
            } catch (ActivityNotFoundException anf) {
                Toast.makeText(mContext, mContext.getString(IdHelper.getString(mContext,
                                "jmui_camera_not_prepared")), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(mContext, mContext.getString(IdHelper.getString(mContext,
                            "jmui_sdcard_not_exist_toast")), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 处理发送图片，刷新界面
     *
     * @param data intent
     */
    private void handleImgRefresh(Intent data) {
        String targetId = data.getStringExtra(TARGET_ID);
        long groupId = data.getLongExtra(GROUP_ID, 0);
        Log.i(TAG, "Refresh Image groupId: " + groupId);
        //判断是否在当前会话中发图片
        if (targetId != null) {
            if (targetId.equals(mTargetId)) {
                mChatAdapter.setSendImg(targetId, data.getIntArrayExtra(MsgIDs));
                mChatView.setToBottom();
            }
        } else if (groupId != 0) {
            if (groupId == mGroupId) {
                mChatAdapter.setSendImg(groupId, data.getIntArrayExtra(MsgIDs));
                mChatView.setToBottom();
            }
        }


    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed!");
        if (RecordVoiceButton.mIsPressed) {
            mChatView.dismissRecordDialog();
            mChatView.releaseRecorder();
            RecordVoiceButton.mIsPressed = false;
        }
        if (mChatView.getMoreMenu().getVisibility() == View.VISIBLE) {
            mChatView.dismissMoreMenu();
        } else {
            if (mConv != null) {
                mConv.resetUnreadCount();
            }
        }
        super.onBackPressed();
    }

    /**
     * 释放资源
     */
    @Override
    protected void onDestroy() {
        //取消订阅
        JMessageClient.unRegisterEventReceiver(this);
        unregisterReceiver(mReceiver);
        mChatAdapter.releaseMediaPlayer();
        mChatView.releaseRecorder();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        RecordVoiceButton.mIsPressed = false;
        JMessageClient.exitConversaion();
        Log.i(TAG, "[Life cycle] - onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        mChatAdapter.stopMediaPlayer();
        if (mChatView.getMoreMenu().getVisibility() == View.VISIBLE) {
            mChatView.dismissMoreMenu();
        }
        if (mConv != null)
            mConv.resetUnreadCount();
        Log.i(TAG, "[Life cycle] - onStop");
        super.onStop();
    }

    @Override
    protected void onResume() {
        if (!RecordVoiceButton.mIsPressed)
            mChatView.dismissRecordDialog();
        String targetID = getIntent().getStringExtra(TARGET_ID);
        boolean isGroup = getIntent().getBooleanExtra(IS_GROUP, false);
        if (isGroup) {
            try {
                long groupID = getIntent().getLongExtra(GROUP_ID, 0);
                if (groupID == 0) {
                    JMessageClient.enterGroupConversation(Long.parseLong(targetID));
                } else JMessageClient.enterGroupConversation(groupID);
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
            }
        } else if (null != targetID) {
            JMessageClient.enterSingleConversaion(targetID);
        }
        if (mChatAdapter != null) {
            mChatAdapter.initMediaPlayer();
        }
        Log.i(TAG, "[Life cycle] - onResume");
        super.onResume();
    }

    /**
     * 用于处理拍照发送图片返回结果以及从其他界面回来后刷新聊天标题
     * 或者聊天消息
     *
     * @param requestCode 请求码
     * @param resultCode  返回码
     * @param data        intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_CANCELED) {
            return;
        }
        if (requestCode == REQUEST_CODE_TAKE_PHOTO) {
            try {
                Bitmap bitmap = BitmapLoader.getBitmapFromFile(mPhotoPath, 720, 1280);
                ImageContent.createImageContentAsync(bitmap, new ImageContent.CreateImageContentCallback() {
                    @Override
                    public void gotResult(int status, String desc, ImageContent imageContent) {
                        if (status == 0) {
                            Message msg = mConv.createSendMessage(imageContent);
                            Intent intent = new Intent();
                            intent.putExtra(MsgIDs, new int[]{msg.getId()});
                            if (mConv.getType() == ConversationType.group) {
                                intent.putExtra(GROUP_ID, mGroupId);
                            } else {
                                intent.putExtra(TARGET_ID, ((UserInfo) mConv.getTargetInfo()).getUserName());
                            }
                            handleImgRefresh(intent);
                        }
                    }
                });
            } catch (NullPointerException e) {
                Log.i(TAG, "onActivityResult unexpected result");
            }
        } else if (resultCode == RESULT_CODE_SELECT_PICTURE) {
            handleImgRefresh(data);
        }
    }

    private void showSoftInputAndDismissMenu() {
        mWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN); // 隐藏软键盘
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mChatView.invisibleMoreMenu();
        mChatView.getInputView().requestFocus();
        if (mImm != null) {
            mImm.showSoftInput(mChatView.getInputView(),
                    InputMethodManager.SHOW_FORCED);//强制显示键盘
        }
        mShowSoftInput = true;
        mChatView.setMoreMenuHeight();
    }

    public void dismissSoftInputAndShowMenu() {
        //隐藏软键盘
        mWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN); // 隐藏软键盘
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mChatView.showMoreMenu();
        if (mImm != null) {
            mImm.hideSoftInputFromWindow(mChatView.getInputView().getWindowToken(), 0); //强制隐藏键盘
        }
        mChatView.setMoreMenuHeight();
        mShowSoftInput = false;
    }

    private static class MyHandler extends Handler {
        private final WeakReference<ChatActivity> mActivity;

        public MyHandler(ChatActivity controller) {
            mActivity = new WeakReference<ChatActivity>(controller);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            ChatActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case REFRESH_LAST_PAGE:
                        activity.mChatAdapter.dropDownToRefresh();
                        activity.mChatView.getListView().onDropDownComplete();
                        if (activity.mChatAdapter.isHasLastPage()) {
                            activity.mChatView.getListView()
                                    .setSelection(activity.mChatAdapter.getOffset());
                            activity.mChatAdapter.refreshStartPosition();
                        } else {
                            activity.mChatView.getListView().setSelection(0);
                        }
                        activity.mChatView.getListView()
                                .setOffset(activity.mChatAdapter.getOffset());
                        break;
                }
            }
        }
    }

    @Override
    public void onKeyBoardStateChange(int state) {
        switch (state) {
            case ChatView.KEYBOARD_STATE_INIT:
                if (mImm != null) {
                    mImm.isActive();
                }
                if (mChatView.getMoreMenu().getVisibility() == View.INVISIBLE
                        || (!mShowSoftInput && mChatView.getMoreMenu().getVisibility() == View.GONE)) {

                    mWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                            | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mChatView.getMoreMenu().setVisibility(View.GONE);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 接收消息类事件
     *
     * @param event 消息事件
     */
    public void onEvent(MessageEvent event) {
        final Message msg = event.getMessage();
        Log.d(TAG, "收到消息 msg.toString() " + msg.toString());
        //若为群聊相关事件，如添加、删除群成员
        Log.i(TAG, event.getMessage().toString());
        //刷新消息
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //收到消息的类型为单聊
                if (msg.getTargetType() == ConversationType.single) {
                    String targetId = ((UserInfo) msg.getTargetInfo()).getUserName();
                    //判断消息是否在当前会话中
                    if (targetId.equals(mTargetId)) {
                        Message lastMsg = mChatAdapter.getLastMsg();
                        //收到的消息和Adapter中最后一条消息比较，如果最后一条为空或者不相同，则加入到MsgList
                        if (lastMsg == null || msg.getId() != lastMsg.getId()) {
                            mChatAdapter.addMsgToList(msg);
                        } else {
                            mChatAdapter.notifyDataSetChanged();
                        }
                    }
                } else {
                    long groupId = ((GroupInfo) msg.getTargetInfo()).getGroupID();
                    if (!mIsSingle && groupId == mGroupId) {
                        Message lastMsg = mChatAdapter.getLastMsg();
                        if (lastMsg == null || msg.getId() != lastMsg.getId()) {
                            mChatAdapter.addMsgToList(msg);
                        } else {
                            mChatAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });
    }

    /**
     * 收到被踢下线事件
     * @param event 登出事件
     */
    public void onEventMainThread(UserLogoutEvent event) {
        String title = mContext.getString(R.string.jmui_user_logout_dialog_title);
        String msg = mContext.getString(R.string.jmui_user_logout_dialog_message);
        mDialog = DialogCreator.createBaseCustomDialog(mContext, title, msg, onClickListener);
        mDialog.getWindow().setLayout((int) (0.8 * mWidth), WindowManager.LayoutParams.WRAP_CONTENT);
        mDialog.show();
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mDialog.dismiss();
            finish();
        }
    };

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (oldh - h > 300) {
            mShowSoftInput = true;
            if (SharePreferenceManager.getCachedWritableFlag()) {
                SharePreferenceManager.setCachedKeyboardHeight(oldh - h);
                SharePreferenceManager.setCachedWritableFlag(false);
            }

            mChatView.setMoreMenuHeight();
        } else {
            mShowSoftInput = false;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (view.getId() == IdHelper.getViewID(mContext, "jmui_chat_input_et")) {
                    if (mChatView.getMoreMenu().getVisibility() == View.VISIBLE && !mShowSoftInput) {
                        showSoftInputAndDismissMenu();
                        return false;
                    } else {
                        return false;
                    }
                }
                if (mChatView.getMoreMenu().getVisibility() == View.VISIBLE) {
                    mChatView.dismissMoreMenu();
                } else if (mShowSoftInput) {
                    View v = getCurrentFocus();
                    if (mImm != null && v != null) {
                        mImm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        mWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                                | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                        mShowSoftInput = false;
                    }
                }
                break;
        }
        return false;
    }
}
