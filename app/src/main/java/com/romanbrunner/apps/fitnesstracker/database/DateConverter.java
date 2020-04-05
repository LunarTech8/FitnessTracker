package com.romanbrunner.apps.fitnesstracker.database;

import androidx.room.TypeConverter;
import java.util.Date;


@SuppressWarnings("WeakerAccess")
class DateConverter
{
    @TypeConverter
    public static Date toDate(Long timestamp)
    {
        return timestamp == null ? null : new Date(timestamp);
    }

    @TypeConverter
    public static Long toTimestamp(Date date)
    {
        return date == null ? null : date.getTime();
    }
}
