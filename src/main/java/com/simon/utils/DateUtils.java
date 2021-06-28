package com.simon.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 〈功能概述〉<br>
 *
 * @className: DateUtils
 * @package: com.lrm.hospital.utils
 * @author: mamingcong
 * @date: 2020/12/7 9:49
 */
public class DateUtils {

    public static List<Date> getNext7Days() {
        List<Date> dateList = new ArrayList<>();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        java.util.Calendar cal = java.util.Calendar.getInstance();
        Date time = cal.getTime();
        dateList.add(time);
        for (int i = 0; i < 6; i++) {
            cal.add(java.util.Calendar.DATE, 1);
            dateList.add(cal.getTime());
        }
        return dateList;
    }

    public static String dateToWeek(Date dateTime){
        String[] weekDays = {"周日","周一","周二","周三","周四","周五","周六"};
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateTime);
        //指示一个星期中的某天
        int w = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if(w < 0){
            w = 0;
        }
        return  weekDays[w];
    }
}
