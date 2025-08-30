package com.snakegame.dds.SnakeGame;


public class Food{
    public int food_id = 0;// @ID(0)
    public int x = 0;// @ID(1)
    public int y = 0;// @ID(2)
    public String color = "";// @ID(3)

    public Food(){

    }

    public Food(Food other){
        this();
        copy(other);
    }

    public Object copy(Object src) {
        Food typedSrc = (Food)src;
        this.food_id =  typedSrc.food_id;
        this.x =  typedSrc.x;
        this.y =  typedSrc.y;
        this.color =  typedSrc.color;
        return this;
    }
}