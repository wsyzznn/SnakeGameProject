package com.snakegame.dds.service;

import com.snakegame.dds.model.GameMap;
import com.snakegame.dds.SnakeGame.*; // DDS Item, ItemType
import com.snakegame.dds.model.Point;
import com.snakegame.dds.model.Snake;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class ItemService {
    private AtomicInteger idGen = new AtomicInteger();

    // ⚡ 对接 DDS: Item, ItemType
    public Item spawnRandomItem(int width, int height, GameMap map) {
        Random rand = new Random();
        int id = idGen.incrementAndGet();

        int x, y;
        String key;

        // 记录已占用位置（蛇+现有食物）
        Set<String> occupied = new HashSet<>();

        // 已有蛇的位置
        for (Snake s : map.snakes.values()) {
            for (Point p : s.body) {
                occupied.add(p.x + "," + p.y);
            }
        }

        // 已有食物的位置
        for (Item it : map.items.values()) {
            occupied.add(it.x + "," + it.y);
        }

        // 随机生成不重复位置
        do {
            x = rand.nextInt(width);
            y = rand.nextInt(height);
            key = x + "," + y;
        } while (occupied.contains(key));

        // 根据概率生成道具类型
        int r = rand.nextInt(100); // 0~99
        ItemType type;
        String image;
        if (r < 60) {               // 60% 普通苹果
            type = ItemType.APPLE;
            image = "apple";
        } else if (r < 80) {        // 20% 增益食物
            type = ItemType.GOOD_FOOD;
            image = "good";
        } else {                     // 20% 减益食物
            type = ItemType.BAD_FOOD;
            image = "bad";
        }

        Item item = new Item();
        item.item_id = id;
        item.item_type = type;
        item.x = x;
        item.y = y;
        item.image_id = image;

        return item;
    }

    public Item spawnItemAt(int x, int y, ItemType type) {
        int id = idGen.incrementAndGet();

        Item item = new Item();
        item.item_id = id;
        item.item_type = type;
        item.x = x;
        item.y = y;

        if (type.equals(ItemType.APPLE)) {
            item.image_id = "apple";
        } else if (type.equals(ItemType.GOOD_FOOD)) {
            item.image_id = "good";
        } else if (type.equals(ItemType.BAD_FOOD)) {
            item.image_id = "bad";
        }

        return item;
    }

    public void removeItem(int itemId, GameMap map) {
        map.items.remove(itemId);
    }
}
