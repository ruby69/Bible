package com.appskimo.app.bible.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

import lombok.Data;

public class Backup {
    @Data
    @DatabaseTable(tableName = "ContentUpdate")
    public static class ContentUpdate implements Serializable {
        private static final long serialVersionUID = 994602066568500791L;

        @DatabaseField(columnName = Content.FIELD_contentUid, generatedId = true) private Integer contentUid;
        @DatabaseField private boolean liked;
    }
}
