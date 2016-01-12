package cn.jmessage.android.uikit.chatting.utils;

import android.content.Context;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

import cn.jpush.android.Configs;


/**
 * Created by Ken on 2015/2/26.
 */
public class TimeFormat {

//    public static TimeFormat timeFormat = new TimeFormat();
//
//    public static TimeFormat getInstance(){
//        return timeFormat;
//    }

    private long mTimeStamp;
    private Context mContext;

    public TimeFormat(Context context, long timeStamp) {
        this.mContext = context;
        this.mTimeStamp = timeStamp;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm ");
        String date = format.format(timeStamp);
    }

    //用于显示会话时间
    public String getTime() {
        long currentTime = Configs.getReportTime();
        Date date1 = new Date(currentTime);
        Date date2 = new Date(mTimeStamp);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm ");
        String date = format.format(mTimeStamp);
        int hour = Integer.parseInt(date.substring(0, 2));
        //今天
        if (date1.getDate() - date2.getDate() == 0) {
            if (hour < 6)
                return mContext.getString(IdHelper.getString(mContext, "jmui_before_dawn")) + " " + date;
            else if (hour < 12)
                return mContext.getString(IdHelper.getString(mContext, "jmui_morning")) + " " + date;
            else if (hour < 18)
                return mContext.getString(IdHelper.getString(mContext, "jmui_afternoon")) + " " + date;
            else return mContext.getString(IdHelper.getString(mContext, "jmui_night")) + " " + date;
            //昨天
        } else if (date1.getDate() - date2.getDate() == 1) {
            return mContext.getString(IdHelper.getString(mContext, "jmui_yesterday"));
        } else if (date1.getDay() - date2.getDay() > 0) {
            if (date2.getDay() == 1) {
                return mContext.getString(IdHelper.getString(mContext, "jmui_monday"));
            } else if (date2.getDay() == 2) {
                return mContext.getString(IdHelper.getString(mContext, "jmui_tuesday"));
            } else if (date2.getDay() == 3) {
                return mContext.getString(IdHelper.getString(mContext, "jmui_wednesday"));
            } else if (date2.getDay() == 4) {
                return mContext.getString(IdHelper.getString(mContext, "jmui_thursday"));
            } else if (date2.getDay() == 5) {
                return mContext.getString(IdHelper.getString(mContext, "jmui_friday"));
            } else if (date2.getDay() == 6) {
                return mContext.getString(IdHelper.getString(mContext, "jmui_saturday"));
            } else {
                return mContext.getString(IdHelper.getString(mContext, "jmui_sunday"));
            }

        } else if (date1.getYear() == date2.getYear()) {
            return date2.getMonth() + 1 + mContext.getString(IdHelper.getString(mContext, "jmui_month"))
                    + date2.getDate() + mContext.getString(IdHelper.getString(mContext, "jmui_day"));
        } else {
            return format1.format(mTimeStamp);
        }
    }

    //用于显示消息具体时间
    public String getDetailTime() {
        long currentTime = Configs.getReportTime();
        Date date1 = new Date(currentTime);
        Date date2 = new Date(mTimeStamp);
        SimpleDateFormat format = new SimpleDateFormat(mContext.getString(IdHelper.getString(mContext,
                "jmui_time_format_hour")), Locale.CHINA);
        String date = format.format(mTimeStamp);
        SimpleDateFormat format1 = new SimpleDateFormat(mContext.getString(IdHelper.getString(mContext,
                "jmui_time_format_year")), Locale.CHINA);
        String date3 = format1.format(mTimeStamp);
        int hour = Integer.parseInt(date.substring(0, 2));
        if (date1.getDate() - date2.getDate() == 0) {
            if (hour < 6)
                return mContext.getString(IdHelper.getString(mContext, "jmui_before_dawn")) + date;
            else if (hour < 12)
                return mContext.getString(IdHelper.getString(mContext, "jmui_morning")) + date;
            else if (hour < 18)
                return mContext.getString(IdHelper.getString(mContext, "jmui_afternoon")) + date;
            else return mContext.getString(IdHelper.getString(mContext, "jmui_night")) + date;
        } else if (date1.getDate() - date2.getDate() == 1) {
            if (hour < 6) {
                return mContext.getString(IdHelper.getString(mContext, "jmui_yesterday")) + " "
                        + mContext.getString(IdHelper.getString(mContext, "jmui_before_dawn")) + date;
            } else if (hour < 12) {
                return mContext.getString(IdHelper.getString(mContext, "jmui_yesterday")) + " "
                        + mContext.getString(IdHelper.getString(mContext, "jmui_morning")) + date;
            } else if (hour < 18) {
                return mContext.getString(IdHelper.getString(mContext, "jmui_yesterday")) + " "
                        + mContext.getString(IdHelper.getString(mContext, "jmui_afternoon")) + date;
            } else {
                return mContext.getString(IdHelper.getString(mContext, "jmui_yesterday")) + " "
                        + mContext.getString(IdHelper.getString(mContext, "jmui_night")) + date;
            }
        } else if (date1.getYear() == date2.getYear()) {
            String combine = date2.getMonth() + 1 + mContext.getString(IdHelper.getString(mContext, "jmui_month"))
                    + date2.getDate() + mContext.getString(IdHelper.getString(mContext, "jmui_day")) + " ";
            if (hour < 6) {
                return  combine + mContext.getString(IdHelper.getString(mContext, "jmui_before_dawn")) + date;
            } else if (hour < 12) {
                return combine + mContext.getString(IdHelper.getString(mContext, "jmui_morning")) + date;
            } else if (hour < 18) {
                return combine + mContext.getString(IdHelper.getString(mContext, "jmui_afternoon")) + date;
            }
            else {
                return combine + mContext.getString(IdHelper.getString(mContext, "jmui_night")) + date;
            }
        } else if (hour < 6) {
            return date3 + " " + mContext.getString(IdHelper.getString(mContext, "jmui_before_dawn")) + date;
        } else if (hour < 12) {
            return date3 + " " + mContext.getString(IdHelper.getString(mContext, "jmui_morning")) + date;
        }
        else if (hour < 18) {
            return date3 + " " + mContext.getString(IdHelper.getString(mContext, "jmui_afternoon")) + date;
        } else {
            return date3 + " " + mContext.getString(IdHelper.getString(mContext, "jmui_night")) + date;
        }
    }
}
