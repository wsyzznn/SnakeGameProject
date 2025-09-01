package com.snakegame.dds.model;

import java.util.Deque;
import java.util.LinkedList;

public class Snake {
    public int playerId;
    public String nickname;
    public String color;
    public Deque<Point> body;   // 头在队首
    public String direction;    // "UP","DOWN","LEFT","RIGHT"
    public int score;
    public boolean alive;

    public Snake(int playerId, String nickname, String color, Point initHead, String direction, int initLength) {
        this.playerId = playerId;
        this.nickname = nickname;
        this.color = color;
        this.direction = direction;
        this.score = 0;
        this.alive = true;
        this.body = new LinkedList<>();

        for (int i = 0; i < initLength; i++) {
            int dx = 0, dy = 0;
            switch (direction) {
                case "UP": dy = i; break;
                case "DOWN": dy = -i; break;
                case "LEFT": dx = i; break;
                case "RIGHT": dx = -i; break;
            }
            body.addLast(new Point(initHead.x + dx, initHead.y + dy));
        }
    }

    public Point getHead() {
        return body.peekFirst();
    }

    public void move(Point newHead, boolean grow) {
        body.addFirst(newHead);
        if (!grow) {
            body.removeLast();
        }
    }
}
