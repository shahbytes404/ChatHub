package com.shabytes.backend.domain;

public enum MemberRole {
    OWNER,
    ADMIN,
    MEMBER;

    public boolean canManagerMembers() {
        return this == OWNER || this == ADMIN;
    }
}
