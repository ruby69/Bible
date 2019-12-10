package com.appskimo.app.bible.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.Collection;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@DatabaseTable(tableName = "Bible")
public class Bible implements Serializable {
    private static final long serialVersionUID = -3398162853497031793L;

    public static final String FIELD_bibleUid= "bibleUid";

    @DatabaseField(columnName = FIELD_bibleUid, id = true, generatedId = false) private Integer bibleUid;
    @DatabaseField private boolean old;

    @DatabaseField private String ntb;
    @DatabaseField private String ntbAbb;
    @DatabaseField private int ntbChapter;
    @DatabaseField private String cte;
    @DatabaseField private String cteAbb;
    @DatabaseField private int cteChapter;
    @DatabaseField private String koTitle;
    @DatabaseField private String koAbb;
    @DatabaseField private int koChapter;
    @DatabaseField private String vul;
    @DatabaseField private String vulAbb;
    @DatabaseField private int vulChapter;
    @DatabaseField private String nab;
    @DatabaseField private String nabAbb;
    @DatabaseField private int nabChapter;
    @DatabaseField private String kjv;
    @DatabaseField private String kjvAbb;
    @DatabaseField private int kjvChapter;
    @DatabaseField private String niv;
    @DatabaseField private String nivAbb;
    @DatabaseField private int nivChapter;
    @DatabaseField private String hebTkv;
    @DatabaseField private String hebTkvAbb;
    @DatabaseField private int hebTkvChapter;

    @ForeignCollectionField private Collection<Content> contents;

    public String getTitle(Edition edition) {
        if(edition == Edition.NTB) {
            return ntb;
        } else if(edition == Edition.CTE) {
            return cte;
        } else if(edition == Edition.NAB) {
            return nab;
        } else if(edition == Edition.KJV) {
            return kjv;
        } else if(edition == Edition.VUL) {
            return vul;
        } else if(edition == Edition.NIV) {
            return niv;
        } else if(edition == Edition.HEBTKV) {
            return hebTkv;
        } else {
            return koTitle;
        }
    }

    public String getAbb(Edition edition) {
        if(edition == Edition.NTB) {
            return ntbAbb;
        } else if(edition == Edition.CTE) {
            return cteAbb;
        } else if(edition == Edition.NAB) {
            return nabAbb;
        } else if(edition == Edition.KJV) {
            return kjvAbb;
        } else if(edition == Edition.VUL) {
            return vulAbb;
        } else if(edition == Edition.NIV) {
            return nivAbb;
        } else if(edition == Edition.HEBTKV) {
            return hebTkvAbb;
        } else {
            return koAbb;
        }
    }

    public int getChapter(Edition edition) {
        if(edition == Edition.NTB) {
            return ntbChapter;
        } else if(edition == Edition.CTE) {
            return cteChapter;
        } else if(edition == Edition.NAB) {
            return nabChapter;
        } else if(edition == Edition.KJV) {
            return kjvChapter;
        } else if(edition == Edition.VUL) {
            return vulChapter;
        } else if(edition == Edition.NIV) {
            return nivChapter;
        } else if(edition == Edition.HEBTKV) {
            return hebTkvChapter;
        } else {
            return koChapter;
        }
    }
}
