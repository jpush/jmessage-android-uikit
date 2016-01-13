package cn.jmessage.android.uikit.groupchatdetail;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;


public class ChatDetailView extends LinearLayout{

	private LinearLayout mAllGroupMemberLL;
	private LinearLayout mGroupNameLL;
	private LinearLayout mMyNameLL;
	private LinearLayout mGroupNumLL;
	private LinearLayout mGroupChatRecordLL;
	private LinearLayout mGroupChatDelLL;
	private ImageButton mReturnBtn;
	private TextView mTitle;
    private TextView mMembersNum;
	private ImageButton mMenuBtn;
	private Button mDelGroupBtn;
	private TextView mGroupName;
	private TextView mGroupNum;
	private TextView mMyName;
	private GroupGridView mGridView;
    private Context mContext;
	private View mDividingLine;

	public ChatDetailView(Context context, AttributeSet attrs) {
		super(context, attrs);
        this.mContext = context;
		// TODO Auto-generated constructor stub
	}
	
	public void initModule(){
        mAllGroupMemberLL = (LinearLayout) findViewById(IdHelper.getViewID(mContext, "jmui_all_member_ll"));
		mGroupNameLL = (LinearLayout) findViewById(IdHelper.getViewID(mContext, "jmui_group_name_ll"));
		mMyNameLL = (LinearLayout) findViewById(IdHelper.getViewID(mContext, "jmui_group_my_name_ll"));
		mGroupNumLL = (LinearLayout) findViewById(IdHelper.getViewID(mContext, "jmui_group_num_ll"));
		mGroupChatRecordLL = (LinearLayout) findViewById(IdHelper.getViewID(mContext, "jmui_group_chat_record_ll"));
		mGroupChatDelLL = (LinearLayout) findViewById(IdHelper.getViewID(mContext, "jmui_group_chat_del_ll"));
		mReturnBtn = (ImageButton) findViewById(IdHelper.getViewID(mContext, "jmui_return_btn"));
		mTitle = (TextView) findViewById(IdHelper.getViewID(mContext, "jmui_title"));
        mMembersNum = (TextView) findViewById(IdHelper.getViewID(mContext, "jmui_members_num"));
		mMenuBtn = (ImageButton) findViewById(IdHelper.getViewID(mContext, "jmui_right_btn"));
		mDelGroupBtn = (Button) findViewById(IdHelper.getViewID(mContext, "jmui_chat_detail_del_group"));
		mGroupName = (TextView) findViewById(IdHelper.getViewID(mContext, "jmui_chat_detail_group_name"));
		mGroupNum = (TextView) findViewById(IdHelper.getViewID(mContext, "jmui_chat_detail_group_num"));
		mDividingLine = findViewById(IdHelper.getViewID(mContext, "jmui_group_num_dividing_line"));
		mMyName = (TextView) findViewById(IdHelper.getViewID(mContext, "jmui_chat_detail_my_name"));
		mGridView = (GroupGridView) findViewById(IdHelper.getViewID(mContext, "jmui_chat_detail_group_gv"));

		mTitle.setText(mContext.getString(IdHelper.getString(mContext, "jmui_chat_detail_title")));
		mMenuBtn.setVisibility(View.GONE);
		//自定义GridView点击背景为透明色
		mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
	}
	
	public void setListeners(OnClickListener onClickListener) {
        mAllGroupMemberLL.setOnClickListener(onClickListener);
		mGroupNameLL.setOnClickListener(onClickListener);
		mMyNameLL.setOnClickListener(onClickListener);
		mGroupNumLL.setOnClickListener(onClickListener);
		mGroupChatRecordLL.setOnClickListener(onClickListener);
		mGroupChatDelLL.setOnClickListener(onClickListener);
	    mReturnBtn.setOnClickListener(onClickListener);
		mDelGroupBtn.setOnClickListener(onClickListener);
	}
	
	public void setItemListener(OnItemClickListener listener) {
		mGridView.setOnItemClickListener(listener);
	}
	
//	public void setLongClickListener(OnItemLongClickListener listener) {
//		mGridView.setOnItemLongClickListener(listener);
//	}

	public void setAdapter(GroupMemberGridAdapter adapter) {
		mGridView.setAdapter(adapter);
	}

	public void setGroupName(String str) {
		mGroupName.setText(str);
	}

	public void setMyName(String str) {
		mMyName.setText(str);
	}
	
	public void setSingleView() {
		mGroupNameLL.setVisibility(View.GONE);
		mGroupNumLL.setVisibility(View.GONE);
		mDividingLine.setVisibility(View.GONE);
		mMyNameLL.setVisibility(View.GONE);
		mDelGroupBtn.setVisibility(View.GONE);
	}

    public void updateGroupName(String newName) {
        mGroupName.setText(newName);
    }

	public void setTitle(int size) {
		String title = mContext.getString(IdHelper.getString(mContext, "jmui_chat_detail_title"))
				+ mContext.getString(IdHelper.getString(mContext, "jmui_combine_title"));
		mTitle.setText(String.format(title, size));
	}

    public GroupGridView getGridView() {
        return mGridView;
    }

    public void setMembersNum(int size) {
        String text = mContext.getString(IdHelper.getString(mContext, "jmui_all_group_members"))
                + mContext.getString(IdHelper.getString(mContext, "jmui_combine_title"));
        mMembersNum.setText(String.format(text, size));
    }

}
