package cn.jmessage.android.uicomponents;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.jmessage.android.uicomponents.groupchatdetail.ChatDetailView;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetGroupInfoCallback;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.content.EventNotificationContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;

public class MainActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static final String TAG = "MainActivity";

    private static final String GROUP_ID = "groupId";
    private static final String DELETE_MODE = "deleteMode";
    private static final String MEMBERS_COUNT = "membersCount";
    private static final int REQUEST_CODE_ALL_MEMBER = 100;
    private static final int ON_GROUP_EVENT = 101;
    private static final int GET_GROUP_INFO_SUCCESS = 102;

    private ChatDetailView mChatDetailView;
    private Context mContext;
    private ProgressDialog mProgressDialog;
    private Dialog mDialog;
    private Dialog mLoadingDialog;
    private long mGroupId;
    private String mGroupName;
    private GroupMemberGridAdapter mAdapter;
    private List<UserInfo> mMembersList;
    // 当前GridView群成员项数
    private int mCurrentNum;
    private boolean mIsCreator;
    private RefreshMemberListener mListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        mChatDetailView = (ChatDetailView) findViewById(R.id.chat_detail_view);
        mChatDetailView.initModule();
        mChatDetailView.setListeners(this);
        mChatDetailView.setItemListener(this);
        mChatDetailView.setGroupName(this.getString(R.string.chat_detail_title));
        mLoadingDialog = DialogCreator.createLoadingDialog(this, this.getString(R.string.loading));
        mLoadingDialog.show();
        Log.d(TAG, "MainActivity onCreated!");
        //配置自己的用户信息
        final UserConfig config = new UserConfig(true);
        //此demo默认获取到的群信息,可以调用JMessageClient.createGroup或者使用Rest API创建
        mGroupId = 10048379;
        JMessageClient.login(config.getMyUsername(), config.getPassword(), new BasicCallback() {
            @Override
            public void gotResult(int status, String desc) {
                mLoadingDialog.dismiss();
                if (status == 0) {
                    Log.d(TAG, "Login success");
                    JMessageClient.getGroupInfo(mGroupId, new GetGroupInfoCallback() {
                        @Override
                        public void gotResult(int status, String desc, GroupInfo groupInfo) {
                            if (status == 0) {
                                mMembersList = groupInfo.getGroupMembers();
                                String owner = groupInfo.getGroupOwner();
                                if (config.getMyUsername().equals(owner)) {
                                    mIsCreator = true;
                                }
                                mGroupName = groupInfo.getGroupName();
                                if (!TextUtils.isEmpty(mGroupName)) {
                                    mChatDetailView.setGroupName(mGroupName);
                                }
                                mHandler.sendEmptyMessage(GET_GROUP_INFO_SUCCESS);
                            }
                        }
                    });
                } else {
                    HandleResponseCode.onHandle(mContext, status, false);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            //显示所有群成员
            case R.id.all_member_ll:
                intent.putExtra(GROUP_ID, mGroupId);
                intent.putExtra(DELETE_MODE, false);
                intent.setClass(mContext, MembersInChatActivity.class);
                startActivityForResult(intent, REQUEST_CODE_ALL_MEMBER);
                break;

            // 设置群组名称
            case R.id.group_name_ll:
                showGroupNameSettingDialog(mGroupId, mGroupName);
                break;

            case R.id.chat_detail_del_group:
                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switch (view.getId()) {
                            case R.id.cancel_btn:
                                mDialog.cancel();
                                break;
                            case R.id.commit_btn:
                                //TODO exit group
                                mDialog.cancel();
                                break;
                        }
                    }
                };
                mDialog = DialogCreator.createExitGroupDialog(mContext, listener);
                mDialog.show();
                break;
        }
    }

    //设置群聊名称
    public void showGroupNameSettingDialog(final long groupID, String groupName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_reset_password, null);
        builder.setView(view);
        TextView title = (TextView) view.findViewById(R.id.title_tv);
        title.setText(mContext.getString(R.string.group_name_hit));
        final EditText pwdEt = (EditText) view.findViewById(R.id.password_et);
        pwdEt.addTextChangedListener(new TextWatcher() {
            private CharSequence temp = "";
            private int editStart;
            private int editEnd;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                temp = s;
            }

            @Override
            public void afterTextChanged(Editable s) {
                editStart = pwdEt.getSelectionStart();
                editEnd = pwdEt.getSelectionEnd();
                byte[] data = temp.toString().getBytes();
                if (data.length > 64) {
                    s.delete(editStart - 1, editEnd);
                    int tempSelection = editStart;
                    pwdEt.setText(s);
                    pwdEt.setSelection(tempSelection);
                }
            }
        });
        pwdEt.setInputType(InputType.TYPE_CLASS_TEXT);
        pwdEt.setHint(groupName);
        pwdEt.setHintTextColor(getResources().getColor(R.color.gray));
        final Button cancel = (Button) view.findViewById(R.id.cancel_btn);
        final Button commit = (Button) view.findViewById(R.id.commit_btn);
        final Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setLayout((int) (0.8 * mWidth), WindowManager.LayoutParams.WRAP_CONTENT);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.cancel_btn:
                        dialog.cancel();
                        break;
                    case R.id.commit_btn:
                        final String newName = pwdEt.getText().toString().trim();
                        if (newName.equals("")) {
                            Toast.makeText(mContext, mContext.getString(R.string.group_name_not_null_toast),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            dismissSoftInput();
                            dialog.dismiss();
                            mProgressDialog = new ProgressDialog(mContext);
                            mProgressDialog.setMessage(mContext.getString(R.string.modifying_hint));
                            mProgressDialog.show();
                            JMessageClient.updateGroupName(groupID, newName, new BasicCallback() {
                                @Override
                                public void gotResult(final int status, final String desc) {
                                    mProgressDialog.dismiss();
                                    if (status == 0) {
                                        mChatDetailView.updateGroupName(newName);
                                        Toast.makeText(mContext, mContext.getString(R.string.modify_success_toast),
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        Log.i(TAG, "desc :" + desc);
                                        HandleResponseCode.onHandle(mContext, status, false);
                                    }
                                }
                            });
                        }
                        break;
                }
            }
        };
        cancel.setOnClickListener(listener);
        commit.setOnClickListener(listener);

    }

    private void dismissSoftInput() {
        //隐藏软键盘
        InputMethodManager imm = ((InputMethodManager) mContext
                .getSystemService(Activity.INPUT_METHOD_SERVICE));
        if (this.getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (this.getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    @Override
    public void handleMsg(Message msg) {
        switch (msg.what) {
            case ON_GROUP_EVENT:
//                mAdapter.refreshMemberList(mGroupId);
                refreshMemberList();
                break;
            case GET_GROUP_INFO_SUCCESS:
                mAdapter = new GroupMemberGridAdapter(mContext, mMembersList, mIsCreator, mAvatarSize);
                setRMLListener(mAdapter);
                if (mMembersList.size() > 40) {
                    mCurrentNum = 39;
                } else {
                    mCurrentNum = mMembersList.size();
                }
                mChatDetailView.setAdapter(mAdapter);
                mChatDetailView.setMembersNum(mMembersList.size());
                break;
        }
    }

    @Override
    protected void onPause() {
        JPushInterface.onPause(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        JPushInterface.onResume(this);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        JMessageClient.unRegisterEventReceiver(this);
        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        // 点击添加按钮
         if (position == mCurrentNum) {
            addMemberToGroup();

            // 是群主, 成员个数大于1并点击删除按钮
        } else if (position == mCurrentNum + 1 && mIsCreator && mCurrentNum > 1) {
            intent.putExtra(DELETE_MODE, true);
            intent.putExtra(GROUP_ID, mGroupId);
            intent.setClass(mContext, MembersInChatActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ALL_MEMBER);
        }
    }

    //点击添加按钮触发事件
    private void addMemberToGroup() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        final View view = LayoutInflater.from(mContext)
                .inflate(R.layout.dialog_add_friend_to_conv_list, null);
        builder.setView(view);
        TextView title = (TextView) view.findViewById(R.id.dialog_name);
        title.setText(mContext.getString(R.string.add_friend_to_group_title));
        final EditText userNameEt = (EditText) view.findViewById(R.id.user_name_et);
        final Button cancel = (Button) view.findViewById(R.id.cancel_btn);
        final Button commit = (Button) view.findViewById(R.id.commit_btn);
        final Dialog dialog = builder.create();
        dialog.show();
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.cancel_btn:
                        dialog.cancel();
                        break;
                    case R.id.commit_btn:
                        final String targetId = userNameEt.getText().toString().trim();
                        Log.i(TAG, "targetID " + targetId);
                        if (TextUtils.isEmpty(targetId)) {
                            Toast.makeText(mContext, mContext.getString(R.string.username_not_null_toast),
                                    Toast.LENGTH_SHORT).show();
                            break;
                            //检查群组中是否包含该用户
                        } else if (checkIfNotContainUser(targetId)) {
                            mLoadingDialog = DialogCreator.createLoadingDialog(mContext,
                                    mContext.getString(R.string.searching_user));
                            mLoadingDialog.show();
                            getUserInfo(targetId, dialog);
                        } else {
                            dialog.cancel();
                            Toast.makeText(mContext, mContext.getString(R.string.user_already_exist_toast),
                                    Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        };
        cancel.setOnClickListener(listener);
        commit.setOnClickListener(listener);
    }

    /**
     * 添加成员时检查是否存在该群成员
     *
     * @param targetID 要添加的用户
     * @return 返回是否存在该用户
     */
    private boolean checkIfNotContainUser(String targetID) {
        if (mMembersList != null) {
            for (UserInfo userInfo : mMembersList) {
                if (userInfo.getUserName().equals(targetID))
                    return false;
            }
            return true;
        }
        return true;
    }

    private void getUserInfo(final String targetId, final Dialog dialog){
        JMessageClient.getUserInfo(targetId, new GetUserInfoCallback() {
            @Override
            public void gotResult(final int status, String desc, final UserInfo userInfo) {
                if (mLoadingDialog != null) {
                    mLoadingDialog.dismiss();
                }
                if (status == 0) {
                    addAMember(userInfo);
                    dialog.cancel();
                } else {
                    HandleResponseCode.onHandle(mContext, status, true);
                }
            }
        });
    }

    /**
     * @param userInfo 要增加的成员的用户名，目前一次只能增加一个
     */
    private void addAMember(final UserInfo userInfo) {
        mLoadingDialog = DialogCreator.createLoadingDialog(mContext,
                mContext.getString(R.string.adding_hint));
        mLoadingDialog.show();
        ArrayList<String> list = new ArrayList<>();
        list.add(userInfo.getUserName());
        JMessageClient.addGroupMembers(mGroupId, list, new BasicCallback() {

            @Override
            public void gotResult(final int status, final String desc) {
                if (status == 0) {
//                    mAdapter.refreshMemberList(mGroupId);
                    refreshMemberList();
                    mCurrentNum++;
                    mChatDetailView.setTitle(mMembersList.size() + 1);
                    mChatDetailView.setMembersNum(mMembersList.size() + 1);
                    mLoadingDialog.dismiss();
                } else {
                    mLoadingDialog.dismiss();
                    HandleResponseCode.onHandle(mContext, status, true);
                }
            }
        });
    }

    /**
     * 接收群成员变化事件
     *
     * @param event 消息事件
     */
    public void onEvent(MessageEvent event) {
        final cn.jpush.im.android.api.model.Message msg = event.getMessage();
        if (msg.getContentType() == ContentType.eventNotification) {
            EventNotificationContent.EventNotificationType msgType = ((EventNotificationContent) msg
                    .getContent()).getEventNotificationType();
            switch (msgType) {
                //添加群成员事件特殊处理
                case group_member_added:
                    List<String> userNames = ((EventNotificationContent) msg.getContent()).getUserNames();
                    for (final String userName : userNames) {
                        JMessageClient.getUserInfo(userName, new GetUserInfoCallback() {
                            @Override
                            public void gotResult(int status, String desc, UserInfo userInfo) {
                                if (status == 0) {
                                    mAdapter.notifyDataSetChanged();
                                } else {
                                    HandleResponseCode.onHandle(mContext, status, false);
                                }
                            }
                        });
                    }
                    break;
                case group_member_removed:
                    break;
                case group_member_exit:
                    break;
            }
            //无论是否添加群成员，刷新界面
            mHandler.sendEmptyMessage(ON_GROUP_EVENT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ALL_MEMBER) {
            refreshMemberList();
        }
    }

    /**
     * 此demo没有Conversation,所以每次都要调用getGroupInfo更新群成员信息
     */
    private void refreshMemberList() {
        JMessageClient.getGroupInfo(mGroupId, new GetGroupInfoCallback() {
            @Override
            public void gotResult(int status, String desc, GroupInfo groupInfo) {
                if (status == 0) {
                    mMembersList = groupInfo.getGroupMembers();
                    mListener.onRefreshMemberList(mMembersList);
                    mChatDetailView.setTitle(mMembersList.size());
                    mChatDetailView.setMembersNum(mMembersList.size());
                } else {
                    HandleResponseCode.onHandle(mContext, status, false);
                }
            }
        });
    }

    public void setRMLListener(RefreshMemberListener listener) {
        mListener = listener;
    }

    interface RefreshMemberListener {
        public void onRefreshMemberList(List<UserInfo> memberList);
    }
}
