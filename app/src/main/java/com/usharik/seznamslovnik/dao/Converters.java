package com.usharik.seznamslovnik.dao;

import androidx.room.TypeConverter;

import java.util.Date;

/**
 * Converters for complex to persist complex data types in sqlite
 */

class Converters {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
