package coraythan.keyswap.synergies

import coraythan.keyswap.House
import coraythan.keyswap.cards.Card
import coraythan.keyswap.cards.CardType
import coraythan.keyswap.cards.Rarity
import coraythan.keyswap.cards.extrainfo.ExtraCardInfo
import coraythan.keyswap.decks.models.Deck

fun basicCard() = Card(
    "",
    "Tremor",
    House.Brobnar,
    CardType.Action,
    "",
    "",
    0,
    0,
    "0",
    0,
    "0",
    Rarity.Common,
    null,
    "001",
    1,
    false,
    false,
    extraCardInfo = ExtraCardInfo(

    )
)

val boringDeck = Deck(
    keyforgeId = "",
    expansion = 1,
    name = "boring",
    houseNamesString = "Brobnar|Sanctum|Untamed"
)