package com.npcipav.activity_service_interaction;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by npcipav on 29.11.2015.
 *
 * Collect news. It's just dummy.
 */
public class NewsCollector {

    private List<News> mNewsList;

    public NewsCollector() {
        mNewsList = new LinkedList<News>();
    }

    public void refreshNews() {
        // TODO! Add news.
    }

    public List<News> getNewsList() {
        return mNewsList;
    }
}
