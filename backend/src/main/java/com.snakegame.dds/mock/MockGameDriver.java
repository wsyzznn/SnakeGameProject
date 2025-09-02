package com.snakegame.dds.mock;

import com.snakegame.dds.controller.GameController;
import com.snakegame.dds.SnakeGame.*; // 里面有 GameSetting, GameState, Snake, 等

import java.util.*;

public class MockGameDriver {

    public static void main(String[] args) throws InterruptedException {
        GameController controller = new GameController(80);

        // ====== 创建测试配置 ======
        GameSetting setting = new GameSetting();
        setting.grid_size = 20;
        setting.speed = 5; // 每秒 5 tick

        // ====== 创建测试玩家 ======
        List<PlayerInfo> players = new ArrayList<>();
        PlayerInfo p1 = new PlayerInfo();
        p1.player_id = 1;
        p1.nickname = "Alice";
        p1.color = "red";
        players.add(p1);

        PlayerInfo p2 = new PlayerInfo();
        p2.player_id = 2;
        p2.nickname = "Bob";
        p2.color = "blue";
        players.add(p2);

        // ====== 启动游戏 ======
        controller.onStartGame(players, setting);

        // ====== 模拟输入（两个玩家不断移动） ======
        new Thread(() -> {
            try {
                Random rand = new Random();
                String[] dirs = {"UP", "DOWN", "LEFT", "RIGHT"};

                while (true) {
                    // Alice 随机方向
                    PlayerMove m1 = new PlayerMove();
                    m1.player_id = 1;
                    m1.direction = dirs[rand.nextInt(dirs.length)];
                    m1.timestamp = (int) (System.currentTimeMillis() / 1000);
                    controller.onPlayerMove(m1);

                    // Bob 随机方向
                    PlayerMove m2 = new PlayerMove();
                    m2.player_id = 2;
                    m2.direction = dirs[rand.nextInt(dirs.length)];
                    m2.timestamp = (int) (System.currentTimeMillis() / 1000);
                    controller.onPlayerMove(m2);

                    Thread.sleep(300); // 每 300ms 产生新输入
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        // ====== 挂住主线程，观察运行结果 ======
        Thread.sleep(20000);

        // ====== 游戏结束 ======
        controller.onEndGame();
    }
}
