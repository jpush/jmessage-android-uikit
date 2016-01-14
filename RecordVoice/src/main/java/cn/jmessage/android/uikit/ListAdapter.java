package cn.jmessage.android.uikit;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListAdapter extends BaseAdapter {

    private final static String TAG = "ListAdapter";

    private List<VoiceMessage> mMsgList = new ArrayList<>();
    private Context mContext;
    private Activity mActivity;
    private LayoutInflater mInflater;
    private float mDensity;
    private final MediaPlayer mp = new MediaPlayer();
    private AnimationDrawable mVoiceAnimation;
    private FileInputStream mFIS;
    private FileDescriptor mFD;
    private boolean mSetData = false;
    private int mPosition = -1;// 和mSetData一起组成判断播放哪条录音的依据
    private boolean mIsEarPhoneOn;

    public ListAdapter(Context context) {
        this.mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mActivity = (Activity) context;
        DisplayMetrics dm = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        mDensity = dm.density;
        AudioManager audioManager = (AudioManager) mContext
                .getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_NORMAL);
        if (audioManager.isSpeakerphoneOn()) {
            audioManager.setSpeakerphoneOn(true);
        } else {
            audioManager.setSpeakerphoneOn(false);
        }
        mp.setAudioStreamType(AudioManager.STREAM_RING);
        mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return false;
            }
        });
    }

    public void initMediaPlayer() {
        mp.reset();
    }

    public void releaseMediaPlayer() {
        if (mp != null)
            mp.release();
    }


    public void addToMsgList(VoiceMessage msg) {
        mMsgList.add(msg);
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    public void setAudioPlayByEarPhone(int state) {
        AudioManager audioManager = (AudioManager) mContext
                .getSystemService(Context.AUDIO_SERVICE);
        int currVolume = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        audioManager.setMode(AudioManager.MODE_IN_CALL);
        if (state == 0) {
            mIsEarPhoneOn = false;
            audioManager.setSpeakerphoneOn(true);
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),
                    AudioManager.STREAM_VOICE_CALL);
            Log.i(TAG, "set SpeakerphoneOn true!");
        } else {
            mIsEarPhoneOn = true;
            audioManager.setSpeakerphoneOn(false);
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, currVolume,
                    AudioManager.STREAM_VOICE_CALL);
            Log.i(TAG, "set SpeakerphoneOn false!");
        }
    }

    public void stopMediaPlayer() {
        if (mp.isPlaying())
            mp.stop();
    }

    @Override
    public int getCount() {
        return mMsgList.size();
    }

    @Override
    public Object getItem(int position) {
        return mMsgList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.jmui_chat_item_send_voice, null);
            holder = new ViewHolder();
            holder.headIcon = (CircleImageView) convertView.findViewById(R.id.avatar_iv);
            holder.txtContent = (TextView) convertView.findViewById(R.id.msg_content);
            holder.voice = (ImageView) convertView.findViewById(R.id.voice_iv);
            holder.voiceLength = (TextView) convertView.findViewById(R.id.voice_length_tv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.headIcon.setImageResource(R.drawable.jmui_head_icon);
        final VoiceMessage msg = mMsgList.get(position);
        int length = msg.getDuration();
        String voiceLength = length + mContext.getString(R.string.jmui_symbol_second);
        holder.voiceLength.setText(voiceLength);
        final String path = msg.getPath();
        //控制语音长度显示，长度增幅随语音长度逐渐缩小
        int width = (int) (-0.04 * length * length + 4.526 * length + 75.214);
        holder.voice.setImageResource(R.drawable.jmui_send_3);
        holder.txtContent.setWidth((int) (width * mDensity));
        holder.txtContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    Toast.makeText(mContext, mContext.getString(R.string.jmui_sdcard_not_exist_toast),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // 如果之前存在播放动画，无论这次点击触发的是暂停还是播放，停止上次播放的动画
                    if (mVoiceAnimation != null) {
                        mVoiceAnimation.stop();
                    }
                    // 播放中点击了正在播放的Item 则暂停播放
                    if (mp.isPlaying() && mPosition == position) {
                        holder.voice.setImageResource(R.drawable.jmui_voice_send);
                        mVoiceAnimation = (AnimationDrawable) holder.voice.getDrawable();
                        pauseVoice();
                        mVoiceAnimation.stop();
                    // 开始播放录音
                    } else {
                        try {
                            holder.voice.setImageResource(R.drawable.jmui_voice_send);
                            mVoiceAnimation = (AnimationDrawable) holder.voice.getDrawable();

                            // 继续播放之前暂停的录音
                            if (mSetData && mPosition == position) {
                                playVoice();
                                // 否则重新播放该录音或者其他录音
                            } else {
                                mp.reset();
                                // 记录播放录音的位置
                                mPosition = position;
                                Log.i(TAG, "content.getLocalPath:" + msg.getPath());
                                mFIS = new FileInputStream(path);
                                mFD = mFIS.getFD();
                                mp.setDataSource(mFD);
                                if (mIsEarPhoneOn) {
                                    mp.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
                                } else {
                                    mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                }

                                mp.prepare();
                                playVoice();
                            }
                        } catch (NullPointerException e) {
                            Toast.makeText(mActivity, mContext.getString(R.string.jmui_file_not_found_toast),
                                    Toast.LENGTH_SHORT).show();
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        } catch (SecurityException e) {
                            e.printStackTrace();
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            Toast.makeText(mActivity, mContext.getString(R.string.jmui_file_not_found_toast),
                                    Toast.LENGTH_SHORT).show();
                        } finally {
                            try {
                                if (mFIS != null) {
                                    mFIS.close();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

            private void playVoice() {
                mVoiceAnimation.start();
                mp.start();
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer arg0) {
                        mVoiceAnimation.stop();
                        mp.reset();
                        mSetData = false;
                        // 播放完毕，恢复初始状态
                        holder.voice.setImageResource(R.drawable.jmui_send_3);
                    }
                });
            }

            private void pauseVoice() {
                mp.pause();
                mSetData = true;
            }
        });

        return convertView;
    }

    public static class ViewHolder {
        CircleImageView headIcon;
        TextView txtContent;
        TextView voiceLength;
        ImageView voice;
    }
}
