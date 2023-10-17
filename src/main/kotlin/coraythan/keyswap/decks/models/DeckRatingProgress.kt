package coraythan.keyswap.decks.models

import coraythan.keyswap.synergy.publishsas.PublishedSasVersionService
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.slf4j.LoggerFactory
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.util.*

@Entity
data class DeckRatingProgress(

    val version: Int,

    val currentPage: Int = 0,

    val completeDateTime: ZonedDateTime? = null,

    @Id
    val id: UUID = UUID.randomUUID()
)

interface DeckRatingProgressRepo : CrudRepository<DeckRatingProgress, UUID> {
    fun findByVersion(version: Int): DeckRatingProgress?
}

var doneRatingDecks: Boolean = true

@Service
class DeckRatingProgressService(
    private val repo: DeckRatingProgressRepo,
    private val publishedSasVersionService: PublishedSasVersionService,
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    fun nextPage(): Int? {
        val publishedAercVersion = publishedSasVersionService.latestSasVersion()
        val progress = repo.findByVersion(publishedAercVersion) ?: repo.save(DeckRatingProgress(publishedAercVersion))
        return if (progress.completeDateTime == null) {
            doneRatingDecks = false
            progress.currentPage
        } else {
            doneRatingDecks = true
            null
        }
    }

    fun revPage() {
        val publishedAercVersion = publishedSasVersionService.latestSasVersion()
        val preexisting = repo.findByVersion(publishedAercVersion)
        if (preexisting != null) {
            val nextPage = preexisting.currentPage + 1
            log.info("Next deck rating page: $nextPage")
            repo.save(preexisting.copy(currentPage = nextPage))
        }
    }

    fun complete() {
        val publishedAercVersion = publishedSasVersionService.latestSasVersion()
        doneRatingDecks = true
        val preexisting = repo.findByVersion(publishedAercVersion)
        if (preexisting != null) {
            repo.save(preexisting.copy(completeDateTime = ZonedDateTime.now()))
        }
    }
}