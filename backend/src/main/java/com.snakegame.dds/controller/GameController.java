package com.snakegame.dds.controller;

import com.snakegame.dds.model.Point;
import com.snakegame.dds.model.Snake;
import com.snakegame.dds.service.GameService;
import com.snakegame.dds.SnakeGame.*; // ⚡ 对接 DDS: PlayerInfo, PlayerMove, GameSetting
import com.snakegame.dds.transport.DdsBridge;
import com.zrdds.infrastructure.LongSeq;

import java.util.*;
import java.util.concurrent.*;

public class GameController {
    private GameService gameService;
    private Map<Integer, String> latestInputs;
    private ScheduledExecutorService scheduler;
    private final DdsBridge bridge;

    private volatile boolean ended = false;

    private int tickCounter = 0;
    private long gameStartTime;
    private long gameDurationMillis = 10 * 60 * 1000; // 游戏总时长，例如 10 分钟

    public GameController(int domainId) {
        this.gameService = new GameService();
        this.latestInputs = new java.util.concurrent.ConcurrentHashMap<>();
        this.bridge = new DdsBridge(domainId, this);
        bridge.init(); // 启动 DDS
        // 可选：把一个回调注入给 GameService
        this.gameService.setCallbacks(new GameService.Callbacks() {
            public void onFoodEaten(Snake s, Item item) { broadcastGetFood(s, item); }
            public void onItemSpawned(Item item) { bridge.publishNewItem(item); }
            public void onCollision(Collision c) { bridge.publishCollision(c); }
        });
    }

    public void onStartGame(List<PlayerInfo> players, GameSetting setting) {
        // 1. 初始化游戏逻辑
        gameService.initGame(players, setting.grid_size, setting.grid_size);

        // 记录游戏开始时间
        gameStartTime = System.currentTimeMillis();

        // 2. 广播初始 GameState
        broadcastAllGameStates();

        // 3. 广播初始道具
        broadcastAllItems();

        // 4. 广播初始排行榜
        broadcastLeaderboard();

        // 5. 启动主循环（定时器驱动）
        startGameLoop(setting);
    }

    // 定时驱动主循环
    public void startGameLoop(GameSetting setting) {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        long tickMillis = 1000 / setting.speed; // speed 表示每秒 tick 数

        scheduler.scheduleAtFixedRate(() -> {
            tickCounter++;

            // 控制食物生成时机（例如：每隔 20 tick 生成一次）
            boolean spawnFoodThisTick = (tickCounter % 20 == 0);

            onGameTick(spawnFoodThisTick);
            latestInputs.clear();
        }, 0, tickMillis, TimeUnit.MILLISECONDS);
    }


    // DDS 回调：收到玩家输入
    public void onPlayerMove(PlayerMove move) {
        if (move != null) {
            latestInputs.put(move.player_id, move.direction);
            //System.out.println(move.player_id + " " + move.direction);
        }
    }

    // 定时器驱动
    public void onGameTick(boolean spawnFoodThisTick) {
        gameService.gameTick(new HashMap<>(latestInputs), spawnFoodThisTick);
        broadcastAllGameStates();
        broadcastLeaderboard();

        // ===== 游戏结束判定 =====
        long elapsed = System.currentTimeMillis() - gameStartTime;
        boolean timeOver = elapsed >= gameDurationMillis;

        int aliveCount = 0;
        Snake lastAlive = null;
        Snake topScoreSnake = null;
        int maxScore = Integer.MIN_VALUE;

        for (Snake s : gameService.getMap().snakes.values()) {
            if (s.alive) {
                aliveCount++;
                lastAlive = s;
            }
            if (s.score > maxScore) {
                maxScore = s.score;
                topScoreSnake = s;
            }
        }

        if (timeOver || aliveCount <= 1) {
            String result;
            if (aliveCount == 1 && lastAlive != null) {
                // 只剩一条蛇
                result = lastAlive.nickname + " 胜利！";
            } else if (timeOver && aliveCount > 1 && topScoreSnake != null) {
                // 时间到但有多条蛇
                result = topScoreSnake.nickname + " 胜利！";
            } else {
                result = "平局！";
            }

            //System.out.println("[GameController] 游戏结束: " + result);
            //广播 END 消息
            broadcastSystemMsg("END", result);
            // 结束游戏
            onEndGame();
        }
    }

    // DDS 回调：结束游戏
    public void onEndGame() {
        if (ended) return;   // 已经结束，直接返回
        ended = true;

        // 停止 scheduler，等待任务结束
        if (scheduler != null) {
            scheduler.shutdown(); // 不直接 shutdownNow
            try {
                if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow(); // 超时才强制中断
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        bridge.shutdown();

        System.out.println("[GameController] Shutdown complete.");

    }

    // 广播所有玩家的 GameState
    private void broadcastAllGameStates() {
        for (Snake s : gameService.getMap().snakes.values()) {
            if (!s.alive) continue;

            // 打印原始 body
            //System.out.println("Init Snake " + s.nickname + " body: " + s.body);

            GameState gs = new GameState();
            gs.player_id = s.playerId;
            gs.length = s.body.size();
            gs.score = s.score;

            gs.snake_x = new LongSeq();
            gs.snake_y = new LongSeq();

            // ⚡ 预分配空间
            gs.snake_x.maximum(s.body.size());
            gs.snake_y.maximum(s.body.size());

            for (Point p : s.body) {
                gs.snake_x.append(p.x);
                gs.snake_y.append(p.y);
            }

            // 打印完整状态
            StringBuilder sb = new StringBuilder();
            sb.append("[DDS] 广播 GameState\n");
            sb.append("  player_id = ").append(gs.player_id).append("\n");
            sb.append("  nickname  = ").append(s.nickname).append("\n");
            sb.append("  score     = ").append(gs.score).append("\n");
            sb.append("  length    = ").append(gs.length).append("\n");
            sb.append("  body      = ");

            for (int i = 0; i < gs.snake_x.length(); i++) {
                sb.append("(")
                        .append(gs.snake_x.get_at(i))
                        .append(",")
                        .append(gs.snake_y.get_at(i))
                        .append(") ");
            }

            //System.out.println(sb.toString());

            // ⚡ 调用 DDS 发布接口
            bridge.publishGameState(gs);
        }
    }

    // 广播地图上所有 Item
    private void broadcastAllItems() {
        for (Item item : gameService.getMap().items.values()) {
            //System.out.println("[DDS] 广播 Item: id=" + item.item_id + " type=" + item.item_type.ordinal() +
                    //" x=" + item.x + " y=" + item.y);

            // ⚡ 调用 DDS 发布接口
            bridge.publishNewItem(item);
        }
    }

    private void broadcastLeaderboard() {
        // ⚡ 对接 DDS: Leaderboard
        Leaderboard lb = new Leaderboard();

        // 初始化 DDS 序列
        lb.entries = new LeaderboardEntrySeq();

        // 构造排序后的列表
        List<LeaderboardEntry> sortedList = new ArrayList<>();
        for (Snake s : gameService.getMap().snakes.values()) {
            LeaderboardEntry e = new LeaderboardEntry();
            e.player_id = s.playerId;
            e.nickname = s.nickname;
            e.score = s.score;
            sortedList.add(e);
        }
        sortedList.sort((a, b) -> Integer.compare(b.score, a.score));

        lb.entries.maximum(sortedList.size());

        // 填充 DDS 序列
        for (LeaderboardEntry e : sortedList) {
            lb.entries.append(e);
        }

        //System.out.println("[DDS] 广播 Leaderboard:");
        for (int i = 0; i < lb.entries.length(); i++) {
            LeaderboardEntry e = lb.entries.get_at(i); // 用 get_at() 访问
            //System.out.println(" - " + e.nickname + " 分数=" + e.score);
        }

        // ⚡ TODO: 调用 DDS 接口发送 Leaderboard
        bridge.publishLeaderboard(lb);
    }

    private void broadcastGetFood(Snake s, Item item) {
        GetFood gf = new GetFood();
        gf.player_id = s.playerId;
        gf.item_id   = item.item_id;
        gf.item_type = item.item_type;
        gf.x = item.x; gf.y = item.y;
        bridge.publishGetFood(gf);
    }

    public void broadcastSystemMsg(String type, String content) {
        // 创建 SystemMsg
        SystemMsg msg = new SystemMsg();
        msg.msg_type = type;
        msg.content = content;
        msg.timestamp = (int)System.currentTimeMillis();

        // 打印调试信息
//        System.out.println("[broadcastSystemMsg] Sending SystemMsg -> type: "
//                + msg.msg_type + ", content: "
//                + msg.content + ", timestamp: "
//                + msg.timestamp);

        // 通过 dds 发布
        bridge.publishSystemMsg(msg);
    }
}
