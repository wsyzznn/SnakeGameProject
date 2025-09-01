package com.snakegame.dds.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.snakegame.dds.SnakeGame.Item; // 使用 DDS 生成的 Item 类

public class GameMap {
    public int width;
    public int height;
    public Map<Integer, Snake> snakes; // playerId -> Snake
    public Map<Integer, Item> items;   // itemId -> Item

    public GameMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.snakes = new ConcurrentHashMap<>();
        this.items = new ConcurrentHashMap<>();
    }

    public void addSnake(Snake snake) {
        snakes.put(snake.playerId, snake);
    }

    public void removeSnake(int playerId) {
        snakes.remove(playerId);
    }

    public void addItem(Item item) {
        items.put(item.item_id, item); // 注意：字段来自 DDS 生成的类
    }

    public void removeItem(int itemId) {
        items.remove(itemId);
    }
}
