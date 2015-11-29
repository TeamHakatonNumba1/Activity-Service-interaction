package com.npcipav.activity_service_interaction;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * Created by npcipav on 29.11.2015.
 *
 * Represents news in code.
 */
public class News {
    private String mText;
    private String mSource;
    private Date mDate;

    public News(String date, String source, String text) {
        mText = text;
        mSource = source;
        try {
            mDate = DateFormat.getDateInstance().parse(date);
        } catch (ParseException e) {
            mDate = new Date();
        }
    }

    public String getText() {return mText;}
    public String getSource() {return mSource;}
    public Date getDate() {return mDate;}
}
