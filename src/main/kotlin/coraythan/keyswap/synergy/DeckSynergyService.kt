package coraythan.keyswap.synergy

import com.google.common.math.DoubleMath.roundToInt
import coraythan.keyswap.House
import coraythan.keyswap.cards.Card
import coraythan.keyswap.cards.CardType
import coraythan.keyswap.decks.models.Deck
import org.slf4j.LoggerFactory
import java.math.RoundingMode
import kotlin.math.absoluteValue

object DeckSynergyService {
    private val log = LoggerFactory.getLogger(this::class.java)

    private fun ratingsToPercent(synRating: Int, traitStrength: TraitStrength): Int {
        return (if (synRating < 0) -1 else 1) * when (synRating.absoluteValue + traitStrength.value) {
            2 -> 2
            3 -> 5
            4 -> 10
            5 -> 15
            6 -> 25
            7 -> 33
            8 -> 50
            else -> {
                log.warn("Bad ratings! $synRating $traitStrength")
                0
            }
        }
    }

    private fun synergizedValue(totalSynPercent: Int, min: Double, max: Double?, hasPositive: Boolean, hasNegative: Boolean): SynergizedValue {
        return if (max.isZeroOrNull()) {
            SynergizedValue(min, 0.0)
        } else {
            val range = max!! - min

            // Divide by 200 if positive + negative so that 100% positive 0% negative maxes out synergy
            val synValue = (totalSynPercent * range) / (if (hasPositive && hasNegative) 200 else 100)
            val startingPoint = when {
                hasPositive && hasNegative -> (range / 2) + min
                hasPositive -> min
                else -> max
            }
            val uncappedValue = synValue + startingPoint
            val value = when {
                uncappedValue < min -> min
                uncappedValue > max -> max
                else -> uncappedValue
            }
            SynergizedValue(value, value - startingPoint)
        }
    }

    fun fromDeckWithCards(deck: Deck, cards: List<Card>): DeckSynergyInfo {

        val traitsMap = mutableMapOf<SynergyTrait, SynTraitValuesForTrait>()

        val cardsMap: Map<House, Map<String, Int>> = cards
                .groupBy { it.house }
                .map { cardsByHouse -> cardsByHouse.key to cardsByHouse.value.groupBy { it.cardTitle }.map { it.key to it.value.size }.toMap() }
                .toMap()

        // Add traits from each card
        cards.forEach { card ->
            val cardInfo = card.extraCardInfo!!
            val cardSpecialTraits = card.traits.mapNotNull {
                val trait = SynergyTrait.fromTrait(it)
                if (trait == null) null else SynTraitValue(trait)
            }
            val cardAllTraits = cardInfo.traits.plus(cardSpecialTraits)
            cardAllTraits
                    .forEach { traitValue ->
                        traitsMap.addTrait(traitValue, card.cardTitle, card.house)
                    }
        }

        // log.info("Traits map is: ${ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(traitsMap)}")

        addDeckTraits(deck, traitsMap, cards)
        addHouseTraits(deck.houses, cards, traitsMap)
        addOutOfHouseTraits(deck.houses, cards, traitsMap)
        val synergyCombos: List<SynergyCombo> = cards
                .groupBy { Pair(it.cardTitle, it.house) }
                .map { cardsById ->
                    val count = cardsById.value.size
                    val card = cardsById.value[0]
                    val cardInfo = card.extraCardInfo ?: error("Oh no, ${card.cardTitle} had null extra info! $card")

                    val matchedTraits: List<SynergyMatch> = cardInfo.synergies.map { synergy ->

                        val synergyTrait = synergy.trait
                        val cardName = synergy.cardName
                        val cardNames = mutableSetOf<String>()
                        val synPercent = TraitStrength.values().map { strength ->
                            val matches = if (synergy.cardName == null) {
                                traitsMap[synergyTrait]?.matches(card.cardTitle, card.house, synergy)
                            } else {
                                val cardCount = when (synergy.house) {
                                    SynTraitHouse.anyHouse -> cardsMap.map { it.value.getOrDefault(synergy.cardName, 0) }.sum()
                                    SynTraitHouse.house -> cardsMap[card.house]?.get(synergy.cardName) ?: 0
                                    else -> cardsMap.entries.filter { it.key != card.house }.map { it.value.getOrDefault(synergy.cardName, 0) }.sum()
                                }
                                if (cardCount != 0) {
                                    SynMatchInfo(mapOf(TraitStrength.NORMAL to if (card.cardTitle == synergy.cardName) cardCount - 1 else cardCount))
                                } else {
                                    null
                                }
                            }
                            if (matches == null) {
                                0
                            } else {
                                cardNames.addAll(matches.cardNames)
                                val matchesAtStrength = matches.matches[strength] ?: 0
                                // log.info("${card.cardTitle}: $synergyTrait syn rating: ${synergy.rating} $strength = $matchesAtStrength")
                                matchesAtStrength * ratingsToPercent(synergy.rating, strength)
                            }
                        }.sum()

                        SynergyMatch(synergyTrait, synPercent, cardNames, synergy.rating, synergy.house, cardName)
                    }

                    val totalSynPercent = matchedTraits.map { it.percentSynergized }.sum()

                    val hasPositive = cardInfo.synergies.find { it.rating > 0 } != null
                    val hasNegative = cardInfo.synergies.find { it.rating < 0 } != null

                    val aValue = synergizedValue(totalSynPercent, cardInfo.amberControl, cardInfo.amberControlMax, hasPositive, hasNegative)
                    val eValue = synergizedValue(totalSynPercent, cardInfo.expectedAmber, cardInfo.expectedAmberMax, hasPositive, hasNegative)
                    // log.info("For card ${card.cardTitle} e value is $eValue expected aember ${cardInfo.expectedAmber}")
                    val rValue = synergizedValue(totalSynPercent, cardInfo.artifactControl, cardInfo.artifactControlMax, hasPositive, hasNegative)
                    val cValue = synergizedValue(totalSynPercent, cardInfo.creatureControl, cardInfo.creatureControlMax, hasPositive, hasNegative)
                    val fValue = synergizedValue(totalSynPercent, cardInfo.efficiency, cardInfo.efficiencyMax, hasPositive, hasNegative)
                    val pValue = if (cardInfo.effectivePower == 0 && (cardInfo.effectivePowerMax == null || cardInfo.effectivePowerMax == 0.0)) {
                        SynergizedValue(card.effectivePower.toDouble(), 0.0)
                    } else {
                        synergizedValue(totalSynPercent, cardInfo.effectivePower.toDouble(), cardInfo.effectivePowerMax, hasPositive, hasNegative)
                    }
                    val dValue = synergizedValue(totalSynPercent, cardInfo.disruption, cardInfo.disruptionMax, hasPositive, hasNegative)
                    val hcValue = synergizedValue(totalSynPercent, cardInfo.houseCheating, cardInfo.houseCheatingMax, hasPositive, hasNegative)
                    val apValue = synergizedValue(totalSynPercent, cardInfo.amberProtection, cardInfo.amberProtectionMax, hasPositive, hasNegative)
                    val oValue = synergizedValue(totalSynPercent, cardInfo.other, cardInfo.otherMax, hasPositive, hasNegative)

                    val synergizedValues = listOf(
                            aValue,
                            eValue,
                            rValue,
                            cValue,
                            fValue,
                            pValue.copy(
                                    value = (pValue.value / 10).toBigDecimal().setScale(1, RoundingMode.HALF_UP).toDouble(),
                                    synergy = (pValue.synergy / 10).toBigDecimal().setScale(1, RoundingMode.HALF_UP).toDouble()
                            ),
                            dValue,
                            hcValue,
                            apValue,
                            oValue
                    )
                    val synergyValues = synergizedValues.map { it.synergy }

                    SynergyCombo(
                            house = card.house,
                            cardName = card.cardTitle,
                            synergies = matchedTraits,
                            netSynergy = synergyValues.sum(),
                            aercScore = synergizedValues.map { it.value }.sum() + (if (card.cardType == CardType.Creature) 0.4 else 0.0),

                            amberControl = aValue.value,
                            expectedAmber = eValue.value,
                            artifactControl = rValue.value,
                            creatureControl = cValue.value,
                            efficiency = fValue.value,
                            effectivePower = pValue.value.toInt(),

                            disruption = dValue.value,
                            houseCheating = hcValue.value,
                            amberProtection = apValue.value,
                            other = oValue.value,
                            copies = count
                    )
                }

        val a = synergyCombos.map { it.amberControl * it.copies }.sum()
        val e = synergyCombos.map { it.expectedAmber * it.copies }.sum()
        val r = synergyCombos.map { it.artifactControl * it.copies }.sum()
        val c = synergyCombos.map { it.creatureControl * it.copies }.sum()
        val f = synergyCombos.map { it.efficiency * it.copies }.sum()
        val d = synergyCombos.map { it.disruption * it.copies }.sum()
        val p = synergyCombos.map { it.effectivePower * it.copies }.sum()
        val o = synergyCombos.map { it.other * it.copies }.sum()
        val ap = synergyCombos.map { it.amberProtection * it.copies }.sum()
        val hc = synergyCombos.map { it.houseCheating * it.copies }.sum()

        val creatureCount = cards.filter { it.cardType == CardType.Creature }.size
        val powerValue = p / 10
        // Remember! When updating this also update Card
        val synergy = roundToInt(synergyCombos.filter { it.netSynergy > 0 }.map { it.netSynergy * it.copies }.sum(), RoundingMode.HALF_UP)
        val antiSynergyToRound = synergyCombos.filter { it.netSynergy < 0 }.map { it.netSynergy * it.copies }.sum()
        val antisynergy = roundToInt(antiSynergyToRound, RoundingMode.HALF_UP).absoluteValue
        val newSas = roundToInt(a + e + r + c + f + d + ap + hc + o + powerValue + (creatureCount.toDouble() * 0.4), RoundingMode.HALF_UP)
        val rawAerc = newSas + antisynergy - synergy

        val info = DeckSynergyInfo(
                synergyRating = synergy,
                antisynergyRating = antisynergy,
                synergyCombos = synergyCombos.sortedByDescending { it.netSynergy },
                rawAerc = rawAerc,
                sasRating = newSas,

                amberControl = a,
                expectedAmber = e,
                artifactControl = r,
                creatureControl = c,
                efficiency = f,
                effectivePower = p,
                disruption = d,
                amberProtection = ap,
                houseCheating = hc,
                other = o
        )

        // log.info("a: $a e $e r $r c $c f $f p $powerValue d $d ap $ap hc $hc o $o creature count ${(creatureCount.toDouble() * 0.4)} $newSas")

        return info

    }

    private fun addOutOfHouseTraits(houses: List<House>, cards: List<Card>, traits: MutableMap<SynergyTrait, SynTraitValuesForTrait>) {
        houses.forEach { house ->
            val cardsNotForHouse = cards.filter { it.house != house }
            val creatureCount = cardsNotForHouse.filter { it.cardType == CardType.Creature }.size

            if (creatureCount > 12) traits.addDeckTrait(SynergyTrait.highCreatureCount, when {
                creatureCount > 16 -> 4
                creatureCount > 15 -> 3
                creatureCount > 14 -> 2
                else -> 1
            }, house, SynTraitHouse.outOfHouse)

            if (creatureCount < 10) traits.addDeckTrait(SynergyTrait.lowCreatureCount, when {
                creatureCount < 6 -> 4
                creatureCount < 7 -> 3
                creatureCount < 8 -> 2
                else -> 1
            }, house, SynTraitHouse.outOfHouse)
        }
    }

    private fun addHouseTraits(houses: List<House>, cards: List<Card>, traits: MutableMap<SynergyTrait, SynTraitValuesForTrait>) {
        houses.forEach { house ->
            val cardsForHouse = cards.filter { it.house == house }
            val totalCreaturePower = cardsForHouse.map { it.power }.sum()
            val creatureCount = cardsForHouse.filter { it.cardType == CardType.Creature }.size
            val artifactCount = cardsForHouse.filter { it.cardType == CardType.Artifact }.size
            val upgradeCount = cardsForHouse.filter { it.cardType == CardType.Upgrade }.size
            val totalExpectedAmber = cardsForHouse.map {
                val max = it.extraCardInfo?.expectedAmberMax ?: 0.0
                val min = it.extraCardInfo?.expectedAmber ?: 0.0
                if (max == 0.0) min else (min + max) / 2
            }.sum()
            val totalArmor = cardsForHouse.map { it.armor }.sum()

            if (totalExpectedAmber > 7) traits.addDeckTrait(SynergyTrait.highExpectedAmber, when {
                totalExpectedAmber > 10 -> 4
                totalExpectedAmber > 9 -> 3
                totalExpectedAmber > 8 -> 2
                else -> 1
            }, house, SynTraitHouse.house)
            if (totalExpectedAmber < 7) traits.addDeckTrait(SynergyTrait.lowExpectedAmber, when {
                totalExpectedAmber < 4 -> 4
                totalExpectedAmber < 5 -> 3
                totalExpectedAmber < 6 -> 2
                else -> 1
            }, house, SynTraitHouse.house)

            if (totalCreaturePower > 21) traits.addDeckTrait(SynergyTrait.highTotalCreaturePower, when {
                totalCreaturePower > 23 -> 4
                totalCreaturePower > 25 -> 3
                totalCreaturePower > 27 -> 2
                else -> 1
            }, house, SynTraitHouse.house)

            if (totalCreaturePower < 20) traits.addDeckTrait(SynergyTrait.lowTotalCreaturePower, when {
                totalCreaturePower < 14 -> 4
                totalCreaturePower < 16 -> 3
                totalCreaturePower < 18 -> 2
                else -> 1
            }, house, SynTraitHouse.house)

            if (upgradeCount > 0) traits.addDeckTrait(SynergyTrait.upgradeCount, when {
                upgradeCount > 3 -> 4
                upgradeCount > 2 -> 3
                upgradeCount > 1 -> 2
                else -> 1
            }, house, SynTraitHouse.house)

            if (creatureCount > 6) traits.addDeckTrait(SynergyTrait.highCreatureCount, when {
                creatureCount > 9 -> 4
                creatureCount > 8 -> 3
                creatureCount > 7 -> 2
                else -> 1
            }, house, SynTraitHouse.house)

            if (creatureCount < 6) traits.addDeckTrait(SynergyTrait.lowCreatureCount, when {
                creatureCount < 3 -> 4
                creatureCount < 4 -> 3
                creatureCount < 5 -> 2
                else -> 1
            }, house, SynTraitHouse.house)

            if (artifactCount > 2) traits.addDeckTrait(SynergyTrait.highArtifactCount, when {
                artifactCount > 3 -> 4
                else -> 2
            }, house, SynTraitHouse.house)

            if (artifactCount < 2) traits.addDeckTrait(SynergyTrait.lowArtifactCount, when {
                artifactCount < 1 -> 4
                else -> 2
            }, house, SynTraitHouse.house)

            if (totalArmor > 1) traits.addDeckTrait(SynergyTrait.highTotalArmor, when {
                totalArmor > 5 -> 4
                totalArmor > 4 -> 3
                totalArmor > 3 -> 2
                else -> 1
            }, house, SynTraitHouse.house)
        }
    }

    private fun addDeckTraits(deck: Deck, traits: MutableMap<SynergyTrait, SynTraitValuesForTrait>, cards: List<Card>) {

        if (deck.houses.contains(House.Mars)) traits.addDeckTrait(SynergyTrait.hasMars, 4)

        val totalExpectedAmber = cards.map { it.extraCardInfo?.expectedAmber ?: 0.0 }.sum()
        if (totalExpectedAmber > 21) traits.addDeckTrait(SynergyTrait.highExpectedAmber, when {
            totalExpectedAmber > 26 -> 4
            totalExpectedAmber > 25 -> 3
            totalExpectedAmber > 23 -> 2
            else -> 1
        })
        if (totalExpectedAmber < 19) traits.addDeckTrait(SynergyTrait.lowExpectedAmber, when {
            totalExpectedAmber < 15 -> 4
            totalExpectedAmber < 17 -> 3
            totalExpectedAmber < 18 -> 2
            else -> 1
        })

        if (deck.totalPower < 60) traits.addDeckTrait(SynergyTrait.lowTotalCreaturePower, when {
            deck.totalPower < 47 -> 4
            deck.totalPower < 52 -> 3
            deck.totalPower < 57 -> 2
            else -> 1
        })
        if (deck.totalPower > 67) traits.addDeckTrait(SynergyTrait.highTotalCreaturePower, when {
            deck.totalPower > 83 -> 4
            deck.totalPower > 77 -> 3
            deck.totalPower > 72 -> 2
            else -> 1
        })

        if (deck.totalArmor > 3) traits.addDeckTrait(SynergyTrait.highTotalArmor, when {
            deck.totalArmor > 8 -> 4
            deck.totalArmor > 6 -> 3
            deck.totalArmor > 4 -> 2
            else -> 1
        })

        if (deck.artifactCount > 4) traits.addDeckTrait(SynergyTrait.highArtifactCount, when {
            deck.artifactCount > 7 -> 4
            deck.artifactCount > 6 -> 3
            deck.artifactCount > 5 -> 2
            else -> 1
        })

        if (deck.artifactCount < 4) traits.addDeckTrait(SynergyTrait.lowArtifactCount, when {
            deck.artifactCount < 1 -> 4
            deck.artifactCount < 2 -> 3
            deck.artifactCount < 3 -> 2
            else -> 1
        })

        if (deck.upgradeCount > 0) traits.addDeckTrait(SynergyTrait.upgradeCount, when {
            deck.upgradeCount > 3 -> 4
            deck.upgradeCount > 2 -> 3
            deck.upgradeCount > 1 -> 2
            else -> 1
        })

        if (deck.creatureCount > 16) traits.addDeckTrait(SynergyTrait.highCreatureCount, when {
            deck.creatureCount > 20 -> 4
            deck.creatureCount > 18 -> 3
            deck.creatureCount > 17 -> 2
            else -> 1
        })

        if (deck.creatureCount < 15) traits.addDeckTrait(SynergyTrait.lowCreatureCount, when {
            deck.creatureCount < 12 -> 4
            deck.creatureCount < 13 -> 3
            deck.creatureCount < 14 -> 2
            else -> 1
        })

        val power1 = cards.filter { it.cardType == CardType.Creature && it.power == 1 }.size
        val power2OrLower = cards.filter { it.cardType == CardType.Creature && it.power < 3 }.size
        val power3OrLower = cards.filter { it.cardType == CardType.Creature && it.power < 4 }.size
        val power3OrHigher = cards.filter { it.cardType == CardType.Creature && it.power > 2 }.size
        val power4OrHigher = cards.filter { it.cardType == CardType.Creature && it.power > 3 }.size
        val power5OrHigher = cards.filter { it.cardType == CardType.Creature && it.power > 4 }.size

        if (power1 > 0) traits.addDeckTrait(SynergyTrait.power1Creatures, when {
            power1 > 3 -> 4
            power1 > 2 -> 3
            power1 > 1 -> 2
            else -> 1
        })

        if (power2OrLower > 3) traits.addDeckTrait(SynergyTrait.power2OrLowerCreatures, when {
            power2OrLower > 6 -> 4
            power2OrLower > 5 -> 3
            power2OrLower > 4 -> 2
            else -> 1
        })

        if (power3OrLower > 8) traits.addDeckTrait(SynergyTrait.power3OrLowerCreatures, when {
            power3OrLower > 11 -> 4
            power3OrLower > 10 -> 3
            power3OrLower > 9 -> 2
            else -> 1
        })

        if (power3OrHigher > 12) traits.addDeckTrait(SynergyTrait.power3OrHigherCreatures, when {
            power3OrHigher > 16 -> 4
            power3OrHigher > 14 -> 3
            power3OrHigher > 13 -> 2
            else -> 1
        })

        if (power4OrHigher > 8) traits.addDeckTrait(SynergyTrait.power4OrHigherCreatures, when {
            power4OrHigher > 12 -> 4
            power4OrHigher > 10 -> 3
            power4OrHigher > 9 -> 2
            else -> 1
        })

        if (power5OrHigher > 5) traits.addDeckTrait(SynergyTrait.power5OrHigherCreatures, when {
            power5OrHigher > 9 -> 4
            power5OrHigher > 7 -> 3
            power5OrHigher > 6 -> 2
            else -> 1
        })
    }
}

data class SynMatchInfo(
        var matches: Map<TraitStrength, Int>,
        var cardNames: List<String> = listOf()
)

data class SynTraitValueWithHouse(
        val value: SynTraitValue,
        val cardName: String?,
        val house: House?,
        val deckTrait: Boolean
)

fun MutableMap<SynergyTrait, SynTraitValuesForTrait>.addTrait(traitValue: SynTraitValue, cardName: String?, house: House?, deckTrait: Boolean = false) {
    if (!this.containsKey(traitValue.trait)) {
        this[traitValue.trait] = SynTraitValuesForTrait()
    }
    this[traitValue.trait]!!.traitValues.add(SynTraitValueWithHouse(traitValue, cardName, house, deckTrait))
}

fun MutableMap<SynergyTrait, SynTraitValuesForTrait>.addDeckTrait(trait: SynergyTrait, count: Int, house: House? = null, traitHouse: SynTraitHouse = SynTraitHouse.anyHouse) {
    repeat(count) {
        this.addTrait(SynTraitValue(trait, TraitStrength.NORMAL.value, traitHouse), null, house, true)
    }
}

data class SynTraitValuesForTrait(
        val traitValues: MutableList<SynTraitValueWithHouse> = mutableListOf()
) {
    fun matches(cardName: String, house: House, synergyValue: SynTraitValue): SynMatchInfo {
        val matchedTraits = traitValues
                .filter {
                    typesMatch(synergyValue.cardTypes, it.value.cardTypes) &&
                            playersMatch(synergyValue.player, it.value.player) &&
                            housesMatch(synergyValue.house, house, it.value.house, it.house, it.deckTrait)
                }

        var sameCard = false
        val cardNames = matchedTraits.mapNotNull {
            if (it.cardName == cardName) {
                sameCard = true
            }
            it.cardName
        }
        val strength = matchedTraits
                .groupBy { it.value.strength() }
                .map {
                    it.key to if (sameCard && it.value.any { it.cardName != null && it.cardName == cardName }) it.value.count() - 1 else it.value.count()
                }
                .toMap()
        return SynMatchInfo(strength, cardNames)
    }

    private fun typesMatch(types1: List<CardType>, types2: List<CardType>): Boolean {
        return types1.isEmpty() || types2.isEmpty() || types1.any { type1Type -> types2.any { type1Type == it } }
    }

    private fun playersMatch(player1: SynTraitPlayer, player2: SynTraitPlayer): Boolean {
        return player1 == SynTraitPlayer.ANY || player2 == SynTraitPlayer.ANY || player1 == player2
    }

    private fun housesMatch(synHouse: SynTraitHouse, house1: House, traitHouse: SynTraitHouse, house2: House?, deckTrait: Boolean = false): Boolean {
        return when (synHouse) {
            SynTraitHouse.anyHouse -> when (traitHouse) {
                // any house with any house always true
                SynTraitHouse.anyHouse -> true
                SynTraitHouse.house -> !deckTrait && house1 == house2
                SynTraitHouse.outOfHouse -> !deckTrait && house1 != house2
            }
            SynTraitHouse.house -> when (traitHouse) {
                SynTraitHouse.anyHouse -> !deckTrait && house1 == house2
                SynTraitHouse.house -> house1 == house2
                // out of house with in house always false
                SynTraitHouse.outOfHouse -> false
            }
            SynTraitHouse.outOfHouse -> when (traitHouse) {
                SynTraitHouse.anyHouse -> !deckTrait && house1 != house2
                // out of house with in house always false
                SynTraitHouse.house -> false
                SynTraitHouse.outOfHouse -> house1 != house2
            }
        }
    }
}

data class SynergizedValue(val value: Double, val synergy: Double)

fun Double?.isZeroOrNull() = this == null || this == 0.0
