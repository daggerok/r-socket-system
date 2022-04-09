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

@Table("users")
data class User(
    val name: String = "",
    @Id val id: Long? = null,
    @CreatedDate val createdAt: Instant? = Instant.now(),
)

interface UserRepository : R2dbcRepository<User, Long>

@RestController
data class UserResource(private val userRepository: UserRepository) {

    @PostMapping
    fun post(@RequestBody message: User) =
        userRepository.save(message)

    @GetMapping
    fun get() =
        userRepository.findAll()
}
