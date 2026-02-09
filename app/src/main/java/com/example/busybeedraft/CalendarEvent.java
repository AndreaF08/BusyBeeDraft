package com.example.busybeedraft;

import java.io.Serializable;

public class CalendarEvent implements Serializable {
    public String time, title, course, date;

    public CalendarEvent(String time, String title, String course, String date) {
        this.time = time;
        this.title = title;
        this.course = course;
        this.date = date;
    }
}