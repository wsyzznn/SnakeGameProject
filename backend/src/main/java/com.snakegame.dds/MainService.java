package com.snakegame.dds;

import com.snakegame.dds.SnakeGame.*;

// ↓↓↓ 这些包名依据 ZR-DDS 常见命名推测，若不一致，请按你 SDK 实际包名修改 ↓↓↓
import com.zrdds.domain.DomainParticipant;
import com.zrdds.domain.DomainParticipantFactory;
import com.zrdds.publication.Publisher;
import com.zrdds.subscription.Subscriber;
import com.zrdds.topic.Topic;
import com.zrdds.subscription.*;
import com.zrdds.infrastructure.InstanceHandle_t;
import com.zrdds.infrastructure.ResourceLimitsQosPolicy;
import com.zrdds.infrastructure.SampleStateKind;
import com.zrdds.infrastructure.ViewStateKind;
import com.zrdds.infrastructure.InstanceStateKind;
import com.zrdds.infrastructure.StatusKind;

import com.snakegame.dds.SnakeGame.PlayerAuth;
import com.snakegame.dds.SnakeGame.PlayerMove;
import com.snakegame.dds.SnakeGame.GameState;
import com.snakegame.dds.SnakeGame.SystemMsg;
import com.snakegame.dds.SnakeGame.Leaderboard;
import com.snakegame.dds.SnakeGame.Collision;
import com.snakegame.dds.SnakeGame.GetFood;
import com.snakegame.dds.SnakeGame.GameSetting;
import com.snakegame.dds.SnakeGame.ChatMsg;
import com.snakegame.dds.SnakeGame.InRoom;
import com.snakegame.dds.SnakeGame.PlayerInfo;
import com.snakegame.dds.SnakeGame.Item;

import com.snakegame.dds.SnakeGame.PlayerAuthTypeSupport;


public class MainServer {

    // 统一管理 Topic 名称，前后端要一致
    public static final String TOPIC_PLAYER_AUTH   = "PlayerAuthTopic";
    public static final String TOPIC_PLAYER_MOVE   = "PlayerMoveTopic";
    public static final String TOPIC_GAME_STATE    = "GameStateTopic";
    public static final String TOPIC_SYSTEM_MSG    = "SystemMsgTopic";
    public static final String TOPIC_LEADERBOARD   = "LeaderboardTopic";
    public static final String TOPIC_COLLISION     = "CollisionTopic";
    public static final String TOPIC_GET_FOOD      = "GetFoodTopic";
    public static final String TOPIC_GAME_SETTING  = "GameSettingTopic";
    public static final String TOPIC_CHAT_MSG      = "ChatMsgTopic";
    public static final String TOPIC_IN_ROOM       = "InRoomTopic";
    public static final String TOPIC_PLAYER_INFO   = "PlayerInfoTopic";
    public static final String TOPIC_ITEM          = "ItemTopic";

    private DomainParticipant participant;
    private Publisher publisher;
    private Subscriber subscriber;

    // 常用 DataWriter（后端 -> 前端）
    private SystemMsgDataWriter     systemMsgWriter;
    private GameStateDataWriter     gameStateWriter;
    private LeaderboardDataWriter   leaderboardWriter;
    private CollisionDataWriter     collisionWriter;
    private GetFoodDataWriter       getFoodWriter;
    private GameSettingDataWriter   gameSettingWriter;
    private ChatMsgDataWriter       chatMsgWriter;
    private InRoomDataWriter        inRoomWriter;
    private ItemDataWriter          itemWriter;

    // 常用 DataReader（前端 -> 后端）
    private PlayerAuthDataReader    authReader;
    private PlayerMoveDataReader    moveReader;
    private ChatMsgDataReader       chatReader;
    private PlayerInfoDataReader    playerInfoReader;
    private InRoomDataReader        inRoomReader;

    public static void main(String[] args) {
        MainServer server = new MainServer();
        server.start(0);  // domainId 可自定义
        // 简单阻塞主线程；生产中请用更优雅的生命周期管理
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
        try { Thread.currentThread().join(); } catch (InterruptedException ignored) {}
    }

    public void start(int domainId) {
        // 1) 创建 Participant（进入 DDS 域）
        participant = DomainParticipantFactory.get_instance()
                .create_participant(domainId, null, null, StatusKind.STATUS_MASK_NONE);

        // 2) 创建 Publisher / Subscriber
        publisher  = participant.create_publisher(null, null, StatusKind.STATUS_MASK_NONE);
        subscriber = participant.create_subscriber(null, null, StatusKind.STATUS_MASK_NONE);

        // 3) 创建 Topic（名字要与前端一致）
        // ----------------- PlayerAuth -----------------
        PlayerAuthTypeSupport authTs = (PlayerAuthTypeSupport) PlayerAuthTypeSupport.get_instance();
        String authTypeName = authTs.get_type_name();
        participant.register_type(authTs, authTypeName);
        Topic playerAuthTopic = participant.create_topic(
                TOPIC_PLAYER_AUTH,
                authTypeName,
                null, null,
                StatusKind.STATUS_MASK_NONE
        );

// ----------------- PlayerMove -----------------
        PlayerMoveTypeSupport moveTs = (PlayerMoveTypeSupport) PlayerMoveTypeSupport.get_instance();
        String moveTypeName = moveTs.get_type_name();
        participant.register_type(moveTs, moveTypeName);
        Topic playerMoveTopic = participant.create_topic(
                TOPIC_PLAYER_MOVE,
                moveTypeName,
                null, null,
                StatusKind.STATUS_MASK_NONE
        );

// ----------------- GameState -----------------
        GameStateTypeSupport stateTs = (GameStateTypeSupport) GameStateTypeSupport.get_instance();
        String stateTypeName = stateTs.get_type_name();
        participant.register_type(stateTs, stateTypeName);
        Topic gameStateTopic = participant.create_topic(
                TOPIC_GAME_STATE,
                stateTypeName,
                null, null,
                StatusKind.STATUS_MASK_NONE
        );

// ----------------- SystemMsg -----------------
        SystemMsgTypeSupport sysMsgTs = (SystemMsgTypeSupport) SystemMsgTypeSupport.get_instance();
        String sysMsgTypeName = sysMsgTs.get_type_name();
        participant.register_type(sysMsgTs, sysMsgTypeName);
        Topic systemMsgTopic = participant.create_topic(
                TOPIC_SYSTEM_MSG,
                sysMsgTypeName,
                null, null,
                StatusKind.STATUS_MASK_NONE
        );

// ----------------- Leaderboard -----------------
        LeaderboardTypeSupport lbTs = (LeaderboardTypeSupport) LeaderboardTypeSupport.get_instance();
        String lbTypeName = lbTs.get_type_name();
        participant.register_type(lbTs, lbTypeName);
        Topic leaderboardTopic = participant.create_topic(
                TOPIC_LEADERBOARD,
                lbTypeName,
                null, null,
                StatusKind.STATUS_MASK_NONE
        );

// ----------------- Collision -----------------
        CollisionTypeSupport collTs = (CollisionTypeSupport) CollisionTypeSupport.get_instance();
        String collTypeName = collTs.get_type_name();
        participant.register_type(collTs, collTypeName);
        Topic collisionTopic = participant.create_topic(
                TOPIC_COLLISION,
                collTypeName,
                null, null,
                StatusKind.STATUS_MASK_NONE
        );

// ----------------- GetFood -----------------
        GetFoodTypeSupport foodTs = (GetFoodTypeSupport) GetFoodTypeSupport.get_instance();
        String foodTypeName = foodTs.get_type_name();
        participant.register_type(foodTs, foodTypeName);
        Topic getFoodTopic = participant.create_topic(
                TOPIC_GET_FOOD,
                foodTypeName,
                null, null,
                StatusKind.STATUS_MASK_NONE
        );

// ----------------- GameSetting -----------------
        GameSettingTypeSupport settingTs = (GameSettingTypeSupport) GameSettingTypeSupport.get_instance();
        String settingTypeName = settingTs.get_type_name();
        participant.register_type(settingTs, settingTypeName);
        Topic gameSettingTopic = participant.create_topic(
                TOPIC_GAME_SETTING,
                settingTypeName,
                null, null,
                StatusKind.STATUS_MASK_NONE
        );

// ----------------- ChatMsg -----------------
        ChatMsgTypeSupport chatTs = (ChatMsgTypeSupport) ChatMsgTypeSupport.get_instance();
        String chatTypeName = chatTs.get_type_name();
        participant.register_type(chatTs, chatTypeName);
        Topic chatMsgTopic = participant.create_topic(
                TOPIC_CHAT_MSG,
                chatTypeName,
                null, null,
                StatusKind.STATUS_MASK_NONE
        );

// ----------------- InRoom -----------------
        InRoomTypeSupport inRoomTs = (InRoomTypeSupport) InRoomTypeSupport.get_instance();
        String inRoomTypeName = inRoomTs.get_type_name();
        participant.register_type(inRoomTs, inRoomTypeName);
        Topic inRoomTopic = participant.create_topic(
                TOPIC_IN_ROOM,
                inRoomTypeName,
                null, null,
                StatusKind.STATUS_MASK_NONE
        );

// ----------------- PlayerInfo -----------------
        PlayerInfoTypeSupport infoTs = (PlayerInfoTypeSupport) PlayerInfoTypeSupport.get_instance();
        String infoTypeName = infoTs.get_type_name();
        participant.register_type(infoTs, infoTypeName);
        Topic playerInfoTopic = participant.create_topic(
                TOPIC_PLAYER_INFO,
                infoTypeName,
                null, null,
                StatusKind.STATUS_MASK_NONE
        );

// ----------------- Item -----------------
        ItemTypeSupport itemTs = (ItemTypeSupport) ItemTypeSupport.get_instance();
        String itemTypeName = itemTs.get_type_name();
        participant.register_type(itemTs, itemTypeName);
        Topic itemTopic = participant.create_topic(
                TOPIC_ITEM,
                itemTypeName,
                null, null,
                StatusKind.STATUS_MASK_NONE
        );


        // 4) 创建 DataWriter（后端 -> 前端要发的）
        systemMsgWriter   = (SystemMsgDataWriter)   publisher.create_datawriter(systemMsgTopic, null, null, StatusKind.STATUS_MASK_NONE);
        gameStateWriter   = (GameStateDataWriter)   publisher.create_datawriter(gameStateTopic, null, null, StatusKind.STATUS_MASK_NONE);
        leaderboardWriter = (LeaderboardDataWriter) publisher.create_datawriter(leaderboardTopic, null, null, StatusKind.STATUS_MASK_NONE);
        collisionWriter   = (CollisionDataWriter)   publisher.create_datawriter(collisionTopic, null, null, StatusKind.STATUS_MASK_NONE);
        getFoodWriter     = (GetFoodDataWriter)     publisher.create_datawriter(getFoodTopic, null, null, StatusKind.STATUS_MASK_NONE);
        gameSettingWriter = (GameSettingDataWriter) publisher.create_datawriter(gameSettingTopic, null, null, StatusKind.STATUS_MASK_NONE);
        chatMsgWriter     = (ChatMsgDataWriter)     publisher.create_datawriter(chatMsgTopic, null, null, StatusKind.STATUS_MASK_NONE);
        inRoomWriter      = (InRoomDataWriter)      publisher.create_datawriter(inRoomTopic, null, null, StatusKind.STATUS_MASK_NONE);
        itemWriter        = (ItemDataWriter)        publisher.create_datawriter(itemTopic, null, null, StatusKind.STATUS_MASK_NONE);

        // 5) 创建 DataReader（前端 -> 后端要收的）
        authReader      = (PlayerAuthDataReader)    subscriber.create_datareader(playerAuthTopic, null, null, StatusKind.STATUS_MASK_NONE);
        moveReader      = (PlayerMoveDataReader)    subscriber.create_datareader(playerMoveTopic, null, null, StatusKind.STATUS_MASK_NONE);
        chatReader      = (ChatMsgDataReader)       subscriber.create_datareader(chatMsgTopic, null, null, StatusKind.STATUS_MASK_NONE);
        playerInfoReader= (PlayerInfoDataReader)    subscriber.create_datareader(playerInfoTopic, null, null, StatusKind.STATUS_MASK_NONE);
        inRoomReader    = (InRoomDataReader)        subscriber.create_datareader(inRoomTopic, null, null, StatusKind.STATUS_MASK_NONE);

        // 6) 绑定监听器（示例：认证、移动、聊天）
        bindPlayerAuthListener();
        bindPlayerMoveListener();
        bindChatListener();

        // 7) 可选：开局下发全局设置（示例）
        GameSetting setting = new GameSetting();
        setting.speed = 8;
        setting.grid_size = 32;
        gameSettingWriter.write(setting, InstanceHandle_t.HANDLE_NIL);

        log("MainServer started. Domain=" + domainId);
    }

    private void bindPlayerAuthListener() {
        authReader.setListener(new DataReaderAdapter() {
            @Override
            public void on_data_available(DataReader reader) {
                PlayerAuthDataReader r = (PlayerAuthDataReader) reader;
                PlayerAuthSeq dataSeq = new PlayerAuthSeq();
                SampleInfoSeq infoSeq = new SampleInfoSeq();
                try {
                    r.take(
                            dataSeq, infoSeq,
                            ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
                            SampleStateKind.ANY_SAMPLE_STATE,
                            ViewStateKind.ANY_VIEW_STATE,
                            InstanceStateKind.ANY_INSTANCE_STATE
                    );
                    for (int i = 0; i < dataSeq.size(); i++) {
                        PlayerAuth auth = dataSeq.get(i);
                        // TODO: 在这里调用你自己的 AuthService 进行注册/登录校验
                        // 下面演示一个最小闭环：直接回复 SystemMsg
                        SystemMsg resp = new SystemMsg();
                        resp.msg_type = "LOGIN_OK";     // 或 REGISTER_OK/FAIL 等
                        resp.content  = "Hello, " + auth.nickname;
                        resp.timestamp = System.currentTimeMillis();
                        systemMsgWriter.write(resp, InstanceHandle_t.HANDLE_NIL);

                        log("Auth received: nickname=" + auth.nickname);
                    }
                } finally {
                    r.return_loan(dataSeq, infoSeq);
                }
            }
        }, StatusKind.DATA_AVAILABLE_STATUS);
    }

    private void bindPlayerMoveListener() {
        moveReader.setListener(new DataReaderAdapter() {
            @Override
            public void on_data_available(DataReader reader) {
                PlayerMoveDataReader r = (PlayerMoveDataReader) reader;
                PlayerMoveSeq dataSeq = new PlayerMoveSeq();
                SampleInfoSeq infoSeq = new SampleInfoSeq();
                try {
                    r.take(
                            dataSeq, infoSeq,
                            ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
                            SampleStateKind.ANY_SAMPLE_STATE,
                            ViewStateKind.ANY_VIEW_STATE,
                            InstanceStateKind.ANY_INSTANCE_STATE
                    );
                    for (int i = 0; i < dataSeq.size(); i++) {
                        PlayerMove mv = dataSeq.get(i);
                        // TODO: 调用 GameService 进行移动、碰撞检测、吃食物、计分等
                        // 这里演示：构造一个最小 GameState 回发给前端（真实项目请从服务里拿状态）
                        GameState st = new GameState();
                        st.player_id = mv.player_id;
                        st.snake_x = new long[]{0}; // 示例占位
                        st.snake_y = new long[]{0};
                        st.length  = 1;
                        st.score   = 0;
                        gameStateWriter.write(st, InstanceHandle_t.HANDLE_NIL);

                        log("Move received: player=" + mv.player_id + " dir=" + mv.direction);
                    }
                } finally {
                    r.return_loan(dataSeq, infoSeq);
                }
            }
        }, StatusKind.DATA_AVAILABLE_STATUS);
    }

    private void bindChatListener() {
        chatReader.setListener(new DataReaderAdapter() {
            @Override
            public void on_data_available(DataReader reader) {
                ChatMsgDataReader r = (ChatMsgDataReader) reader;
                ChatMsgSeq dataSeq = new ChatMsgSeq();
                SampleInfoSeq infoSeq = new SampleInfoSeq();
                try {
                    r.take(
                            dataSeq, infoSeq,
                            ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
                            SampleStateKind.ANY_SAMPLE_STATE,
                            ViewStateKind.ANY_VIEW_STATE,
                            InstanceStateKind.ANY_INSTANCE_STATE
                    );
                    for (int i = 0; i < dataSeq.size(); i++) {
                        ChatMsg msg = dataSeq.get(i);
                        // 简单转发：后端收到就广播回去（生产环境可做房间隔离/敏感词等）
                        chatMsgWriter.write(msg, InstanceHandle_t.HANDLE_NIL);
                        log("Chat received from " + msg.player_id + ": " + msg.content);
                    }
                } finally {
                    r.return_loan(dataSeq, infoSeq);
                }
            }
        }, StatusKind.DATA_AVAILABLE_STATUS);
    }

    public void shutdown() {
        log("MainServer shutting down...");
        try {
            if (participant != null) {
                participant.delete_contained_entities();
                DomainParticipantFactory.get_instance().delete_participant(participant);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void log(String s) {
        System.out.println("[MainServer] " + s);
    }
}
