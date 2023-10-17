package coraythan.keyswap.userdeck

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import coraythan.keyswap.decks.models.Deck
import coraythan.keyswap.users.KeyUser
import jakarta.persistence.*
import org.springframework.data.repository.CrudRepository
import java.util.*

@Entity
data class FavoritedDeck(

        @ManyToOne
        val user: KeyUser,

        @JsonIgnoreProperties("favoritedDecks")
        @ManyToOne
        val deck: Deck,

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO, generator = "hibernate_sequence")
        @SequenceGenerator(name = "hibernate_sequence", allocationSize = 1)
        val id: Long = -1
)

interface FavoritedDeckRepo : CrudRepository<FavoritedDeck, Long> {
        fun existsByDeckIdAndUserId(deckId: Long, userId: UUID): Boolean
        fun deleteByDeckIdAndUserId(deckId: Long, userId: UUID)
}
