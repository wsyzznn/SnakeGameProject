package com.snakegame.dds.SnakeGame;


public class Collision{
    public String player_id = "";// @ID(0)
    public String collision_type = "";// @ID(1)
    public String target_id = "";// @ID(2)

    public Collision(){

    }

    public Collision(Collision other){
        this();
        copy(other);
    }

    public Object copy(Object src) {
        Collision typedSrc = (Collision)src;
        this.player_id =  typedSrc.player_id;
        this.collision_type =  typedSrc.collision_type;
        this.target_id =  typedSrc.target_id;
        return this;
    }
}