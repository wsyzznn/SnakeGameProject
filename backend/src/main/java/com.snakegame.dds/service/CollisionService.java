package com.snakegame.dds.service;

import com.snakegame.dds.model.*;
import java.util.*;

public class CollisionService {
    // 检查墙体碰撞
    public List<Integer> checkWallCollisions(GameMap map) {
        List<Integer> dead = new ArrayList<>();
        for (Snake s : map.snakes.values()) {
            if (!s.alive) continue;
            Point head = s.getHead();
            if (head.x < 0 || head.y < 0 || head.x >= map.width || head.y >= map.height) {
                dead.add(s.playerId);
            }
        }
        return dead;
    }

    // 检查蛇与蛇的碰撞（不算撞到自己）
    public List<Integer> checkSnakeCollisions(GameMap map) {
        List<Integer> dead = new ArrayList<>();
        Map<String, List<Integer>> headPos = new HashMap<>();

        for (Snake s : map.snakes.values()) {
            if (!s.alive) continue;
            Point head = s.getHead();
            String key = head.x + "," + head.y;
            headPos.computeIfAbsent(key, k -> new ArrayList<>()).add(s.playerId);
        }

        // 头对头冲突
        for (List<Integer> ids : headPos.values()) {
            if (ids.size() > 1) dead.addAll(ids);
        }

        // 头撞别人身体
        for (Snake s : map.snakes.values()) {
            if (!s.alive) continue;
            Point head = s.getHead();
            for (Snake other : map.snakes.values()) {
                if (other.playerId == s.playerId || !other.alive) continue;
                if (other.body.contains(head)) {
                    dead.add(s.playerId);
                    break;
                }
            }
        }

        return dead;
    }
}
