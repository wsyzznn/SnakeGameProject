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
        int initLength = 3; // 初始蛇长度

        String[] directions = {"UP", "DOWN", "LEFT", "RIGHT"};

        for (PlayerInfo p : players) {
            int x = 0, y = 0;
            String key;
            String dir = directions[rand.nextInt(directions.length)]; // 随机方向

            // 循环直到生成一个不重复且预留空间的位置
            do {
                switch (dir) {
                    case "UP":
                        x = rand.nextInt(width);
                        y = rand.nextInt(height - initLength);  // 0 ~ height - initLength -1
                        break;
                    case "DOWN":
                        x = rand.nextInt(width);
                        y = rand.nextInt(height - initLength) + initLength - 1; // initLength-1 ~ height-1
                        break;
                    case "LEFT":
                        x = rand.nextInt(width - initLength);
                        y = rand.nextInt(height);
                        break;
                    case "RIGHT":
                        x = rand.nextInt(width - initLength) + initLength - 1;
                        y = rand.nextInt(height);
                        break;
                }
                key = x + "," + y;
            } while (occupied.contains(key));

            occupied.add(key);  // 标记该位置已占用

            Snake snake = new Snake(p.player_id, p.nickname, p.color, new Point(x, y), dir, initLength);
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
            broadcastNewItem(item);
        }
        System.out.println("[GameService] 生成了 " + count + " 个食物");
    }

    private void broadcastNewItem(Item item) {
        System.out.println("[DDS] 广播新生成 Item: id=" + item.item_id +
                " type=" + item.item_type + " x=" + item.x + " y=" + item.y);

        // ⚡ 调用 DDS 发布接口
        // ddsPublisher.publishItem(item);
    }

    private void broadcastGetFood(int playerId, Item item) {
        GetFood gf = new GetFood();
        gf.player_id = playerId;
        gf.item_id = item.item_id;
        gf.item_type = item.item_type;
        gf.x = item.x;
        gf.y = item.y;

        System.out.println("[DDS] 广播 GetFood: player=" + playerId +
                " item=" + item.item_id + " type=" + item.item_type +
                " x=" + item.x + " y=" + item.y);

        // ⚡ 调用 DDS 发布接口
        // ddsPublisher.publishGetFood(gf);
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
                    int scoreChange = 0;

                    if (item.item_type == ItemType.APPLE) {
                        growAmount = 1;
                        scoreChange = 10;
                    } else if (item.item_type == ItemType.GOOD_FOOD) {
                        growAmount = 2;
                        scoreChange = 20;
                    } else if (item.item_type == ItemType.BAD_FOOD) {
                        growAmount = -1;
                        scoreChange = -5;
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

                    // 更新积分
                    snake.score += scoreChange;
                    if (snake.score < 0) snake.score = 0; // 避免负分

                    // ⚡ 只广播这个玩家获取的食物
                    broadcastGetFood(snake.playerId, item);

                    // 移除已吃的道具
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
                s.score = 0; // 死亡清空积分
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

        col.player_ids.maximum(deadPlayers.size());
        col.collisions.maximum(deadPlayers.size());

        for (int i = 0; i < deadPlayers.size(); i++) {
            col.player_ids.append(deadPlayers.get(i));
            col.collisions.append(true);
        }

        System.out.println("[DDS] 广播 Collision: 死亡人数=" + deadPlayers.size());

        // ⚡ TODO: 调用 DDS 接口发送 Collision
        // ddsPublisher.publishCollision(col);
    }

    public GameMap getMap() {
        return map;
    }
}
