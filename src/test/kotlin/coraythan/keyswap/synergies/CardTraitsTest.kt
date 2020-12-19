package coraythan.keyswap.synergies

import coraythan.keyswap.House
import coraythan.keyswap.cards.Card
import coraythan.keyswap.cards.CardType
import coraythan.keyswap.cards.ExtraCardInfo
import coraythan.keyswap.synergy.DeckSynergyService
import coraythan.keyswap.synergy.SynTraitValue
import coraythan.keyswap.synergy.SynergyTrait
import org.junit.Assert.assertEquals
import org.junit.Test
import org.slf4j.LoggerFactory

class CardTraitsTest {

    private val log = LoggerFactory.getLogger(this::class.java)

    val queenCards = listOf<Card>()
            .plus(
                    basicCard().copy(
                            id = "niffle ape",
                            house = House.Untamed,
                            traits = setOf("Niffle"),
                            cardTitle = "Niffle Ape",
                            extraCardInfo = ExtraCardInfo(
                                    effectivePower = 3
                            )
                    )
            )
            .plus(
                    basicCard().copy(
                            id = "snufflegator",
                            house = House.Untamed,
                            cardTitle = "Snufflegator",
                            traits = setOf("BEAST"),
                            extraCardInfo = ExtraCardInfo(
                                    effectivePower = 4
                            )
                    )
            )
            .plus((0..1).map {
                basicCard().copy(
                        id = "niffle queen",
                        house = House.Untamed,
                        traits = setOf("NIFFLE"),
                        cardTitle = "Niffle Queen",
                        extraCardInfo = ExtraCardInfo(
                                effectivePower = 6,
                                effectivePowerMax = 12.0,
                                synergies = listOf(
                                        SynTraitValue(SynergyTrait.any, 3, cardTraitsString = "NIFFLE"),
                                        SynTraitValue(SynergyTrait.any, 3, cardTraitsString = "BEAST")
                                )
                        )
                )
            })

    @Test
    fun testNiffles() {

        val niffleResults = DeckSynergyService.fromDeckWithCards(boringDeck, queenCards)
        assertEquals(3, niffleResults.synergyCombos.size)
        assertEquals(1, niffleResults.synergyRating)
    }

    val troopCallCards = listOf<Card>()
            .plus(
                    basicCard().copy(
                            id = "niffle ape",
                            house = House.Untamed,
                            traits = setOf("NIFFLE"),
                            cardTitle = "niffle ape",
                            cardType = CardType.Creature,
                            extraCardInfo = ExtraCardInfo(
                                    effectivePower = 3
                            )
                    )
            )
            .plus(
                    basicCard().copy(
                            id = "dust pixie",
                            house = House.Untamed,
                            cardTitle = "dust pixie",
                            cardType = CardType.Creature,
                            traits = setOf("FAERIE"),
                            extraCardInfo = ExtraCardInfo(
                                    effectivePower = 1,
                                    expectedAmber = 0.0,
                                    expectedAmberMax = 4.0,
                                    synergies = listOf(
                                            SynTraitValue(SynergyTrait.replays, 3, cardTypesInitial = listOf(CardType.Creature))
                                    )
                            )
                    )
            )
            .plus(
                    basicCard().copy(
                            id = "troop call",
                            house = House.Untamed,
                            cardTitle = "troop call",
                            cardType = CardType.Action,
                            extraCardInfo = ExtraCardInfo(
                                    other = 0.0,
                                    otherMax = 4.0,
                                    traits = listOf(
                                            SynTraitValue(SynergyTrait.replays, 3, cardTraitsString = "NIFFLE", cardTypesInitial = listOf(CardType.Creature)),
                                    ),
                                    synergies = listOf(
                                            SynTraitValue(SynergyTrait.any, 3, cardTraitsString = "NIFFLE", cardTypesInitial = listOf(CardType.Creature)),
                                    )
                            )
                    )
            )

    @Test
    fun testTroopCall() {
        val troopResults = DeckSynergyService.fromDeckWithCards(boringDeck, troopCallCards)
        assertEquals(0.0, troopResults.synergyCombos.find { combo -> combo.cardName == "dust pixie" }?.expectedAmber ?: -100.0, 0.001)
        assertEquals(1.0, troopResults.synergyCombos.find { combo -> combo.cardName == "troop call" }?.other ?: -100.0, 0.001)
        assertEquals(1, troopResults.synergyRating)
    }

}
