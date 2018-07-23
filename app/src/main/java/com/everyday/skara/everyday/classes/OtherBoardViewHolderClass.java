package com.everyday.skara.everyday.classes;

import com.everyday.skara.everyday.OtherBoardsActivity;
import com.everyday.skara.everyday.pojo.BoardMembersPOJO;

import java.util.ArrayList;

public class OtherBoardViewHolderClass {
    public int position;
    public OtherBoardsActivity.BoardsAdapter boardsAdapter;
    public OtherBoardsActivity.MembersViewAdapter membersAdapter;
    public ArrayList<BoardMembersPOJO> boardMembersPOJOArrayList;

    public OtherBoardViewHolderClass() {
        boardMembersPOJOArrayList = new ArrayList<>();
    }

    public OtherBoardViewHolderClass(int position, OtherBoardsActivity.BoardsAdapter boardsAdapter, OtherBoardsActivity.MembersViewAdapter membersAdapter, ArrayList<BoardMembersPOJO> boardMembersPOJOArrayList) {
        this.position = position;
        this.boardsAdapter = boardsAdapter;
        this.membersAdapter = membersAdapter;
        this.boardMembersPOJOArrayList = boardMembersPOJOArrayList;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public OtherBoardsActivity.BoardsAdapter getBoardsAdapter() {
        return boardsAdapter;
    }

    public void setBoardsAdapter(OtherBoardsActivity.BoardsAdapter boardsAdapter) {
        this.boardsAdapter = boardsAdapter;
    }

    public OtherBoardsActivity.MembersViewAdapter getMembersAdapter() {
        return membersAdapter;
    }

    public void setMembersAdapter(OtherBoardsActivity.MembersViewAdapter membersAdapter) {
        this.membersAdapter = membersAdapter;
    }

    public ArrayList<BoardMembersPOJO> getBoardMembersPOJOArrayList() {
        return boardMembersPOJOArrayList;
    }

    public void setBoardMembersPOJOArrayList(ArrayList<BoardMembersPOJO> boardMembersPOJOArrayList) {
        this.boardMembersPOJOArrayList = boardMembersPOJOArrayList;
    }
}