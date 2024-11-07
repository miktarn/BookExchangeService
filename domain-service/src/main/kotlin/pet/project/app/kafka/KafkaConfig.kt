package pet.project.app.kafka

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pet.project.internal.app.topic.KafkaTopic
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions

@Configuration
class KafkaConfig(
    @Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: String,
    @Value("\${spring.kafka.consumer.auto-offset-reset}") private val autoOffsetReset: String,
) {

    private val commonConfig = mapOf(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers)

    @Bean
    fun kafkaBookAmountIncreasedReceiver(): KafkaReceiver<String, ByteArray> {
        val config = commonConfig + mapOf(
            ConsumerConfig.GROUP_ID_CONFIG to CONSUMER_GROUP_ID,
            ConsumerConfig.CLIENT_ID_CONFIG to CONSUMER_CLIENT_ID,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to autoOffsetReset,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to ByteArrayDeserializer::class.java,
        )
        val options = ReceiverOptions.create<String, ByteArray>(config)
            .subscription(setOf(KafkaTopic.Book.AMOUNT_INCREASED))
        return KafkaReceiver.create(options)
    }

    @Bean
    fun kafkaBookAmountIncreasedSender(): KafkaSender<String, ByteArray> {
        val config = commonConfig + mapOf(
            ProducerConfig.CLIENT_ID_CONFIG to PRODUCER_CLIENT_ID,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to ByteArraySerializer::class.java
        )
        val senderOptions = SenderOptions.create<String, ByteArray>(config)
        return KafkaSender.create(senderOptions)
    }

    companion object {
        private const val CONSUMER_GROUP_ID = "book-amount-increased-consumer-group"
        private const val CONSUMER_CLIENT_ID = "book-amount-increased-consumer"
        private const val PRODUCER_CLIENT_ID = "book-amount-increased-producer"
    }
}
