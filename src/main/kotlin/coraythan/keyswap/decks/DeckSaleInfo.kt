package coraythan.keyswap.decks

import coraythan.keyswap.userdeck.DeckCondition
import java.time.LocalDate

data class DeckSaleInfo(
        val forSale: Boolean,
        val forTrade: Boolean,

        val askingPrice: Double?,

        val listingInfo: String?,
        val externalLink: String?,

        val condition: DeckCondition,

        val dateListed: LocalDate,
        val dateExpires: LocalDate,

        val username: String,
        val publicContactInfo: String?
)