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
    private String mReason;
    private String mBounds;
    private String mSource;
    private Date mDate;

    public News(String date, String source, String reason, String bounds) {
        mReason = reason;
        mBounds = bounds;
        mSource = source;
        try {
            mDate = DateFormat.getDateInstance().parse(date);
        } catch (ParseException e) {
            mDate = new Date();
        }
    }

    public String getReason() {return mReason;}
    public String getBounds() {return mBounds;}
    public String getSource() {return mSource;}
    public Date getDate() {return mDate;}
}
