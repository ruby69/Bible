package com.appskimo.app.bible.event;

import com.appskimo.app.bible.domain.Content;

import lombok.Getter;

public class OnCheck {
    public enum From {
        LIST, LIKE;

        public boolean isList() {
            return this == LIST;
        }

        public boolean isLike() {
            return this == LIKE;
        }
    }

    private From from;
    @Getter private Content content;

    public OnCheck(Content content, From from) {
        this.content = content;
        this.from = from;
    }

    public From from() {
        return from;
    }
}
