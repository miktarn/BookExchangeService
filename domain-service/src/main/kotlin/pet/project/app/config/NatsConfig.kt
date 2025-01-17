package pet.project.app.config

import io.nats.client.Connection
import io.nats.client.Dispatcher
import io.nats.client.Nats
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class NatsConfig {

    @Value("\${nats.connection-uri}")
    private lateinit var natsConnectionUrl: String

    @Bean
    fun natsConnection(): Connection = Nats.connect(natsConnectionUrl)

    @Bean
    fun createDispatcher(connection: Connection): Dispatcher {
        return connection.createDispatcher()
    }
}
