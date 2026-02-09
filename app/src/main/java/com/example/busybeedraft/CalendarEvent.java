package com.example.busybeedraft;

import java.io.Serializable;

public class CalendarEvent implements Serializable {
    public String title, startDate, endDate, time, course, color;

    // This constructor now has all 6 required fields
    public CalendarEvent(String title, String startDate, String endDate, String time, String course, String color) {
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.time = time;
        this.course = course;
        this.color = color;
    }
}