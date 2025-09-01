package com.snakegame.dds.controller;

import com.snakegame.dds.model.Point;
import com.snakegame.dds.model.Snake;
import com.snakegame.dds.service.GameService;
import com.snakegame.dds.SnakeGame.*; // ⚡ 对接 DDS: PlayerInfo, PlayerMove, GameSetting
import com.zrdds.infrastructure.LongSeq;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.*;

public class GameController {
    private GameService gameService;
    private Map<Integer, String> latestInputs;
    private ScheduledExecutorService scheduler;
    private int tickCounter = 0;


    public GameController() {
        this.gameService = new GameService();
        this.latestInputs = new ConcurrentHashMap<>();
    }

//    // DDS 订阅 SystemMsg
//    public void subscribeSystemMsg(DdsSubscriber<SystemMsg> subscriber) {
//        subscriber.subscribe(SystemMsg.class, this::onSystemMsgReceived);
//    }
//
//    private void onSystemMsgReceived(SystemMsg msg) {
//        switch (msg.msg_type) {
//            case "START":
//                // 前端发起 START
//                List<PlayerInfo> players = parsePlayers(msg.content);
//                GameSetting setting = parseGameSetting(msg.content);
//                onStartGame(players, setting);
//                break;
//            case "EXIT":
//                // 玩家加入/退出处理
//        }
//    }

    public void onStartGame(List<PlayerInfo> players, GameSetting setting) {
        // 1. 初始化游戏逻辑
        gameService.initGame(players, setting.grid_size, setting.grid_size);

        // 2. 广播初始 GameState
        broadcastAllGameStates();

        // 3. 广播初始道具
        broadcastAllItems();

        // 4. 启动主循环（定时器驱动）
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
        }
    }

    // 定时器驱动
    public void onGameTick(boolean spawnFoodThisTick) {
        gameService.gameTick(new HashMap<>(latestInputs), spawnFoodThisTick);
        broadcastAllGameStates();
        broadcastAllItems();
        broadcastLeaderboard();
    }

    // DDS 回调：结束游戏
    public void onEndGame() {
        scheduler.shutdownNow();
        gameService.endGame();
    }

    // 广播所有玩家的 GameState
    private void broadcastAllGameStates() {
        for (Snake s : gameService.getMap().snakes.values()) {
            if (!s.alive) continue;

            GameState gs = new GameState();
            gs.player_id = s.playerId;
            gs.length = s.body.size();
            gs.score = s.score;
            gs.snake_x = new LongSeq();
            gs.snake_y = new LongSeq();

            int i = 0;
            for (Point p : s.body) {
                gs.snake_x.append(p.x);
                gs.snake_y.append(p.y);
                i++;
            }

            // ⚡ 调用 DDS 发布接口
            // ddsPublisher.publishGameState(gs);
            System.out.println("[DDS] 广播 GameState: player=" + s.nickname + " length=" + gs.length);
        }
    }

    // 广播地图上所有 Item
    private void broadcastAllItems() {
        for (Item item : gameService.getMap().items.values()) {
            // ⚡ 调用 DDS 发布接口
            // ddsPublisher.publishItem(item);
            System.out.println("[DDS] 广播 Item: id=" + item.item_id + " type=" + item.item_type.ordinal() +
                    " x=" + item.x + " y=" + item.y);
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

        // 填充 DDS 序列
        for (LeaderboardEntry e : sortedList) {
            lb.entries.append(e);
        }

        System.out.println("[DDS] 广播 Leaderboard:");
        for (int i = 0; i < lb.entries.length(); i++) {
            LeaderboardEntry e = lb.entries.get_at(i); // 用 get_at() 访问
            System.out.println(" - " + e.nickname + " 分数=" + e.score);
        }

        // ⚡ TODO: 调用 DDS 接口发送 Leaderboard
        // ddsPublisher.publishLeaderboard(lb);
    }
}
