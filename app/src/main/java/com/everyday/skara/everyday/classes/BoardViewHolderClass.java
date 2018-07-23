package com.everyday.skara.everyday.classes;

import com.everyday.skara.everyday.BoardMembersActivity;
import com.everyday.skara.everyday.MainActivity;
import com.everyday.skara.everyday.pojo.BoardMembersPOJO;

import java.util.ArrayList;

public class BoardViewHolderClass {
    public int position;
    public MainActivity.BoardsAdapter boardsAdapter;
    public BoardMembersActivity.MembersAdapter membersAdapter;
    public ArrayList<BoardMembersPOJO> boardMembersPOJOArrayList;

    public BoardViewHolderClass() {
        boardMembersPOJOArrayList = new ArrayList<>();
    }

    public BoardViewHolderClass(int position, MainActivity.BoardsAdapter boardsAdapter, BoardMembersActivity.MembersAdapter membersAdapter, ArrayList<BoardMembersPOJO> boardMembersPOJOArrayList) {
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

    public MainActivity.BoardsAdapter getBoardsAdapter() {
        return boardsAdapter;
    }

    public void setBoardsAdapter(MainActivity.BoardsAdapter boardsAdapter) {
        this.boardsAdapter = boardsAdapter;
    }

    public BoardMembersActivity.MembersAdapter getMembersAdapter() {
        return membersAdapter;
    }

    public void setMembersAdapter(BoardMembersActivity.MembersAdapter membersAdapter) {
        this.membersAdapter = membersAdapter;
    }

    public ArrayList<BoardMembersPOJO> getBoardMembersPOJOArrayList() {
        return boardMembersPOJOArrayList;
    }

    public void setBoardMembersPOJOArrayList(ArrayList<BoardMembersPOJO> boardMembersPOJOArrayList) {
        this.boardMembersPOJOArrayList = boardMembersPOJOArrayList;
    }
}