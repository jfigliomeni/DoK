package coraythan.keyswap.synergy.synergysystem

import coraythan.keyswap.House
import coraythan.keyswap.cards.CardType
import coraythan.keyswap.cards.dokcards.DokCardInDeck
import coraythan.keyswap.decks.models.GenericDeck
import coraythan.keyswap.synergy.SynTraitHouse
import coraythan.keyswap.synergy.SynergyTrait
import coraythan.keyswap.synergy.TraitStrength

object GenerateDeckAndHouseTraits {

    fun addOutOfHouseTraits(
        houses: List<House>,
        cards: List<DokCardInDeck>,
        traits: MutableMap<SynergyTrait, MatchSynergiesToTraits>
    ) {
        houses.forEach { house ->
            val cardsNotForHouse = cards.filter { it.house != house }
            val creatureCount = cardsNotForHouse.count { it.card.cardType == CardType.Creature }

            if (creatureCount > 12) traits.addDeckTrait(
                SynergyTrait.highCreatureCount, when {
                    creatureCount > 16 -> 4
                    creatureCount > 15 -> 3
                    creatureCount > 14 -> 2
                    else -> 1
                }, house, SynTraitHouse.outOfHouse
            )

            if (creatureCount < 10) traits.addDeckTrait(
                SynergyTrait.lowCreatureCount, when {
                    creatureCount < 6 -> 4
                    creatureCount < 7 -> 3
                    creatureCount < 8 -> 2
                    else -> 1
                }, house, SynTraitHouse.outOfHouse
            )
        }
    }

    fun addHouseTraits(
        houses: List<House>,
        cards: List<DokCardInDeck>,
        traits: MutableMap<SynergyTrait, MatchSynergiesToTraits>,
    ) {
        houses.forEach { house ->
            val cardsForHouse = cards.filter { it.house == house }
            val totalCreaturePower = cardsForHouse.sumOf { it.card.power }
            val creatureCount = cardsForHouse.count { it.card.cardType == CardType.Creature }
            val artifactCount = cardsForHouse.count { it.card.cardType == CardType.Artifact }
            val upgradeCount = cardsForHouse.count { it.card.cardType == CardType.Upgrade }

            val bonusAmber = cardsForHouse.map { it.totalAmber }.sum()
            traits.addDeckTrait(
                SynergyTrait.bonusAmber,
                bonusAmber,
                house,
                SynTraitHouse.house,
                strength = TraitStrength.EXTRA_WEAK
            )

            val totalExpectedAmber = cardsForHouse.map {
                val max = it.extraCardInfo.expectedAmberMax ?: 0.0
                val min = it.extraCardInfo.expectedAmber
                if (max == 0.0) min else (min + max) / 2
            }.sum()
            val totalArmor = cardsForHouse.sumOf { it.card.armor }

            if (totalExpectedAmber > 7) traits.addDeckTrait(
                SynergyTrait.highExpectedAmber, when {
                    totalExpectedAmber > 10 -> 4
                    totalExpectedAmber > 9 -> 3
                    totalExpectedAmber > 8 -> 2
                    else -> 1
                }, house, SynTraitHouse.house
            )
            if (totalExpectedAmber < 7) traits.addDeckTrait(
                SynergyTrait.lowExpectedAmber, when {
                    totalExpectedAmber < 4 -> 4
                    totalExpectedAmber < 5 -> 3
                    totalExpectedAmber < 6 -> 2
                    else -> 1
                }, house, SynTraitHouse.house
            )

            if (totalCreaturePower > 21) traits.addDeckTrait(
                SynergyTrait.highTotalCreaturePower, when {
                    totalCreaturePower > 27 -> 4
                    totalCreaturePower > 25 -> 3
                    totalCreaturePower > 23 -> 2
                    else -> 1
                }, house, SynTraitHouse.house
            )

            if (totalCreaturePower < 20) traits.addDeckTrait(
                SynergyTrait.lowTotalCreaturePower, when {
                    totalCreaturePower < 14 -> 4
                    totalCreaturePower < 16 -> 3
                    totalCreaturePower < 18 -> 2
                    else -> 1
                }, house, SynTraitHouse.house
            )

            if (upgradeCount > 0) traits.addDeckTrait(
                SynergyTrait.upgradeCount, when {
                    upgradeCount > 3 -> 4
                    upgradeCount > 2 -> 3
                    upgradeCount > 1 -> 2
                    else -> 1
                }, house, SynTraitHouse.house
            )

            if (creatureCount > 6) traits.addDeckTrait(
                SynergyTrait.highCreatureCount, when {
                    creatureCount > 9 -> 4
                    creatureCount > 8 -> 3
                    creatureCount > 7 -> 2
                    else -> 1
                }, house, SynTraitHouse.house
            )

            if (creatureCount < 6) traits.addDeckTrait(
                SynergyTrait.lowCreatureCount, when {
                    creatureCount < 3 -> 4
                    creatureCount < 4 -> 3
                    creatureCount < 5 -> 2
                    else -> 1
                }, house, SynTraitHouse.house
            )

            if (artifactCount > 2) traits.addDeckTrait(
                SynergyTrait.highArtifactCount, when {
                    artifactCount > 3 -> 4
                    else -> 2
                }, house, SynTraitHouse.house
            )

            if (artifactCount < 2) traits.addDeckTrait(
                SynergyTrait.lowArtifactCount, when {
                    artifactCount < 1 -> 4
                    else -> 2
                }, house, SynTraitHouse.house
            )

            if (totalArmor > 1) traits.addDeckTrait(
                SynergyTrait.highTotalArmor, when {
                    totalArmor > 5 -> 4
                    totalArmor > 4 -> 3
                    totalArmor > 3 -> 2
                    else -> 1
                }, house, SynTraitHouse.house
            )
        }
    }

    fun addDeckTraits(
        deck: GenericDeck,
        traits: MutableMap<SynergyTrait, MatchSynergiesToTraits>,
        cards: List<DokCardInDeck>
    ) {

        if (deck.houses.contains(House.Mars)) traits.addDeckTrait(SynergyTrait.hasMars, 4)

        val bonusAmber = cards.sumOf { it.totalAmber }
        val bonusCapture = cards.sumOf { it.bonusCapture }
        val bonusDraw = cards.sumOf { it.bonusDraw }
        val bonusDamage = cards.sumOf { it.bonusDamage }

        traits.addDeckTrait(SynergyTrait.bonusAmber, bonusAmber, strength = TraitStrength.EXTRA_WEAK)
        traits.addDeckTrait(SynergyTrait.bonusCapture, bonusCapture, strength = TraitStrength.EXTRA_WEAK)
        traits.addDeckTrait(SynergyTrait.bonusDraw, bonusDraw, strength = TraitStrength.EXTRA_WEAK)
        traits.addDeckTrait(SynergyTrait.bonusDamage, bonusDamage, strength = TraitStrength.EXTRA_WEAK)

        val totalExpectedAmber = cards.sumOf { it.extraCardInfo.expectedAmber }

        val totalPower = cards.sumOf { it.card.power }
        val totalArmor = cards.sumOf { it.card.armor }
        val artifactCount = cards.count { it.card.cardType == CardType.Artifact }
        val upgradeCount = cards.count { it.card.cardType == CardType.Upgrade }
        val creatureCount = cards.count { it.card.cardType == CardType.Creature }

        if (totalExpectedAmber > 21) traits.addDeckTrait(
            SynergyTrait.highExpectedAmber, when {
                totalExpectedAmber > 26 -> 4
                totalExpectedAmber > 25 -> 3
                totalExpectedAmber > 23 -> 2
                else -> 1
            }
        )
        if (totalExpectedAmber < 19) traits.addDeckTrait(
            SynergyTrait.lowExpectedAmber, when {
                totalExpectedAmber < 15 -> 4
                totalExpectedAmber < 17 -> 3
                totalExpectedAmber < 18 -> 2
                else -> 1
            }
        )

        if (totalPower < 60) traits.addDeckTrait(
            SynergyTrait.lowTotalCreaturePower, when {
                totalPower < 47 -> 4
                totalPower < 52 -> 3
                totalPower < 57 -> 2
                else -> 1
            }
        )
        if (totalPower > 67) traits.addDeckTrait(
            SynergyTrait.highTotalCreaturePower, when {
                totalPower > 83 -> 4
                totalPower > 77 -> 3
                totalPower > 72 -> 2
                else -> 1
            }
        )

        if (totalArmor > 3) traits.addDeckTrait(
            SynergyTrait.highTotalArmor, when {
                totalArmor > 8 -> 4
                totalArmor > 6 -> 3
                totalArmor > 4 -> 2
                else -> 1
            }
        )

        if (artifactCount > 4) traits.addDeckTrait(
            SynergyTrait.highArtifactCount, when {
                artifactCount > 7 -> 4
                artifactCount > 6 -> 3
                artifactCount > 5 -> 2
                else -> 1
            }
        )

        if (artifactCount < 4) traits.addDeckTrait(
            SynergyTrait.lowArtifactCount, when {
                artifactCount < 1 -> 4
                artifactCount < 2 -> 3
                artifactCount < 3 -> 2
                else -> 1
            }
        )

        if (upgradeCount > 0) traits.addDeckTrait(
            SynergyTrait.upgradeCount, when {
                upgradeCount > 3 -> 4
                upgradeCount > 2 -> 3
                upgradeCount > 1 -> 2
                else -> 1
            }
        )

        if (creatureCount > 18) traits.addDeckTrait(
            SynergyTrait.highCreatureCount, when {
                creatureCount > 21 -> 4
                creatureCount > 20 -> 3
                creatureCount > 19 -> 2
                else -> 1
            }
        )

        if (creatureCount < 17) traits.addDeckTrait(
            SynergyTrait.lowCreatureCount, when {
                creatureCount < 14 -> 4
                creatureCount < 15 -> 3
                creatureCount < 16 -> 2
                else -> 1
            }
        )
    }

}