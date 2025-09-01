package com.snakegame.dds;

import com.zrdds.topic.TypeSupport;
import com.zrdds.infrastructure.StatusKind;
import com.zrdds.domain.DomainParticipant;
import com.zrdds.domain.DomainParticipantFactory;
import com.zrdds.publication.Publisher;
import com.zrdds.subscription.Subscriber;
import com.zrdds.topic.Topic;


public class DDSConfig {

    // 创建 DomainParticipant
    public static DomainParticipant createParticipant(int domainId) {
        return DomainParticipantFactory.get_instance().create_participant(
                domainId,
                DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT,
                null,
                StatusKind.STATUS_MASK_NONE
        );
    }

    // 创建 Publisher
    public static Publisher createPublisher(DomainParticipant participant) {
        return participant.create_publisher(
                DomainParticipant.PUBLISHER_QOS_DEFAULT,
                null,
                StatusKind.STATUS_MASK_NONE
        );
    }

    // 创建 Subscriber
    public static Subscriber createSubscriber(DomainParticipant participant) {
        return participant.create_subscriber(
                DomainParticipant.SUBSCRIBER_QOS_DEFAULT,
                null,
                StatusKind.STATUS_MASK_NONE
        );
    }

    // 注册类型 & 创建 Topic
    public static Topic createTopic(DomainParticipant participant, String typeName, String topicName, TypeSupport ts) {
        ts.register_type(participant, typeName);
        return participant.create_topic(
                topicName,
                typeName,
                DomainParticipant.TOPIC_QOS_DEFAULT,
                null,
                StatusKind.STATUS_MASK_NONE
        );
    }
}
