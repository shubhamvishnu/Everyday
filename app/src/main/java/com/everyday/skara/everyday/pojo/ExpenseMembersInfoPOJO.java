package com.everyday.skara.everyday.pojo;

public class ExpenseMembersInfoPOJO {
    BoardMembersPOJO userInfoPOJO;
    boolean hasPaid;

    public ExpenseMembersInfoPOJO() {
    }

    public ExpenseMembersInfoPOJO(BoardMembersPOJO userInfoPOJO, boolean hasPaid) {
        this.userInfoPOJO = userInfoPOJO;
        this.hasPaid = hasPaid;
    }

    public BoardMembersPOJO getUserInfoPOJO() {
        return userInfoPOJO;
    }

    public void setUserInfoPOJO(BoardMembersPOJO userInfoPOJO) {
        this.userInfoPOJO = userInfoPOJO;
    }

    public boolean isHasPaid() {
        return hasPaid;
    }

    public void setHasPaid(boolean hasPaid) {
        this.hasPaid = hasPaid;
    }
}
