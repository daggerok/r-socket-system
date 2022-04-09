package daggerok

import org.apache.logging.log4j.kotlin.logger
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import reactor.test.StepVerifier

@TestInstance(PER_CLASS)
@SpringBootTest(webEnvironment = RANDOM_PORT)
class AppTests @Autowired constructor(val messageRepository: MessageRepository) {

    @BeforeEach
    fun setUp() {
        messageRepository.findAll()
            .doOnNext(log::info)
            .flatMap { messageRepository.delete(it) }
            .subscribe(log::info)
    }

    @Test
    fun `should save`() {
        // when
        messageRepository.save(Message(body = "ololo"))
            .subscribe(log::info)

        // then
        StepVerifier.create(messageRepository.findAll())
            .consumeNextWith { log.info { it } }
            .verifyComplete()
    }

    companion object { val log = logger() }
}
