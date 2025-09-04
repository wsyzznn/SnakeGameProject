package com.snakegame.dds.test;

import com.snakegame.dds.controller.GameController;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class UnderTestMain {
    public static void main(String[] args) throws Exception {
        int domain = (args.length > 0) ? Integer.parseInt(args[0]) : 0;
        System.out.println("[UnderTestMain] Starting GameController (domain=" + domain + ") ...");

        // 使用真实 GameController（你的 GameController 构造器会 init 内部 DdsBridge）
        GameController controller = new GameController(domain);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("[UnderTestMain] ShutdownHook -> onEndGame()");
                controller.onEndGame();
                TimeUnit.MILLISECONDS.sleep(300);
            } catch (Throwable t) { t.printStackTrace(); }
            System.out.println("[UnderTestMain] ShutdownHook done.");
        }));

        System.out.println("[UnderTestMain] Ready. Press ENTER to shutdown.");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        br.readLine(); // 阻塞直到按回车

        System.out.println("[UnderTestMain] ENTER pressed -> shutting down.");
        controller.onEndGame();
        TimeUnit.MILLISECONDS.sleep(300);
        System.out.println("[UnderTestMain] Exit.");
    }
}
