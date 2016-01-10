package org.voiddog.lucene.model;

/**
 * 搜索结果
 * Created by qgx44 on 2016/1/9.
 */
public class SearchResultItem {

    private String title;

    private String url;

    private String content;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
