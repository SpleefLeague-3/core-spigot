/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spleefleague.core.util.variable;

import com.spleefleague.coreapi.database.variable.DBVariable;

import java.time.Instant;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

/**
 * @author NickM13
 */
public class Day extends DBVariable<Integer> {

    private static final TimeZone timeZone = TimeZone.getTimeZone("PST");
    private int day;

    private static int dailyRandomDay = 0;
    private static int dailyRandom;

    public static int asDay(long millis) {
        int day = (int) (millis / 1000 / 60 / 60 / 24);
        return day;
    }

    public static TimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * @return
     */
    private static final long MILLIS_IN_DAY = 1000 * 60 * 60 * 24;

    public static float remainingHours() {
        Date date = Date.from(Instant.now());
        long offset = timeZone.getRawOffset() + (timeZone.inDaylightTime(date) ? 1000 * 60 * 60 : 0);
        int sec = (int) ((System.currentTimeMillis() + offset) / 1000);
        return 24 - ((float) sec / 60 / 60 % 24);
    }

    public static long remainingMillis() {
        Date date = Date.from(Instant.now());
        long offset = timeZone.getRawOffset() + (timeZone.inDaylightTime(date) ? 1000 * 60 * 60 : 0);
        long millis = MILLIS_IN_DAY - ((System.currentTimeMillis() + offset) % MILLIS_IN_DAY);
        return millis;
    }

    public static long getTomorrowMillis() {
        Date date = Date.from(Instant.now());
        long offset = timeZone.getRawOffset() + (timeZone.inDaylightTime(date) ? 1000 * 60 * 60 : 0);
        long millis = MILLIS_IN_DAY - ((System.currentTimeMillis() + offset) % MILLIS_IN_DAY);
        return System.currentTimeMillis() + millis;
    }

    public static int getCurrentDay() {
        Date date = Date.from(Instant.now());
        long offset = timeZone.getRawOffset() + (timeZone.inDaylightTime(date) ? 1000 * 60 * 60 : 0);
        int day = asDay(System.currentTimeMillis() + offset);
        return day;
    }

    public static int getNextWeekDay() {
        int day = getCurrentDay();
        return day % 7;
    }

    public int getDay() {
        return day;
    }

    public static int getDailyRandom() {
        if (dailyRandomDay != getCurrentDay()) {
            dailyRandomDay = getCurrentDay();
            dailyRandom = new Random(dailyRandomDay).nextInt();
        }
        return dailyRandom;
    }

    @Override
    public void load(Integer doc) {
        day = doc;
    }

    @Override
    public Integer save() {
        return day;
    }

    @Override
    public boolean equals(Object day) {
        return (day instanceof Day && this.day == ((Day) day).getDay());
    }

    @Override
    public int hashCode() {
        return day;
    }

}
