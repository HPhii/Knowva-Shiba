package com.example.demo.config;

import com.example.demo.model.io.dto.EmailMessage;
import com.example.demo.model.io.dto.ImageUploadMessage;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${spring.kafka.topic.dlq}")
    private String dlqTopic;

    @Value("${spring.kafka.topic.cloudinary-upload}")
    private String cloudinaryUploadTopic;

    @Value("${spring.kafka.topic.cloudinary-upload-dlq}")
    private String cloudinaryUploadDlqTopic;

    // ==================== CẤU HÌNH CHUNG ====================

    @Bean
    public KafkaTemplate<String, Object> genericKafkaTemplate() {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class,
                JsonSerializer.ADD_TYPE_INFO_HEADERS, false
        )));
    }


    // ==================== CẤU HÌNH CHO EMAIL ====================

    @Bean
    public NewTopic emailTopic() {
        return TopicBuilder.name("email_events").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic dlqEmailTopic() {
        return TopicBuilder.name(dlqTopic).partitions(1).replicas(1).build();
    }

    @Bean
    public ProducerFactory<String, EmailMessage> emailProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, EmailMessage> emailKafkaTemplate() {
        return new KafkaTemplate<>(emailProducerFactory());
    }

    @Bean
    public ConsumerFactory<String, EmailMessage> emailConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, EmailMessage.class);
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(EmailMessage.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EmailMessage> kafkaListenerContainerFactory(
            @Qualifier("genericKafkaTemplate") KafkaTemplate<String, Object> genericKafkaTemplate) {
        ConcurrentKafkaListenerContainerFactory<String, EmailMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(emailConsumerFactory());
        factory.setCommonErrorHandler(new DefaultErrorHandler(
                new DeadLetterPublishingRecoverer(genericKafkaTemplate),
                new FixedBackOff(2000L, 2)
        ));
        return factory;
    }


    // ==================== CẤU HÌNH CHO UPLOAD ẢNH ====================

    @Bean
    public NewTopic cloudinaryUploadTopic() {
        return TopicBuilder.name(cloudinaryUploadTopic).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic dlqCloudinaryUploadTopic() {
        return TopicBuilder.name(cloudinaryUploadDlqTopic).partitions(1).replicas(1).build();
    }

    @Bean
    public ProducerFactory<String, ImageUploadMessage> imageUploadProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, ImageUploadMessage> imageUploadKafkaTemplate() {
        return new KafkaTemplate<>(imageUploadProducerFactory());
    }

    @Bean
    public ConsumerFactory<String, ImageUploadMessage> imageUploadConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "cloudinary_group");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(ImageUploadMessage.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ImageUploadMessage> imageUploadListenerContainerFactory(
            @Qualifier("genericKafkaTemplate") KafkaTemplate<String, Object> genericKafkaTemplate) {
        ConcurrentKafkaListenerContainerFactory<String, ImageUploadMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(imageUploadConsumerFactory());
        factory.setCommonErrorHandler(new DefaultErrorHandler(
                new DeadLetterPublishingRecoverer(genericKafkaTemplate),
                new FixedBackOff(2000L, 2)
        ));
        return factory;
    }
}