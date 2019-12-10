package com.appskimo.app.bible.event;

import com.appskimo.app.bible.domain.Content;

import lombok.Data;

@Data
public class SelectContent {
    private Content content;

    public SelectContent(Content content) {
        this.content = content;
    }
}
