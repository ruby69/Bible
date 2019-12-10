package com.appskimo.app.bible.event;

import lombok.Data;

@Data
public class SelectEdition {
    private boolean withMode;

    public SelectEdition() {}

    public SelectEdition(boolean withMode) {
        this.withMode = withMode;
    }
}
