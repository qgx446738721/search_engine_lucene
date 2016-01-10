package org.voiddog.lucene.model;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * 搜索消息
 * Created by qgx44 on 2016/1/9.
 */
public class SearchMessage {
    private int pageId = 1;

    private int startPageId = 1;

    private int endPageId = 1;

    @NotEmpty(message = "search string is require")
    private String searchString;

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public int getPageId() {
        return pageId;
    }

    public void setPageId(int pageId) {
        this.pageId = pageId;
    }

    public int getStartPageId() {
        return startPageId;
    }

    public void setStartPageId(int startPageId) {
        this.startPageId = startPageId;
    }

    public int getEndPageId() {
        return endPageId;
    }

    public void setEndPageId(int endPageId) {
        this.endPageId = endPageId;
    }
}
