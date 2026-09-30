package com.shahbytes.chathub.config;

import com.shahbytes.chathub.messaging.RedisRealtimeSubscriber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisConfig {

    @Bean
    RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            RedisRealtimeSubscriber subscriber,
            @Value("${chathub.messaging.realtime-channel}")
            String channel
    ) {
        var container = new RedisMessageListenerContainer();

        container.setConnectionFactory(connectionFactory);

        container.addMessageListener(subscriber, new ChannelTopic(channel));

        return container;
    }
}
