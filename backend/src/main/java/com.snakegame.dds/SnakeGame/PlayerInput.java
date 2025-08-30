package com.snakegame.dds.SnakeGame;


public class PlayerInput{
    public String player_id = "";// @ID(0)
    public String nickname = "";// @ID(1)
    public String password = "";// @ID(2)
    public String direction = "";// @ID(3)
    public int timestamp = 0;// @ID(4)

    public PlayerInput(){

    }

    public PlayerInput(PlayerInput other){
        this();
        copy(other);
    }

    public Object copy(Object src) {
        PlayerInput typedSrc = (PlayerInput)src;
        this.player_id =  typedSrc.player_id;
        this.nickname =  typedSrc.nickname;
        this.password =  typedSrc.password;
        this.direction =  typedSrc.direction;
        this.timestamp =  typedSrc.timestamp;
        return this;
    }
}