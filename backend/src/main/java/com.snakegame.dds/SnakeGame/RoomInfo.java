package com.snakegame.dds.SnakeGame;


public class RoomInfo{
    public String room_id = "";// @ID(0)
    public com.zrdds.infrastructure.StringSeq player_ids = new com.zrdds.infrastructure.StringSeq();// @ID(1)

    public RoomInfo(){

        this.player_ids.maximum(255);
    }

    public RoomInfo(RoomInfo other){
        this();
        copy(other);
    }

    public Object copy(Object src) {
        RoomInfo typedSrc = (RoomInfo)src;
        this.room_id =  typedSrc.room_id;
        this.player_ids.copy(typedSrc.player_ids);
        return this;
    }
}