package daggerok

import java.time.Instant
import java.util.Locale
import java.util.TimeZone
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.ResourceLoader
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.data.relational.core.mapping.Table
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.server.router

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
