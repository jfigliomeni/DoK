package coraythan.keyswap.decks.models

import coraythan.keyswap.House
import coraythan.keyswap.cards.Card
import coraythan.keyswap.expansions.Expansion
import coraythan.keyswap.generatets.GenerateTs
import coraythan.keyswap.stats.DeckStatistics
import coraythan.keyswap.synergy.DeckSynergyInfo

interface GenericDeck {

    val name: String
    val expansion: Int

    val rawAmber: Int
    val totalPower: Int
    val bonusDraw: Int?
    val bonusCapture: Int?
    val creatureCount: Int
    val actionCount: Int
    val artifactCount: Int
    val upgradeCount: Int
    val totalArmor: Int

    val expectedAmber: Double
    val amberControl: Double
    val creatureControl: Double
    val artifactControl: Double
    val efficiency: Double
    val recursion: Double?
    val effectivePower: Int
    val creatureProtection: Double?
    val disruption: Double
    val other: Double
    val aercScore: Double
    val previousSasRating: Int?
    val previousMajorSasRating: Int?
    val aercVersion: Int?
    val sasRating: Int
    val synergyRating: Int
    val antisynergyRating: Int

    // Json of card ids for performance loading decks, loading cards from cache
    val cardIds: String

    val cardNames: String

    val houseNamesString: String

    /**
     * Format: Anger&Brobnar&ACCDM~Cull the Weak&Dis&A
     *
     * A = aember
     * C = capture
     * D = draw
     * M = damage
     */
    val bonusIconsString: String?

    val expansionEnum: Expansion
        get() = Expansion.forExpansionNumber(expansion)

    val houses: List<House>
        get() = this.houseNamesString.split("|").map { House.valueOf(it) }

    fun toDeckSearchResult(
        housesAndCards: List<HouseAndCards>? = null,
        cards: List<Card>? = null,
        stats: DeckStatistics? = null,
        synergies: DeckSynergyInfo? = null,
        includeDetails: Boolean = false
    ): DeckSearchResult

}

@GenerateTs
enum class DeckType {
    STANDARD,
    ALLIANCE
}
