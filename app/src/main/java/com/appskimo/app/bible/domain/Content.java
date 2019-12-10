package com.appskimo.app.bible.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString(of = {"contentUid", "chapter", "verse"})
@DatabaseTable(tableName = "Content")
public class Content implements Serializable {
    private static final long serialVersionUID = 5973737262353851620L;

    public static final String FIELD_contentUid = "contentUid";
    public static final String FIELD_chapter = "chapter";
    public static final String FIELD_verse = "verse";
    public static final String FIELD_liked = "liked";

    @DatabaseField(columnName = FIELD_contentUid, generatedId = true) private Integer contentUid;
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = Bible.FIELD_bibleUid) private Bible bible;
    @DatabaseField(index = true) private int chapter;
    @DatabaseField(index = true) private int verse;
    @DatabaseField private String ntb;
    @DatabaseField private String cte;
    @DatabaseField private String krv;
    @DatabaseField private String hrv;
    @DatabaseField private String tkv;
    @DatabaseField private String nab;
    @DatabaseField private String kjv;
    @DatabaseField private String niv;
    @DatabaseField private String vul;
    @DatabaseField private String hebTkv;
    @DatabaseField private boolean liked;
    private boolean checked;

    private static final String FORMAT_KR = "%s %s장 %s절";
    private static final String FORMAT_EN = "%s %s:%s";

    public String getInfo(Edition edition) {
        return edition.isKo() ? String.format(FORMAT_KR, bible.getTitle(edition), chapter, verse) : String.format(FORMAT_EN, bible.getTitle(edition), chapter, verse);
    }

    public String getSimpleInfo(Edition edition) {
        return edition.isKo() ? String.format(FORMAT_EN, bible.getAbb(edition), chapter, verse) : String.format(FORMAT_EN, bible.getAbb(edition), chapter, verse);
    }

    public String getContentsForShare(Edition edition) {
        return getContents(edition) + " - " + getInfo(edition);
    }

    public String getContents(Edition edition) {
        if(edition == Edition.NTB) {
            return ntb != null && !ntb.isEmpty() && !"null".equals(ntb) ? ntb : null;
        } else if(edition == Edition.HRV) {
            return hrv != null && !hrv.isEmpty() && !"null".equals(hrv) ? hrv : null;
        } else if(edition == Edition.CTE) {
            return cte != null && !cte.isEmpty() && !"null".equals(cte) ? cte : null;
        } else if(edition == Edition.TKV) {
            return tkv != null && !tkv.isEmpty() && !"null".equals(tkv) ? tkv : null;
        } else if(edition == Edition.NAB) {
            return nab != null && !nab.isEmpty() && !"null".equals(nab) ? nab : null;
        } else if(edition == Edition.KJV) {
            return kjv != null && !kjv.isEmpty() && !"null".equals(kjv) ? kjv : null;
        } else if(edition == Edition.VUL) {
            return vul != null && !vul.isEmpty() && !"null".equals(vul) ? vul : null;
        } else if(edition == Edition.NIV) {
            return niv != null && !niv.isEmpty() && !"null".equals(niv) ? niv : null;
        } else if(edition == Edition.HEBTKV) {
            return hebTkv != null && !hebTkv.isEmpty() && !"null".equals(hebTkv) ? hebTkv : null;
        } else {
            return krv != null && !krv.isEmpty() && !"null".equals(krv) ? krv : null;
        }
    }
}
