package com.appskimo.app.bible.domain;

import lombok.Getter;

@Getter
public enum Edition {
    KRV("개역개정"),
    NTB("새번역"),
    HRV("개역한글"),
    CTE("공동번역개정"),
    TKV("현대어"),
    NAB("NAB"),
    KJV("KJV"),
    VUL("VUL"),
    NIV("NIV"),
    HEBTKV("히브리어");

    private String title;

    Edition(String title) {
        this.title = title;
    }

    public boolean isKo() {
        return this == KRV || this == NTB || this == HRV || this == CTE || this == TKV;
    }
}
