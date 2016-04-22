/*
 * Copyright 2016 Elmar Rhex Gomez.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.elmargomez.dominohttp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateGenerator {

    private static final String TIME_FORMAT = "HH:mm:ss-yyyy-MM-dd";
    private static final String RFC_1123_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";
    private static DateGenerator generator = new DateGenerator();
    private SimpleDateFormat format;
    private SimpleDateFormat format2;

    private DateGenerator() {
        format = new SimpleDateFormat(TIME_FORMAT);
        format2 = new SimpleDateFormat(RFC_1123_FORMAT);
    }

    public static DateGenerator getGenerator() {
        return generator;
    }

    public String getTime() {
        return format.format(new Date());
    }

    public long getEpoch(String rfc_date) {
        try {
            format2.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date d = format2.parse(rfc_date);
            format2.setTimeZone(TimeZone.getDefault());
            String sDate = format2.format(d);
            return format2.parse(sDate).getTime();
        } catch (ParseException e) {
            return 0;
        }
    }
}
