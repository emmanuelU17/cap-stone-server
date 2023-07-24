package com.emmanuel.sarabrandserver.util;

import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

@Service
public class CustomUtil {
    public final String logoutURL = "/api/v1/auth/logout";

    /**
     * Converts date to UTC Date
     * @param date of type java.util.date
     * @return Date of type java.util.date
     * */
    public Optional<Date> toUTC(Date date) {
        if (date == null) {
            return Optional.empty();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        return Optional.of(calendar.getTime());
    }

    /** Deletes cookie */
    public void expireCookie(Cookie cookie) {
        cookie.setValue("");
        cookie.setPath("/");
        cookie.setMaxAge(0);
    }

}
