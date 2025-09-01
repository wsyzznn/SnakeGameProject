package com.snakegame.dds.service;

import com.snakegame.dds.model.*;
import com.snakegame.dds.SnakeGame.*; // DDS 自动生成的类（PlayerInfo, GameState, Item, Leaderboard 等）
import com.zrdds.infrastructure.BooleanSeq;
import com.zrdds.infrastructure.LongSeq;

import java.util.*;

public class GameService {
    public GameMap map;
    private ItemService itemService;
    private CollisionService collisionService;

    // 初始化游戏
    // ⚡ 对接 DDS: PlayerInfo, GameSetting
    public void initGame(List<PlayerInfo> players, int width, int height) {
        this.map = new GameMap(width, height);
        this.itemService = new ItemService();
        this.collisionService = new CollisionService();

        // 用于记录已占用的位置
        Set<String> occupied = new HashSet<>();

        Random rand = new Random();
        for (PlayerInfo p : players) {
            int x, y;
            String key;
            // 循环直到生成一个不重复的位置
            do {
                x = rand.nextInt(width);
                y = rand.nextInt(height);
                key = x + "," + y;
            } while (occupied.contains(key));

            occupied.add(key);  // 标记该位置已占用

            String dir = "RIGHT"; // 初始方向固定为向右
            Snake snake = new Snake(p.player_id, p.nickname, p.color, new Point(x, y), dir, 3);
            map.addSnake(snake);
        }

        // 初始生成一些食物
        for (int i = 0; i < 20; i++) {
            Item item = itemService.spawnRandomItem(width, height, map);
            map.addItem(item);
        }

        System.out.println("[GameService] 游戏初始化完成: 玩家=" + players.size());
    }


    // 游戏循环
    public void gameTick(Map<Integer, String> playerInputs, boolean spawnFoodThisTick) {
        moveSnakes(playerInputs);

        handleItemConsumption();

        List<Integer> wallDeaths = collisionService.checkWallCollisions(map);
        List<Integer> snakeDeaths = collisionService.checkSnakeCollisions(map);

        Set<Integer> allDead = new HashSet<>();
        allDead.addAll(wallDeaths);
        allDead.addAll(snakeDeaths);

        if (!allDead.isEmpty()) {
            handleDeaths(new ArrayList<>(allDead));
        }

        if (spawnFoodThisTick) {
            spawnFood(3);  // 比如每次 3 个
        }
    }

    // 游戏结束
    public void endGame() {
        System.out.println("[GameService] 游戏结束，排行榜已生成");
    }

    // =============== 私有方法 ===============

    private void spawnFood(int count) {
        for (int i = 0; i < count; i++) {
            Item item = itemService.spawnRandomItem(map.width, map.height, map);
            map.addItem(item);
        }
        System.out.println("[GameService] 生成了 " + count + " 个食物");
    }

    private void moveSnakes(Map<Integer, String> inputs) {
        for (Snake snake : map.snakes.values()) {
            if (!snake.alive) continue;

            String newDir = inputs.get(snake.playerId);
            if (newDir != null && !isOpposite(snake.direction, newDir)) {
                snake.direction = newDir;
            }

            Point head = snake.getHead();
            Point newHead = switch (snake.direction) {
                case "UP" -> new Point(head.x, head.y - 1);
                case "DOWN" -> new Point(head.x, head.y + 1);
                case "LEFT" -> new Point(head.x - 1, head.y);
                case "RIGHT" -> new Point(head.x + 1, head.y);
                default -> throw new IllegalStateException("Unexpected value: " + snake.direction);
            };

            snake.move(newHead, false);
        }
    }

    private boolean isOpposite(String d1, String d2) {
        return (d1.equals("UP") && d2.equals("DOWN"))
                || (d1.equals("DOWN") && d2.equals("UP"))
                || (d1.equals("LEFT") && d2.equals("RIGHT"))
                || (d1.equals("RIGHT") && d2.equals("LEFT"));
    }

    private void handleItemConsumption() {
        Iterator<Item> it = map.items.values().iterator();
        while (it.hasNext()) {
            Item item = it.next();
            for (Snake snake : map.snakes.values()) {
                if (!snake.alive) continue;
                if (snake.getHead().x == item.x && snake.getHead().y == item.y) {

                    int growAmount = 0;
                    if (item.item_type == ItemType.APPLE) {
                        growAmount = 1;
                    } else if (item.item_type == ItemType.GOOD_FOOD) {
                        growAmount = 2;
                    } else if (item.item_type == ItemType.BAD_FOOD) {
                        growAmount = -1;
                    }

                    if (growAmount > 0) {
                        // 在蛇尾延长
                        for (int i = 0; i < growAmount; i++) {
                            Point tail = snake.body.getLast();
                            if (snake.body.size() >= 2) {
                                Point preTail = ((LinkedList<Point>) snake.body).get(snake.body.size() - 2);
                                int dx = tail.x - preTail.x;
                                int dy = tail.y - preTail.y;
                                Point newTail = new Point(tail.x + dx, tail.y + dy);
                                snake.body.addLast(newTail);
                            } else {
                                // 特殊情况：蛇长度为1，随便往后加一个（比如往下）
                                snake.body.addLast(new Point(tail.x, tail.y + 1));
                            }
                        }
                    } else if (growAmount < 0) {
                        // 缩短蛇身
                        for (int i = 0; i < -growAmount && snake.body.size() > 1; i++) {
                            snake.body.removeLast();
                        }
                    }
                    // 移除已吃的道具，并生成新道具
                    it.remove();
                    break; // 一个道具只能被一条蛇吃掉
                }
            }
        }
    }

    private void handleDeaths(List<Integer> deadPlayers) {
        for (int pid : deadPlayers) {
            Snake s = map.snakes.get(pid);
            if (s != null) {
                s.alive = false;
                System.out.println("[GameService] 玩家死亡: " + s.nickname);

                // 💡 蛇身掉落食物：每隔 3 格生成一个 APPLE
                int interval = 3;
                int counter = 0;
                for (Point bodyPart : s.body) {
                    if (counter % interval == 0) {
                        Item food = itemService.spawnItemAt(bodyPart.x, bodyPart.y, ItemType.APPLE);
                        map.addItem(food);
                    }
                    counter++;
                }
            }
        }

        // ⚡ 对接 DDS: Collision
        Collision col = new Collision();

        // 使用 DDS 序列类型
        col.player_ids = new LongSeq();
        col.collisions = new BooleanSeq();

        for (int i = 0; i < deadPlayers.size(); i++) {
            col.player_ids.set_at(i, deadPlayers.get(i));
            col.collisions.set_at(i, true);
        }

        System.out.println("[DDS] 广播 Collision: 死亡人数=" + deadPlayers.size());

        // ⚡ TODO: 调用 DDS 接口发送 Collision
        // ddsPublisher.publishCollision(col);
    }

    public GameMap getMap() {
        return map;
    }
}
