package com.snakegame.dds.Publisher;

import com.snakegame.dds.SnakeGame.PlayerAuth;
import com.snakegame.dds.SnakeGame.PlayerAuthDataWriter;
import com.snakegame.dds.SnakeGame.PlayerAuthTypeSupport;
import com.zrdds.domain.DomainParticipant;
import com.zrdds.domain.DomainParticipantFactory;
import com.zrdds.infrastructure.ReturnCode_t;
import com.zrdds.infrastructure.StatusKind;
import com.zrdds.publication.DataWriter;
import com.zrdds.publication.Publisher;
import com.zrdds.topic.Topic;

/**
 * 用户认证后端 Publisher（向 PlayerAuthTopic 发送认证结果）
 */
public class PlayerAuthPublisher {
    private static DomainParticipant dp;
    private static Publisher pub;
    private static Topic tp;
    private static PlayerAuthDataWriter writer;

    public static void main(String[] args) {
        loadLibrary();
        ReturnCode_t rtn;

        int domain_id = 80;

        // 1. 创建 DomainParticipant
        dp = DomainParticipantFactory.get_instance().create_participant(
                domain_id,
                DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT,
                null,
                StatusKind.STATUS_MASK_NONE
        );
        if (dp == null) {
            System.out.println("create dp failed");
            return;
        }

        // 2. 注册 PlayerAuth 类型
        PlayerAuthTypeSupport ts = (PlayerAuthTypeSupport) PlayerAuthTypeSupport.get_instance();
        rtn = ts.register_type(dp, null);
        if (rtn != ReturnCode_t.RETCODE_OK) {
            System.out.println("register type failed");
            return;
        }

        // 3. 创建 Topic
        tp = dp.create_topic(
                "PlayerAuthTopic",
                ts.get_type_name(),
                DomainParticipant.TOPIC_QOS_DEFAULT,
                null,
                StatusKind.STATUS_MASK_NONE
        );
        if (tp == null) {
            System.out.println("create topic failed");
            return;
        }

        // 4. 创建 Publisher
        pub = dp.create_publisher(
                DomainParticipant.PUBLISHER_QOS_DEFAULT,
                null,
                StatusKind.STATUS_MASK_NONE
        );
        if (pub == null) {
            System.out.println("create publisher failed");
            return;
        }

        // 5. 创建 DataWriter
        DataWriter dw = pub.create_datawriter(
                tp,
                Publisher.DATAWRITER_QOS_DEFAULT,
                null,
                StatusKind.STATUS_MASK_NONE
        );
        if (dw == null) {
            System.out.println("create datawriter failed");
            return;
        }
        writer = (PlayerAuthDataWriter) dw;

        System.out.println("用户认证 Publisher 启动，准备发送认证结果...");

        // ===== 示例发送 =====
        sendAuthResult(5231, "Alice", "REGISTER_SUCCESS");
        sendAuthResult(3692, "Bob", "LOGIN_FAIL");

        // ===== 业务逻辑里也可以调用 sendAuthResult(...) =====
    }

    /**
     * 发送用户认证结果
     */
    public static void sendAuthResult(int playerId, String nickname, String result) {
        PlayerAuth msg = new PlayerAuth();
        msg.player_id = playerId;
        msg.nickname = nickname;
        msg.password = "";   // 不传密码
        msg.auth_type = result; // 这里直接放 "LOGIN_SUCCESS"/"REGISTER_FAIL" 等

        ReturnCode_t ret = writer.write(msg, null);
        if (ret.equals(ReturnCode_t.RETCODE_OK)) {
            System.out.println("发送认证结果成功: " + result + " -> " + nickname);
        } else {
            System.out.println("发送认证结果失败: " + ret);
        }
    }

    // 已加载库标识
    private static boolean hasLoad = false;

    // 加载DDS库
    public static void loadLibrary() {
        if (!hasLoad) {
            System.loadLibrary("ZRDDS_JAVA");
            hasLoad = true;
        }
    }
}
