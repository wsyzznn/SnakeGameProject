package com.snakegame.dds.SnakeGame;


public class Item{
    public int item_id = 0;// @ID(0)
    public String item_type = "";// @ID(1)
    public int x = 0;// @ID(2)
    public int y = 0;// @ID(3)

    public Item(){

    }

    public Item(Item other){
        this();
        copy(other);
    }

    public Object copy(Object src) {
        Item typedSrc = (Item)src;
        this.item_id =  typedSrc.item_id;
        this.item_type =  typedSrc.item_type;
        this.x =  typedSrc.x;
        this.y =  typedSrc.y;
        return this;
    }
}