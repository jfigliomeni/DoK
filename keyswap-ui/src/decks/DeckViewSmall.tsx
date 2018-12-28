import Card from "@material-ui/core/Card/Card"
import CardActions from "@material-ui/core/CardActions/CardActions"
import CardContent from "@material-ui/core/CardContent/CardContent"
import { blue } from "@material-ui/core/colors"
import Divider from "@material-ui/core/Divider/Divider"
import List from "@material-ui/core/List/List"
import Typography from "@material-ui/core/Typography/Typography"
import { observer } from "mobx-react"
import * as React from "react"
import { CardAsLine } from "../cards/CardSimpleView"
import { KCard } from "../cards/KCard"
import { spacing } from "../config/MuiConfig"
import { ScreenStore } from "../config/ScreenStore"
import { InfoBox } from "../generic/InfoBox"
import { House, houseValues } from "../houses/House"
import { HouseBanner } from "../houses/HouseBanner"
import { WishlistDeck } from "./buttons/WishlistDeck"
import { Deck, DeckUtils } from "./Deck"

interface DeckViewSmallProps {
    deck: Deck
}

@observer
export class DeckViewSmall extends React.Component<DeckViewSmallProps> {
    render() {
        const {name, houses, expectedAmber, totalPower, id} = this.props.deck
        return (
            <Card
                style={{
                    margin: spacing(2),
                    width: ScreenStore.instance.screenSizeXs() ? 344 : 544,
                }}
            >
                <div
                    style={{
                        backgroundColor: blue ["500"],
                        width: "100%", display: "flex",
                        flexDirection: "column",
                        justifyContent: "center",
                        paddingTop: spacing(1),
                        paddingBottom: spacing(1),
                    }}
                >
                    <div style={{display: "flex", flexWrap: "wrap"}}>
                        <HouseBanner houses={houses} style={{flexGrow: 1}}/>
                        <div
                            style={{
                                display: "flex",
                                justifyContent: "space-evenly",
                                marginTop: spacing(1),
                                flexGrow: 1,
                            }}
                        >
                            <InfoBox top={"SAS"} bottom={"52"} popInfo={"Synergy and Anti-Synergy Rating"} textColor={"#FFFFFF"}/>
                            <InfoBox top={"Aember"} bottom={expectedAmber} popInfo={"Expected aember generated from cards"} textColor={"#FFFFFF"}/>
                            <InfoBox top={"Power"} bottom={totalPower} popInfo={"Power of all creatures combined"} textColor={"#FFFFFF"}/>
                        </div>
                    </div>
                </div>
                <CardContent style={{paddingBottom: 0, flexGrow: 1}}>
                    <Typography variant={"h5"} style={{paddingBottom: spacing(1)}}>{name}</Typography>
                    <div style={{display: "flex"}}>
                        <DisplayAllCardsByHouse deck={this.props.deck}/>
                    </div>
                </CardContent>

                <CardActions>
                    <WishlistDeck deckName={name} wishlisted={false} deckId={id}/>
                </CardActions>
            </Card>
        )
    }
}

const DisplayAllCardsByHouse = (props: { deck: Deck }) => {
    const cardsByHouse = DeckUtils.cardsInHouses(props.deck)

    if (ScreenStore.instance.screenSizeXs()) {
        return <DisplayAllCardsByHouseCompact {...props}/>
    }

    return (
        <div style={{display: "flex", justifyContent: "space-between", width: "100%"}}>
            {cardsByHouse.map((cardsForHouse) => (<DisplayCardsInHouse key={cardsForHouse.house} {...cardsForHouse}/>))}
        </div>
    )
}

const DisplayAllCardsByHouseCompact = (props: { deck: Deck }) => {
    const cardsByHouse = DeckUtils.cardsInHouses(props.deck)

    return (
        <div style={{display: "flex", flexWrap: "wrap", justifyContent: "center"}}>
            {cardsByHouse.map((cardsForHouse) => (
                <div style={{flex: 1}}>
                    <DisplayCardsInHouse key={cardsForHouse.house} {...cardsForHouse}/>
                </div>
            ))}
            <div style={{flex: 1}}/>
        </div>
    )
}

const DisplayCardsInHouse = (props: { house: House, cards: KCard[] }) => (
    <List>
        {houseValues.get(props.house)!.title}
        <Divider style={{marginTop: 4}}/>
        {props.cards.map((card, idx) => (<CardAsLine key={idx} card={card}/>))}
    </List>
)