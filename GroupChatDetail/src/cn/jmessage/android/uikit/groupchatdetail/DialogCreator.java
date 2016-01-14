package cn.jmessage.android.uikit.groupchatdetail;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;



public class DialogCreator {
    public static Dialog createLoadingDialog(Context context, String msg) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(IdHelper.getLayout(context, "jmui_loading_view"), null);
        RelativeLayout layout = (RelativeLayout) v.findViewById(IdHelper.getViewID(context, "jmui_dialog_view"));
        ImageView mLoadImg = (ImageView) v.findViewById(IdHelper.getViewID(context, "jmui_loading_img"));
        TextView mLoadText = (TextView) v.findViewById(IdHelper.getViewID(context, "jmui_loading_txt"));
        AnimationDrawable mDrawable = (AnimationDrawable) mLoadImg.getDrawable();
        mDrawable.start();
        mLoadText.setText(msg);
        final Dialog loadingDialog = new Dialog(context, IdHelper.getStyle(context, "jmui_loading_dialog_style"));
        loadingDialog.setCancelable(true);
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        return loadingDialog;
    }

    public static Dialog createBaseCustomDialog(Context context, String title, String text,
                                                View.OnClickListener onClickListener) {
        Dialog baseDialog = new Dialog(context, IdHelper.getStyle(context, "jmui_default_dialog_style"));
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(IdHelper.getLayout(context, "jmui_dialog_base"), null);
        TextView titleTv = (TextView) v.findViewById(IdHelper.getViewID(context, "jmui_dialog_base_title_tv"));
        TextView textTv = (TextView) v.findViewById(IdHelper.getViewID(context, "jmui_dialog_base_text_tv"));
        Button confirmBtn = (Button) v.findViewById(IdHelper.getViewID(context, "jmui_dialog_base_confirm_btn"));
        titleTv.setText(title);
        textTv.setText(text);
        confirmBtn.setOnClickListener(onClickListener);
        baseDialog.setContentView(v);
        baseDialog.setCancelable(true);
        return baseDialog;
    }

    public static Dialog createExitGroupDialog(Context context, View.OnClickListener listener){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(IdHelper.getLayout(context, "jmui_dialog_base_with_button"), null);
        builder.setView(v);
        Dialog dialog = builder.create();
        TextView title = (TextView) v.findViewById(IdHelper.getViewID(context, "jmui_title"));
        title.setText(context.getString(IdHelper.getString(context, "jmui_delete_group_confirm_title")));
        final Button cancel = (Button) v.findViewById(IdHelper.getViewID(context, "jmui_cancel_btn"));
        final Button commit = (Button) v.findViewById(IdHelper.getViewID(context, "jmui_commit_btn"));
        commit.setText(context.getString(IdHelper.getString(context, "jmui_confirm")));
        cancel.setOnClickListener(listener);
        commit.setOnClickListener(listener);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    public static Dialog createDeleteMemberDialog(Context context, View.OnClickListener listener,
                                                  boolean isSingle) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(IdHelper.getLayout(context, "jmui_dialog_base_with_button"), null);
        builder.setView(view);
        TextView title = (TextView) view.findViewById(IdHelper.getViewID(context, "jmui_title"));
        if (isSingle) {
            title.setText(context.getString(IdHelper.getString(context, "jmui_delete_member_confirm_hint")));
        } else {
            title.setText(context.getString(IdHelper.getString(context, "jmui_delete_confirm_hint")));
        }
        final Button cancel = (Button) view.findViewById(IdHelper.getViewID(context, "jmui_cancel_btn"));
        final Button commit = (Button) view.findViewById(IdHelper.getViewID(context, "jmui_commit_btn"));
        cancel.setOnClickListener(listener);
        commit.setOnClickListener(listener);
        commit.setText(context.getString(IdHelper.getString(context, "jmui_confirm")));
        final Dialog dialog = builder.create();
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

}
