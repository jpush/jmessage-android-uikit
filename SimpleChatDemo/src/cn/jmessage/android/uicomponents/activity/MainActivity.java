package cn.jmessage.android.uicomponents.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.jmessage.android.uicomponents.R;
import cn.jmessage.android.uicomponents.adapter.MsgListAdapter;
import cn.jmessage.android.uicomponents.application.SimpleChatApplication;
import cn.jmessage.android.uicomponents.tools.BitmapLoader;
import cn.jmessage.android.uicomponents.tools.DialogCreator;
import cn.jmessage.android.uicomponents.tools.FileHelper;
import cn.jmessage.android.uicomponents.tools.HandleResponseCode;
import cn.jmessage.android.uicomponents.tools.SharePreferenceManager;
import cn.jmessage.android.uicomponents.tools.UserConfig;
import cn.jmessage.android.uicomponents.view.ChatView;
import cn.jmessage.android.uicomponents.view.DropDownListView;
import cn.jmessage.android.uicomponents.view.RecordVoiceButton;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.content.CustomContent;
import cn.jpush.im.android.api.content.EventNotificationContent;
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;
import de.greenrobot.event.EventBus;


/**
 * 一个简单的聊天界面,可以发送文字,语音及拍照发送图片功能
 */
public class MainActivity extends Activity implements View.OnClickListener, View.OnTouchListener,
        ChatView.OnSizeChangedListener, ChatView.OnKeyBoardChangeListener {

    private static final String TAG = "MainActivity";
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JMessageClient.registerEventReceiver(this);
        setContentView(R.layout.activity_main);
        mChatView = (ChatView) findViewById(R.id.chat_view);
        mChatView.initModule();
        mContext = this;
        this.mWindow = getWindow();
        this.mImm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        mChatView.setListeners(this);
        mChatView.setOnTouchListener(this);
        mChatView.setOnSizeChangedListener(this);
        mChatView.setOnKbdStateListener(this);
        initReceiver();

        //更换用户时请在UserConfig中手动设置
        UserConfig userConfig = new UserConfig(false);
        mTargetId = userConfig.getTargetId();

        final Dialog loadingDialog = DialogCreator.createLoadingDialog(this, this.getString(R.string.loading));
        loadingDialog.show();
//        JMessageClient.register("user001", "1111", new BasicCallback() {
//            @Override
//            public void gotResult(int status, String desc) {
//                if (status == 0) {
//                    Log.d(TAG, "Register user1 success !");
//                } else {
//                    HandleResponseCode.onHandle(mContext, status, false);
//                }
//            }
//        });
//        JMessageClient.register("user002", "1111", new BasicCallback() {
//            @Override
//            public void gotResult(int status, String desc) {
//                if (status == 0) {
//                    Log.d(TAG, "Register user2 success !");
//                } else {
//                    HandleResponseCode.onHandle(mContext, status, false);
//                }
//            }
//        });
        JMessageClient.login(userConfig.getMyUsername(), userConfig.getPassword(), new BasicCallback() {
            @Override
            public void gotResult(int status, String desc) {
                loadingDialog.dismiss();
                if (status == 0) {
                    mConv = JMessageClient.getSingleConversation(mTargetId);
                    if (mConv == null) {
                        mConv = Conversation.createSingleConversation(mTargetId);
                    }
                    mChatAdapter = new MsgListAdapter(mContext, mTargetId);
                    mChatView.setChatListAdapter(mChatAdapter);
                    mChatAdapter.initMediaPlayer();
                    JMessageClient.getUserInfo(mTargetId, new GetUserInfoCallback() {
                        @Override
                        public void gotResult(int status, String desc, UserInfo userInfo) {
                            if (status == 0) {
                                mChatView.setChatTitle(userInfo.getUserName());
                            }
                        }
                    });
                } else {
                    HandleResponseCode.onHandle(mContext, status, false);
                }
            }
        });

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
        switch (v.getId()) {
            // 切换输入
            case R.id.switch_voice_ib:
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
                break;
            // 发送文本消息
            case R.id.send_msg_btn:
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
                break;
            // 点击添加按钮，弹出更多选项菜单
            case R.id.add_file_btn:
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
                break;
            // 拍照
            case R.id.pick_from_camera_btn:
                takePhoto();
                if (mChatView.getMoreMenu().getVisibility() == View.VISIBLE) {
                    mChatView.dismissMoreMenu();
                }
                break;
            case R.id.pick_from_local_btn:
                if (mChatView.getMoreMenu().getVisibility() == View.VISIBLE) {
                    mChatView.dismissMoreMenu();
                }
                Intent intent = new Intent();
                intent.putExtra(SimpleChatApplication.TARGET_ID, mTargetId);
                //TODO 发送本地图片
//                mContext.startPickPictureTotalActivity(intent);
                break;
        }
    }

    private void takePhoto() {
        if (FileHelper.isSdCardExist()) {
            mPhotoPath = FileHelper.createAvatarPath(null);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mPhotoPath)));
            try {
                startActivityForResult(intent, SimpleChatApplication.REQUEST_CODE_TAKE_PHOTO);
            } catch (ActivityNotFoundException anf) {
                Toast.makeText(mContext, mContext.getString(R.string.camera_not_prepared),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.sdcard_not_exist_toast),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 处理发送图片，刷新界面
     *
     * @param data intent
     */
    private void handleImgRefresh(Intent data) {
        String targetId = data.getStringExtra(SimpleChatApplication.TARGET_ID);
        long groupId = data.getLongExtra(SimpleChatApplication.GROUP_ID, 0);
        Log.i(TAG, "Refresh Image groupId: " + groupId);
        //判断是否在当前会话中发图片
        if (targetId != null) {
            if (targetId.equals(mTargetId)) {
                mChatAdapter.setSendImg(targetId, data.getIntArrayExtra(SimpleChatApplication.MsgIDs));
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
        JPushInterface.onResume(this);
        if (!RecordVoiceButton.mIsPressed)
            mChatView.dismissRecordDialog();
        String targetID = getIntent().getStringExtra(SimpleChatApplication.TARGET_ID);
        boolean isGroup = getIntent().getBooleanExtra(SimpleChatApplication.IS_GROUP, false);
        if (isGroup) {
            try {
                long groupID = getIntent().getLongExtra(SimpleChatApplication.GROUP_ID, 0);
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
        if (requestCode == SimpleChatApplication.REQUEST_CODE_TAKE_PHOTO) {
            try {
                Bitmap bitmap = BitmapLoader.getBitmapFromFile(mPhotoPath, 720, 1280);
                ImageContent.createImageContentAsync(bitmap, new ImageContent.CreateImageContentCallback() {
                    @Override
                    public void gotResult(int status, String desc, ImageContent imageContent) {
                        if (status == 0) {
                            Message msg = mConv.createSendMessage(imageContent);
                            Intent intent = new Intent();
                            intent.putExtra(SimpleChatApplication.MsgIDs, new int[]{msg.getId()});
                            intent.putExtra(SimpleChatApplication.TARGET_ID,
                                        ((UserInfo) mConv.getTargetInfo()).getUserName());
                            handleImgRefresh(intent);
                        }
                    }
                });
            } catch (NullPointerException e) {
                Log.i(TAG, "onActivityResult unexpected result");
            }
        } else if (resultCode == SimpleChatApplication.RESULT_CODE_SELECT_PICTURE) {
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
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity controller) {
            mActivity = new WeakReference<MainActivity>(controller);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            MainActivity activity = mActivity.get();
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
        //若为群聊相关事件，如添加、删除群成员
        Log.i(TAG, event.getMessage().toString());
        //刷新消息
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //收到消息的类型为单聊
                if (msg.getTargetType().equals(ConversationType.single)) {
                    String targetID = ((UserInfo) msg.getTargetInfo()).getUserName();
                    //判断消息是否在当前会话中
                    if (targetID.equals(mTargetId)) {
                        Message lastMsg = mChatAdapter.getLastMsg();
                        //收到的消息和Adapter中最后一条消息比较，如果最后一条为空或者不相同，则加入到MsgList
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

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (oldh - h > 300) {
            mShowSoftInput = true;
            mChatView.setMoreMenuHeight();
        } else {
            mShowSoftInput = false;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                switch (view.getId()) {
                    case R.id.chat_input_et:
                        if (mChatView.getMoreMenu().getVisibility() == View.VISIBLE && !mShowSoftInput) {
                            showSoftInputAndDismissMenu();
                            return false;
                        }else {
                            return false;
                        }
                }
                if (mChatView.getMoreMenu().getVisibility() == View.VISIBLE){
                    mChatView.dismissMoreMenu();
                }else if (mShowSoftInput){
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
