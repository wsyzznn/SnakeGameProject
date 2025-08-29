package com.snakegame.dds.SnakeGame;


public class PlayerInput{
    public String player_id = "";// @ID(0)
    public String direction = "";// @ID(1)
    public int timestamp = 0;// @ID(2)

    public PlayerInput(){

    }

    public PlayerInput(PlayerInput other){
        this();
        copy(other);
    }

    public Object copy(Object src) {
        PlayerInput typedSrc = (PlayerInput)src;
        this.player_id =  typedSrc.player_id;
        this.direction =  typedSrc.direction;
        this.timestamp =  typedSrc.timestamp;
        return this;
    }
}