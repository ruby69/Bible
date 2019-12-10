package com.appskimo.app.bible.domain;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lombok.Data;

@Data
public class More implements Serializable {
    private static final long serialVersionUID = 1045585771904402263L;

    private Long scale = 30L;
    private List<Content> content;
    private Map<String, Object> p = new TreeMap<String, Object>();
    private boolean hasMore;

    public Integer getLastId() {
        if(hasContents()) {
            return content.get(content.size() - 1).getContentUid();
        }
        return null;
    }

    private boolean hasContents() {
        return content != null && !content.isEmpty();
    }
}
