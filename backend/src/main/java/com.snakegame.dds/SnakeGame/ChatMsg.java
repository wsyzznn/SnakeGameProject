package com.snakegame.dds.SnakeGame;


public class ChatMsg{
    public String player_id = "";// @ID(0)
    public String nickname = "";// @ID(1)
    public String content = "";// @ID(2)
    public int timestamp = 0;// @ID(3)

    public ChatMsg(){

    }

    public ChatMsg(ChatMsg other){
        this();
        copy(other);
    }

    public Object copy(Object src) {
        ChatMsg typedSrc = (ChatMsg)src;
        this.player_id =  typedSrc.player_id;
        this.nickname =  typedSrc.nickname;
        this.content =  typedSrc.content;
        this.timestamp =  typedSrc.timestamp;
        return this;
    }
}