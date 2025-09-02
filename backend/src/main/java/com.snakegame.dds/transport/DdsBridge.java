package com.snakegame.dds.transport;

import com.snakegame.dds.SnakeGame.*; // 由 zrd dsgen 生成的类型、TypeSupport、Readers/Writers
import com.snakegame.dds.controller.GameController;

import com.zrdds.domain.DomainParticipant;
import com.zrdds.domain.DomainParticipantFactory;
import com.zrdds.infrastructure.*;
import com.zrdds.publication.DataWriterQos;
import com.zrdds.publication.Publisher;
import com.zrdds.publication.DataWriter;
import com.zrdds.subscription.DataReaderQos;
import com.zrdds.subscription.Subscriber;
import com.zrdds.subscription.DataReader;
import com.zrdds.subscription.DataReaderListener;
import com.zrdds.topic.Topic;

import java.util.ArrayList;
import java.util.List;

public class DdsBridge {

    // ==== Topics ====
    public static final String T_PLAYER_MOVE   = "PLAYER_MOVE";
    public static final String T_SYSTEM_MSG    = "SYSTEM_MSG";
    public static final String T_PLAYER_INFO   = "PLAYER_INFO";
    public static final String T_GAME_SETTING  = "GAME_SETTING";

    public static final String T_GAME_STATE    = "GAME_STATE";
    public static final String T_ITEM          = "ITEM";
    public static final String T_GET_FOOD      = "GET_FOOD";
    public static final String T_LEADERBOARD   = "LEADERBOARD";
    public static final String T_COLLISION     = "COLLISION";

    private final int domainId;
    private final GameController controller;

    private DomainParticipant dp;
    private Publisher pub;
    private Subscriber sub;

    // Writers
    private GameStateDataWriter gameStateWriter;
    private ItemDataWriter itemWriter;
    private GetFoodDataWriter getFoodWriter;
    private LeaderboardDataWriter leaderboardWriter;
    private CollisionDataWriter collisionWriter;
    private SystemMsgDataWriter systemMsgWriter;

    // Readers
    private PlayerMoveDataReader playerMoveReader;
    private SystemMsgDataReader systemMsgReader;
    private PlayerInfoDataReader playerInfoReader;
    private GameSettingDataReader gameSettingReader;

    public DdsBridge(int domainId, GameController controller) {
        this.domainId = domainId;
        this.controller = controller;
    }

    // ========= Init / Shutdown =========
    public boolean init() {
        loadLibrary();

        dp = DomainParticipantFactory.get_instance().create_participant(
                domainId, DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT, null,
                StatusKind.STATUS_MASK_NONE);
        if (dp == null) { System.err.println("dp create failed"); return false; }

        // ---- register all types (由 idl 生成的 TypeSupport) ----
        PlayerMoveTypeSupport playerMoveTS       = (PlayerMoveTypeSupport) PlayerMoveTypeSupport.get_instance();
        SystemMsgTypeSupport systemMsgTS         = (SystemMsgTypeSupport) SystemMsgTypeSupport.get_instance();
        PlayerInfoTypeSupport playerInfoTS       = (PlayerInfoTypeSupport) PlayerInfoTypeSupport.get_instance();
        GameSettingTypeSupport gameSettingTS     = (GameSettingTypeSupport) GameSettingTypeSupport.get_instance();
        GameStateTypeSupport gameStateTS         = (GameStateTypeSupport) GameStateTypeSupport.get_instance();
        ItemTypeSupport itemTS                   = (ItemTypeSupport) ItemTypeSupport.get_instance();
        GetFoodTypeSupport getFoodTS             = (GetFoodTypeSupport) GetFoodTypeSupport.get_instance();
        LeaderboardTypeSupport leaderboardTS     = (LeaderboardTypeSupport) LeaderboardTypeSupport.get_instance();
        CollisionTypeSupport collisionTS         = (CollisionTypeSupport) CollisionTypeSupport.get_instance();

        if (playerMoveTS.register_type(dp, null)     != ReturnCode_t.RETCODE_OK) return false;
        if (systemMsgTS.register_type(dp, null)     != ReturnCode_t.RETCODE_OK) return false;
        if (playerInfoTS.register_type(dp, null)    != ReturnCode_t.RETCODE_OK) return false;
        if (gameSettingTS.register_type(dp, null)   != ReturnCode_t.RETCODE_OK) return false;
        if (gameStateTS.register_type(dp, null)     != ReturnCode_t.RETCODE_OK) return false;
        if (itemTS.register_type(dp, null)          != ReturnCode_t.RETCODE_OK) return false;
        if (getFoodTS.register_type(dp, null)       != ReturnCode_t.RETCODE_OK) return false;
        if (leaderboardTS.register_type(dp, null)   != ReturnCode_t.RETCODE_OK) return false;
        if (collisionTS.register_type(dp, null)     != ReturnCode_t.RETCODE_OK) return false;

        // ---- topics ----
        Topic tpMove        = dp.create_topic(T_PLAYER_MOVE,   playerMoveTS.get_type_name(),   DomainParticipant.TOPIC_QOS_DEFAULT, null, StatusKind.STATUS_MASK_NONE);
        Topic tpSys         = dp.create_topic(T_SYSTEM_MSG,    systemMsgTS.get_type_name(),    DomainParticipant.TOPIC_QOS_DEFAULT, null, StatusKind.STATUS_MASK_NONE);
        Topic tpPInfo       = dp.create_topic(T_PLAYER_INFO,   playerInfoTS.get_type_name(),   DomainParticipant.TOPIC_QOS_DEFAULT, null, StatusKind.STATUS_MASK_NONE);
        Topic tpSetting     = dp.create_topic(T_GAME_SETTING,  gameSettingTS.get_type_name(),  DomainParticipant.TOPIC_QOS_DEFAULT, null, StatusKind.STATUS_MASK_NONE);
        Topic tpState       = dp.create_topic(T_GAME_STATE,    gameStateTS.get_type_name(),    DomainParticipant.TOPIC_QOS_DEFAULT, null, StatusKind.STATUS_MASK_NONE);
        Topic tpItem        = dp.create_topic(T_ITEM,          itemTS.get_type_name(),         DomainParticipant.TOPIC_QOS_DEFAULT, null, StatusKind.STATUS_MASK_NONE);
        Topic tpGetFood     = dp.create_topic(T_GET_FOOD,      getFoodTS.get_type_name(),      DomainParticipant.TOPIC_QOS_DEFAULT, null, StatusKind.STATUS_MASK_NONE);
        Topic tpLeaderboard = dp.create_topic(T_LEADERBOARD,   leaderboardTS.get_type_name(),  DomainParticipant.TOPIC_QOS_DEFAULT, null, StatusKind.STATUS_MASK_NONE);
        Topic tpCollision   = dp.create_topic(T_COLLISION,     collisionTS.get_type_name(),    DomainParticipant.TOPIC_QOS_DEFAULT, null, StatusKind.STATUS_MASK_NONE);


        if (tpMove==null||tpSys==null||tpPInfo==null||tpSetting==null||tpState==null||tpItem==null||tpGetFood==null||tpLeaderboard==null||tpCollision==null){
            System.err.println("topic create failed"); return false;
        }

        // ---- pub / sub ----
        pub = dp.create_publisher(DomainParticipant.PUBLISHER_QOS_DEFAULT, null, StatusKind.STATUS_MASK_NONE);
        if (pub == null) { System.err.println("publisher create failed"); return false; }

        sub = dp.create_subscriber(DomainParticipant.SUBSCRIBER_QOS_DEFAULT, null, StatusKind.STATUS_MASK_NONE);
        if (sub == null) { System.err.println("subscriber create failed"); return false; }

        // ---- create writers with QoS ----
        gameStateWriter  = (GameStateDataWriter)  pub.create_datawriter(tpState,     reliableLatestQos(pub, DurabilityQosPolicyKind.VOLATILE_DURABILITY_QOS), null, StatusKind.STATUS_MASK_NONE);
        itemWriter       = (ItemDataWriter)       pub.create_datawriter(tpItem,      reliableKeepAllQos(pub, DurabilityQosPolicyKind.TRANSIENT_LOCAL_DURABILITY_QOS), null, StatusKind.STATUS_MASK_NONE);
        getFoodWriter    = (GetFoodDataWriter)    pub.create_datawriter(tpGetFood,   reliableKeepAllQos(pub, DurabilityQosPolicyKind.VOLATILE_DURABILITY_QOS), null, StatusKind.STATUS_MASK_NONE);
        leaderboardWriter= (LeaderboardDataWriter)pub.create_datawriter(tpLeaderboard,reliableLatestQos(pub, DurabilityQosPolicyKind.TRANSIENT_LOCAL_DURABILITY_QOS), null, StatusKind.STATUS_MASK_NONE);
        collisionWriter  = (CollisionDataWriter)  pub.create_datawriter(tpCollision, reliableKeepAllQos(pub, DurabilityQosPolicyKind.VOLATILE_DURABILITY_QOS), null, StatusKind.STATUS_MASK_NONE);
        systemMsgWriter  = (SystemMsgDataWriter)  pub.create_datawriter(tpSys,       reliableLatestQos(pub, DurabilityQosPolicyKind.TRANSIENT_LOCAL_DURABILITY_QOS), null, StatusKind.STATUS_MASK_NONE);

        // ---- create readers with QoS + listeners ----
        playerMoveReader = (PlayerMoveDataReader) sub.create_datareader(tpMove,     reliableLatestQos(sub, DurabilityQosPolicyKind.VOLATILE_DURABILITY_QOS), new PlayerMoveListener(), StatusKind.DATA_AVAILABLE_STATUS);
        systemMsgReader  = (SystemMsgDataReader)  sub.create_datareader(tpSys,      reliableLatestQos(sub, DurabilityQosPolicyKind.TRANSIENT_LOCAL_DURABILITY_QOS), new SystemMsgListener(), StatusKind.DATA_AVAILABLE_STATUS);
        playerInfoReader = (PlayerInfoDataReader) sub.create_datareader(tpPInfo,    reliableKeepAllQos(sub, DurabilityQosPolicyKind.TRANSIENT_LOCAL_DURABILITY_QOS), new PlayerInfoListener(), StatusKind.DATA_AVAILABLE_STATUS);
        gameSettingReader= (GameSettingDataReader)sub.create_datareader(tpSetting,  reliableLatestQos(sub, DurabilityQosPolicyKind.TRANSIENT_LOCAL_DURABILITY_QOS), new GameSettingListener(), StatusKind.DATA_AVAILABLE_STATUS);

        return true;
    }

    public void shutdown() {
        if (dp == null) return;
        dp.delete_contained_entities();
        DomainParticipantFactory.get_instance().delete_participant(dp);
        DomainParticipantFactory.finalize_instance();
    }

    private static boolean hasLoad = false;
    private static void loadLibrary() {
        if (!hasLoad) { System.loadLibrary("ZRDDS_JAVA"); hasLoad = true; }
    }

    // ========= QoS helpers (Readers 与 Writers 版本各一套) =========
    private static DataWriterQos reliableLatestQos(Publisher pub, DurabilityQosPolicyKind durability) {
        DataWriterQos q = new DataWriterQos();
        pub.get_default_datawriter_qos(q);
        q.reliability.kind = ReliabilityQosPolicyKind.RELIABLE_RELIABILITY_QOS;
        q.durability.kind  = durability;
        q.history.kind     = HistoryQosPolicyKind.KEEP_LAST_HISTORY_QOS;
        q.history.depth    = 1;
        return q;
    }
    private static DataWriterQos reliableKeepAllQos(Publisher pub, DurabilityQosPolicyKind durability) {
        DataWriterQos q = new DataWriterQos();
        pub.get_default_datawriter_qos(q);
        q.reliability.kind = ReliabilityQosPolicyKind.RELIABLE_RELIABILITY_QOS;
        q.durability.kind  = durability;
        q.history.kind     = HistoryQosPolicyKind.KEEP_ALL_HISTORY_QOS;
        return q;
    }
    private static DataReaderQos reliableLatestQos(Subscriber sub, DurabilityQosPolicyKind durability) {
        DataReaderQos q = new DataReaderQos();
        sub.get_default_datareader_qos(q);
        q.reliability.kind = ReliabilityQosPolicyKind.RELIABLE_RELIABILITY_QOS;
        q.durability.kind  = durability;
        q.history.kind     = HistoryQosPolicyKind.KEEP_LAST_HISTORY_QOS;
        q.history.depth    = 1;
        return q;
    }
    private static DataReaderQos reliableKeepAllQos(Subscriber sub, DurabilityQosPolicyKind durability) {
        DataReaderQos q = new DataReaderQos();
        sub.get_default_datareader_qos(q);
        q.reliability.kind = ReliabilityQosPolicyKind.RELIABLE_RELIABILITY_QOS;
        q.durability.kind  = durability;
        q.history.kind     = HistoryQosPolicyKind.KEEP_ALL_HISTORY_QOS;
        return q;
    }

    // ========= Writers（给 GameController 调用）=========
    public void publishGameState(GameState gs) {
        if (gameStateWriter == null) return;
        gameStateWriter.write(gs, InstanceHandle_t.HANDLE_NIL_NATIVE);
    }

    public void publishNewItem(Item it) {
        if (itemWriter == null) return;
        itemWriter.write(it, InstanceHandle_t.HANDLE_NIL_NATIVE);
    }

    public void publishGetFood(GetFood gf) {
        if (getFoodWriter == null) return;
        getFoodWriter.write(gf, InstanceHandle_t.HANDLE_NIL_NATIVE);
    }

    public void publishLeaderboard(Leaderboard lb) {
        if (leaderboardWriter == null) return;
        leaderboardWriter.write(lb, InstanceHandle_t.HANDLE_NIL_NATIVE);
    }

    public void publishCollision(Collision c) {
        if (collisionWriter == null) return;
        collisionWriter.write(c, InstanceHandle_t.HANDLE_NIL_NATIVE);
    }

    public void publishSystemMsg(SystemMsg m) {
        if (systemMsgWriter == null) return;
        systemMsgWriter.write(m, InstanceHandle_t.HANDLE_NIL_NATIVE);
    }

    // ========= Readers Listeners =========

    /** 高频输入：latest only，交给 GameController.onPlayerMove */
    private class PlayerMoveListener implements DataReaderListener {
        public void on_data_available(DataReader dr) {
            PlayerMoveDataReader r = (PlayerMoveDataReader) dr;
            PlayerMoveSeq dataSeq = new PlayerMoveSeq();
            SampleInfoSeq infoSeq = new SampleInfoSeq();
            ReturnCode_t rc = r.take(dataSeq, infoSeq, -1,
                    SampleStateKind.ANY_SAMPLE_STATE,
                    ViewStateKind.ANY_VIEW_STATE,
                    InstanceStateKind.ANY_INSTANCE_STATE);
            if (rc != ReturnCode_t.RETCODE_OK) return;

            for (int i = 0; i < infoSeq.length(); i++) {
                SampleInfo si = infoSeq.get_at(i);
                if (!si.valid_data) continue;
                PlayerMove mv = dataSeq.get_at(i);
                controller.onPlayerMove(mv); // 只保留 latestInputs 的策略在 controller 内部
            }
            r.return_loan(dataSeq, infoSeq);
        }
        // 其它回调留空
        public void on_liveliness_changed(DataReader a, LivelinessChangedStatus b) {}
        public void on_requested_deadline_missed(DataReader a, RequestedDeadlineMissedStatus b) {}
        public void on_requested_incompatible_qos(DataReader a, RequestedIncompatibleQosStatus b) {}
        public void on_sample_lost(DataReader a, SampleLostStatus b) {}
        public void on_sample_rejected(DataReader a, SampleRejectedStatus b) {}
        public void on_subscription_matched(DataReader a, SubscriptionMatchedStatus b) {}
        public void on_data_arrived(DataReader a, Object o, SampleInfo si) {}
    }

    /** SystemMsg：等待 "START"，并依赖 PlayerInfo / GameSetting 缓存 */
    private class SystemMsgListener implements DataReaderListener {
        public void on_data_available(DataReader dr) {
            SystemMsgDataReader r = (SystemMsgDataReader) dr;
            SystemMsgSeq dataSeq = new SystemMsgSeq();
            SampleInfoSeq infoSeq = new SampleInfoSeq();
            ReturnCode_t rc = r.take(dataSeq, infoSeq, -1,
                    SampleStateKind.ANY_SAMPLE_STATE,
                    ViewStateKind.ANY_VIEW_STATE,
                    InstanceStateKind.ANY_INSTANCE_STATE);
            if (rc != ReturnCode_t.RETCODE_OK) return;

            for (int i=0;i<infoSeq.length();i++){
                if (!infoSeq.get_at(i).valid_data) continue;
                SystemMsg m = dataSeq.get_at(i);
                if ("START".equals(m.msg_type)) {
                    // 从缓存取 PlayerInfo 与 GameSetting
                    PlayerInfoSeq players = readAllOnce(playerInfoReader);
                    GameSetting setting = readLastOnce(gameSettingReader);

                    List<PlayerInfo> playerList = new ArrayList<>();
                    for (int j = 0; j < players.length(); j++) {
                        playerList.add(players.get_at(j));
                    }

                    controller.onStartGame(playerList, setting);

                }
            }
            r.return_loan(dataSeq, infoSeq);
        }
        public void on_liveliness_changed(DataReader a, LivelinessChangedStatus b) {}
        public void on_requested_deadline_missed(DataReader a, RequestedDeadlineMissedStatus b) {}
        public void on_requested_incompatible_qos(DataReader a, RequestedIncompatibleQosStatus b) {}
        public void on_sample_lost(DataReader a, SampleLostStatus b) {}
        public void on_sample_rejected(DataReader a, SampleRejectedStatus b) {}
        public void on_subscription_matched(DataReader a, SubscriptionMatchedStatus b) {}
        public void on_data_arrived(DataReader a, Object o, SampleInfo si) {}
    }

    /** PlayerInfo：只负责缓存（TRANSIENT_LOCAL），真正使用在 START 时 */
    private class PlayerInfoListener implements DataReaderListener {
        public void on_data_available(DataReader dr) {
            // 可选：也可以维护一份本地 List<PlayerInfo> 缓存
            // 这里保持简单，不做立即处理，START 再统一读取
            // 调用 read/take 是为了清空 NOT_READ 状态，避免累计
            PlayerInfoDataReader r = (PlayerInfoDataReader) dr;
            PlayerInfoSeq dataSeq = new PlayerInfoSeq();
            SampleInfoSeq infoSeq = new SampleInfoSeq();
            r.read(dataSeq, infoSeq, -1,
                    SampleStateKind.ANY_SAMPLE_STATE,
                    ViewStateKind.ANY_VIEW_STATE,
                    InstanceStateKind.ANY_INSTANCE_STATE);
            r.return_loan(dataSeq, infoSeq);
        }
        public void on_liveliness_changed(DataReader a, LivelinessChangedStatus b) {}
        public void on_requested_deadline_missed(DataReader a, RequestedDeadlineMissedStatus b) {}
        public void on_requested_incompatible_qos(DataReader a, RequestedIncompatibleQosStatus b) {}
        public void on_sample_lost(DataReader a, SampleLostStatus b) {}
        public void on_sample_rejected(DataReader a, SampleRejectedStatus b) {}
        public void on_subscription_matched(DataReader a, SubscriptionMatchedStatus b) {}
        public void on_data_arrived(DataReader a, Object o, SampleInfo si) {}
    }

    /** GameSetting：同上，仅缓存最新一条 */
    private class GameSettingListener implements DataReaderListener {
        public void on_data_available(DataReader dr) {
            GameSettingDataReader r = (GameSettingDataReader) dr;
            GameSettingSeq dataSeq = new GameSettingSeq();
            SampleInfoSeq infoSeq = new SampleInfoSeq();
            r.read(dataSeq, infoSeq, -1,
                    SampleStateKind.ANY_SAMPLE_STATE,
                    ViewStateKind.ANY_VIEW_STATE,
                    InstanceStateKind.ANY_INSTANCE_STATE);
            r.return_loan(dataSeq, infoSeq);
        }
        public void on_liveliness_changed(DataReader a, LivelinessChangedStatus b) {}
        public void on_requested_deadline_missed(DataReader a, RequestedDeadlineMissedStatus b) {}
        public void on_requested_incompatible_qos(DataReader a, RequestedIncompatibleQosStatus b) {}
        public void on_sample_lost(DataReader a, SampleLostStatus b) {}
        public void on_sample_rejected(DataReader a, SampleRejectedStatus b) {}
        public void on_subscription_matched(DataReader a, SubscriptionMatchedStatus b) {}
        public void on_data_arrived(DataReader a, Object o, SampleInfo si) {}
    }

    // ======= Helpers: 一次性把缓存里的数据读出来 =======
    private static PlayerInfoSeq readAllOnce(PlayerInfoDataReader r) {
        PlayerInfoSeq ds = new PlayerInfoSeq();
        SampleInfoSeq is = new SampleInfoSeq();
        r.take(ds, is, -1, SampleStateKind.ANY_SAMPLE_STATE, ViewStateKind.ANY_VIEW_STATE, InstanceStateKind.ANY_INSTANCE_STATE);
        r.return_loan(ds, is);
        return ds;
    }
    private static GameSetting readLastOnce(GameSettingDataReader r) {
        GameSettingSeq ds = new GameSettingSeq();
        SampleInfoSeq is = new SampleInfoSeq();
        r.take(ds, is, -1, SampleStateKind.ANY_SAMPLE_STATE, ViewStateKind.ANY_VIEW_STATE, InstanceStateKind.ANY_INSTANCE_STATE);
        GameSetting last = (ds.length() > 0) ? ds.get_at(ds.length()-1) : defaultSetting();
        r.return_loan(ds, is);
        return last;
    }
    private static GameSetting defaultSetting() {
        GameSetting g = new GameSetting();
        g.grid_size = 40;
        g.speed = 10;
        return g;
    }
}
