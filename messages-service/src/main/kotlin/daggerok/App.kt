package daggerok

import io.rsocket.internal.UnboundedProcessor
import java.time.Duration
import java.time.Instant
import java.util.Locale
import java.util.TimeZone
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.data.relational.core.mapping.Table
import org.springframework.http.MediaType
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.server.RequestPredicates.GET
import org.springframework.web.reactive.function.server.RequestPredicates.path
import org.springframework.web.reactive.function.server.RouterFunctions.resources
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Flux

fun main(args: Array<String>) {
    runApplication<App>(*args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        Locale.setDefault(Locale.US)
    }
}

@SpringBootApplication
class App {

    @Bean
    fun routes(/* 1: @Value("classpath:/static/index.html") html: Resource, 3: */resourceLoader: ResourceLoader) = router {
        resources("/", ClassPathResource("classpath:/static/"))
        "/".nest {
            "/api".nest {
                GET("/hello") {
                    ok().bodyValue(mapOf("hello" to "world"))
                }
                path("/**") {
                    val baseUrl = it.uri().run { "$scheme://$authority" }
                    ok().bodyValue(mapOf(
                        "hello _self" to it.uri(),
                        "hello GET" to "$baseUrl/api/hello"
                    ))
                }
            }
            val html = resourceLoader.getResource("classpath:/static/index.html")
            path("/") {
                // 2: val html = ClassPathResource("static/index.html", App::class.java.classLoader) // no classpath prefix!
                ok().contentType(MediaType.TEXT_HTML).bodyValue(html)
            }
        }
    }
}

// @Table("messages")
// data class Message(
//     @Id private val id: UUID = nil,
//     val name: String = "",
//     @CreatedDate val at: Instant = Instant.now(),
// ) : org.springframework.data.domain.Persistable<UUID> {
//
//     @JsonIgnore
//     override fun getId(): UUID? =
//         if (id == nil) null else id
//
//     @JsonIgnore
//     override fun isNew(): Boolean =
//         id == nil
//
//     private companion object {
//         val nil: UUID = UUID.fromString("0-0-0-0-0")
//     }
// }
//
// interface MessageRepository : R2dbcRepository<Message, UUID>

@Table("messages")
data class Message(
    val body: String = "",
    @Id val id: Long? = null,
    @CreatedDate val at: Instant? = Instant.now(),
)

interface MessageRepository : R2dbcRepository<Message, Long>

@Configuration
class UnboundedProcessorConfig {

    @Bean
    fun helloMessagesStream(messageRepository: MessageRepository): Flux<Map<String, Message>> =
        Flux.interval(Duration.ofSeconds(3))
            .map { "Hello $it at ${Instant.now()}" }
            .map { Message(body = it) }
            .flatMap(messageRepository::save)
            .map { mapOf("result" to it) }
            .share()
}

@Controller
class RSocketResource(private val helloMessagesStream: Flux<Map<String, Message>>) {

    @MessageMapping("hello")
    fun hello() =
        helloMessagesStream.doOnNext(log::info)

    private companion object { val log = logger() }
}

@RestController
data class MessagesResource(private val messageRepository: MessageRepository) {

    @PostMapping
    fun post(@RequestBody message: Message) =
        messageRepository.save(message)

    @GetMapping
    fun get() =
        messageRepository.findAll()
}
