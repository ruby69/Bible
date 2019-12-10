package com.appskimo.app.bible.event;

import lombok.Getter;

public class Like {
    public enum From {
        LIST, LIKE, VERSE;

        public boolean isList() {
            return this == LIST;
        }

        public boolean isLike() {
            return this == LIKE;
        }

        public boolean isVerse() {
            return this == VERSE;
        }
    }

    private From from;
    @Getter private Integer contentUid;
    @Getter private boolean liked;

    public Like(From from, Integer contentUid, boolean liked) {
        this.from = from;
        this.contentUid = contentUid;
        this.liked = liked;
    }

    public From from() {
        return from;
    }
}
