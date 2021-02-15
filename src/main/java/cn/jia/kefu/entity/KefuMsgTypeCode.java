package cn.jia.kefu.entity;

import lombok.Getter;

public enum KefuMsgTypeCode {
    VOTE("vote", "每日一题");

    @Getter
    private final String code;
    @Getter
    private final String name;

    KefuMsgTypeCode(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
