package com.snakegame.dds.Subscriber;

import com.snakegame.dds.SnakeGame.PlayerAuth;
import com.snakegame.dds.SnakeGame.PlayerAuthDataReader;
import com.snakegame.dds.SnakeGame.PlayerAuthSeq;
import com.snakegame.dds.SnakeGame.PlayerAuthTypeSupport;
import com.zrdds.domain.DomainParticipant;
import com.zrdds.domain.DomainParticipantFactory;
import com.zrdds.infrastructure.*;
import com.zrdds.subscription.DataReader;
import com.zrdds.subscription.DataReaderListener;
import com.zrdds.subscription.Subscriber;
import com.zrdds.topic.Topic;

/**
 * 用户认证后端订阅者（使用监听器接收 PlayerAuth 消息）
 */
public class PlayerAuthSubscriberByListener {
    public static void main(String[] args) {
        loadLibrary();
        ReturnCode_t rtn;

        int domain_id = 80;

        // 1. 创建 DomainParticipant
        DomainParticipant dp = DomainParticipantFactory.get_instance().create_participant(
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
        Topic tp = dp.create_topic(
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

        // 4. 创建 Subscriber
        Subscriber sub = dp.create_subscriber(
                DomainParticipant.SUBSCRIBER_QOS_DEFAULT,
                null,
                StatusKind.STATUS_MASK_NONE
        );
        if (sub == null) {
            System.out.println("create sub failed");
            return;
        }

        // 5. 创建监听器
        PlayerAuthDataReaderListener listener = new PlayerAuthDataReaderListener();

        // 6. 创建 DataReader，并绑定监听器
        DataReader dr = sub.create_datareader(
                tp,
                Subscriber.DATAREADER_QOS_DEFAULT,
                listener,
                StatusKind.DATA_AVAILABLE_STATUS
        );
        if (dr == null) {
            System.out.println("create datareader failed");
            return;
        }

        System.out.println("用户认证后端启动，等待客户端登录/注册请求...");

        // 主线程阻塞，等待消息
        while (true) {
            tSleep(5000);
        }
    }

    // 等待接口
    public static void tSleep(int ti) {
        try {
            Thread.sleep(ti);
        } catch (InterruptedException e) {
            e.printStackTrace();
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

/**
 * PlayerAuth DataReader 的监听器实现
 */
class PlayerAuthDataReaderListener implements DataReaderListener {
    @Override
    public void on_data_available(DataReader dataReader) {
        PlayerAuthDataReader dr = (PlayerAuthDataReader) dataReader;
        PlayerAuthSeq dataSeq = new PlayerAuthSeq();
        SampleInfoSeq infoSeq = new SampleInfoSeq();

        ReturnCode_t rtn = dr.take(
                dataSeq,
                infoSeq,
                -1,
                SampleStateKind.ANY_SAMPLE_STATE,
                ViewStateKind.ANY_VIEW_STATE,
                InstanceStateKind.ANY_INSTANCE_STATE
        );

        if (rtn != ReturnCode_t.RETCODE_OK) {
            System.out.println("take data failed");
            return;
        }

        for (int i = 0; i < infoSeq.length(); ++i) {
            if (!infoSeq.get_at(i).valid_data) {
                continue;
            }

            PlayerAuth auth = dataSeq.get_at(i);
            System.out.println("收到用户认证请求:");
            System.out.println("  player_id: " + auth.player_id);
            System.out.println("  nickname: " + auth.nickname);
            System.out.println("  auth_type: " + auth.auth_type);

            // TODO 调用业务层处理逻辑
            // UserService.handleAuth(auth);
        }

        rtn = dr.return_loan(dataSeq, infoSeq);
        if (rtn != ReturnCode_t.RETCODE_OK) {
            System.out.println("return loan failed");
        }
    }

    // 下面几个回调可以根据需要实现，这里简单留空
    public void on_liveliness_changed(DataReader arg0, LivelinessChangedStatus arg1) {}
    public void on_requested_deadline_missed(DataReader arg0, RequestedDeadlineMissedStatus arg1) {}
    public void on_requested_incompatible_qos(DataReader arg0, RequestedIncompatibleQosStatus arg1) {}
    public void on_sample_lost(DataReader arg0, SampleLostStatus arg1) {}
    public void on_sample_rejected(DataReader arg0, SampleRejectedStatus arg1) {}
    public void on_subscription_matched(DataReader arg0, SubscriptionMatchedStatus arg1) {}
    public void on_data_arrived(DataReader arg0, Object arg1, SampleInfo arg2) {}
}
