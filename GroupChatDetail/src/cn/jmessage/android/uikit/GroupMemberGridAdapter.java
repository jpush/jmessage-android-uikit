package cn.jmessage.android.uikit;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.jmessage.android.uicomponents.R;
import cn.jmessage.android.uikit.groupchatdetail.BitmapLoader;
import cn.jmessage.android.uikit.groupchatdetail.CircleImageView;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.callback.GetGroupInfoCallback;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.UserInfo;

public class GroupMemberGridAdapter extends BaseAdapter implements MainActivity.RefreshMemberListener {

    private static final String TAG = "GroupMemberGridAdapter";

    private LayoutInflater mInflater;
    //群成员列表
    private List<UserInfo> mMemberList = new ArrayList<UserInfo>();
    private boolean mIsCreator = false;
    //群成员个数
    private int mCurrentNum;
    //记录空白项的数组
    private int[] mRestArray = new int[]{2, 1, 0, 3};
    //用群成员项数余4得到，作为下标查找mRestArray，得到空白项
    private int mRestNum;
    private boolean mIsGroup;
    private String mTargetId;
    private Context mContext;
    private int mAvatarSize;

    //群聊
    public GroupMemberGridAdapter(Context context, List<UserInfo> memberList, boolean isCreator,
                                  int size) {
        this.mContext = context;
        mInflater = LayoutInflater.from(context);
        mIsGroup = true;
        this.mMemberList = memberList;
        mCurrentNum = mMemberList.size();
        this.mIsCreator = isCreator;
        this.mAvatarSize = size;
        initBlankItem();
    }

    //单聊
    public GroupMemberGridAdapter(Context context, String targetId) {
        this.mContext = context;
        mInflater = LayoutInflater.from(context);
        this.mTargetId = targetId;
    }

    public void initBlankItem() {
        if (mMemberList.size() > 40) {
            mCurrentNum = 39;
        } else {
            mCurrentNum = mMemberList.size();
        }
        mRestNum = mRestArray[mCurrentNum % 4];
    }

    @Override
    public void onRefreshMemberList(List<UserInfo> memberList) {
        mMemberList = memberList;
        if (mMemberList.size() > 40) {
            mCurrentNum = 39;
        } else {
            mCurrentNum = mMemberList.size();
        }
        mRestNum = mRestArray[mCurrentNum % 4];
        notifyDataSetChanged();
        Log.d(TAG, "Refreshed!");
    }

    public void refreshMemberList(long groupId) {
        //由于此demo没有Conversation,只能每次都从服务器更新群成员信息
//        Conversation conv = JMessageClient.getGroupConversation(groupId);
//        GroupInfo groupInfo = (GroupInfo) conv.getTargetInfo();
        JMessageClient.getGroupInfo(groupId, new GetGroupInfoCallback() {
            @Override
            public void gotResult(int status, String desc, GroupInfo groupInfo) {
                if (status == 0) {
                    mMemberList = groupInfo.getGroupMembers();
                    if (mMemberList.size() > 40) {
                        mCurrentNum = 39;
                    } else {
                        mCurrentNum = mMemberList.size();
                    }
                    mRestNum = mRestArray[mCurrentNum % 4];
                    notifyDataSetChanged();
                } else {
                    HandleResponseCode.onHandle(mContext, status, false);
                }
            }
        });
    }

    @Override
    public int getCount() {
        //如果是普通成员，并且群组成员余4等于3，特殊处理，隐藏下面一栏空白
        if (mCurrentNum % 4 == 3 && !mIsCreator) {
            return mCurrentNum + 1;
        } else {
            return mCurrentNum + mRestNum + 2;
        }
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.group_grid_view_item, null);
            holder = new ViewHolder((CircleImageView) convertView.findViewById(R.id.grid_avatar),
                    (TextView) convertView.findViewById(R.id.grid_name));
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        //群聊
        if (mIsGroup) {
            //群成员
            if (position < mMemberList.size()) {
                UserInfo userInfo = mMemberList.get(position);
                holder.icon.setVisibility(View.VISIBLE);
                holder.name.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(userInfo.getAvatar())) {
                    File file = userInfo.getAvatarFile();
                    if (file != null && file.isFile()) {
                        Bitmap bitmap = BitmapLoader.getBitmapFromFile(file.getAbsolutePath(),
                                mAvatarSize, mAvatarSize);
                        holder.icon.setImageBitmap(bitmap);
                    } else {
                        userInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                            @Override
                            public void gotResult(int status, String desc, Bitmap bitmap) {
                                if (status == 0) {
                                    holder.icon.setImageBitmap(bitmap);
                                } else {
                                    holder.icon.setImageResource(R.drawable.head_icon);
                                    HandleResponseCode.onHandle(mContext, status, false);
                                }
                            }
                        });
                    }
                } else {
                    holder.icon.setImageResource(R.drawable.head_icon);
                }

                if (TextUtils.isEmpty(userInfo.getNickname())) {
                    holder.name.setText(userInfo.getUserName());
                } else {
                    holder.name.setText(userInfo.getNickname());
                }
            }
            if (position < mCurrentNum) {
                holder.icon.setVisibility(View.VISIBLE);
                holder.name.setVisibility(View.VISIBLE);
            } else if (position == mCurrentNum) {
                holder.icon.setImageResource(R.drawable.chat_detail_add);
                holder.icon.setVisibility(View.VISIBLE);
                holder.name.setVisibility(View.INVISIBLE);

                //设置删除群成员按钮
            } else if (position == mCurrentNum + 1) {
                if (mIsCreator && mCurrentNum > 1) {
                    holder.icon.setImageResource(R.drawable.chat_detail_del);
                    holder.icon.setVisibility(View.VISIBLE);
                    holder.name.setVisibility(View.INVISIBLE);
                } else {
                    holder.icon.setVisibility(View.GONE);
                    holder.name.setVisibility(View.GONE);
                }
                //空白项
            } else {
                holder.icon.setVisibility(View.INVISIBLE);
                holder.name.setVisibility(View.INVISIBLE);
            }
        } else {
            if (position == 0) {
                Conversation conv = JMessageClient.getSingleConversation(mTargetId);
                UserInfo userInfo = (UserInfo) conv.getTargetInfo();
                if (!TextUtils.isEmpty(userInfo.getAvatar())) {
                    userInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                        @Override
                        public void gotResult(int status, String desc, Bitmap bitmap) {
                            if (status == 0) {
                                Log.d(TAG, "Get small avatar success");
                                holder.icon.setImageBitmap(bitmap);
                            } else {
                                HandleResponseCode.onHandle(mContext, status, false);
                            }
                        }
                    });
                }
                if (TextUtils.isEmpty(userInfo.getNickname())) {
                    holder.name.setText(userInfo.getUserName());
                } else {
                    holder.name.setText(userInfo.getNickname());
                }
                holder.icon.setVisibility(View.VISIBLE);
                holder.name.setVisibility(View.VISIBLE);
            } else {
                holder.icon.setImageResource(R.drawable.chat_detail_add);
                holder.icon.setVisibility(View.VISIBLE);
                holder.name.setVisibility(View.INVISIBLE);
            }

        }

        return convertView;
    }

    public void setCreator(boolean isCreator) {
        mIsCreator = isCreator;
        notifyDataSetChanged();
    }

    class ViewHolder {

        protected CircleImageView icon;
        protected TextView name;

        public ViewHolder(CircleImageView icon, TextView name) {
            this.icon = icon;
            this.name = name;
        }
    }
}
