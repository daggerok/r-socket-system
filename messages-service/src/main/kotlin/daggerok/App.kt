package daggerok

import java.time.Instant
import java.util.Locale
import java.util.TimeZone
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.data.relational.core.mapping.Table
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class App

fun main(args: Array<String>) {
    runApplication<App>(*args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        Locale.setDefault(Locale.US)
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

@RestController
data class MessagesResource(private val messageRepository: MessageRepository) {

    @PostMapping
    fun post(@RequestBody message: Message) =
        messageRepository.save(message)

    @GetMapping
    fun get() =
        messageRepository.findAll()
}
