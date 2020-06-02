import { observer } from "mobx-react"
import * as React from "react"
import { AercRadar } from "../aerc/AercRadar"
import { cardStore } from "../cards/CardStore"
import { keyLocalStorage } from "../config/KeyLocalStorage"
import { Loader } from "../mui-restyled/Loader"
import { CardTypeRadar } from "../stats/CardTypeRadar"
import { screenStore } from "../ui/ScreenStore"
import { DeckViewSmall } from "./DeckViewSmall"
import { DeckSearchResult } from "./models/DeckSearchResult"

export interface DeckListViewProps {
    decks: DeckSearchResult[]
    sellerView?: boolean
}

@observer
export class DeckListView extends React.Component<DeckListViewProps> {
    render() {

        if (!cardStore.cardsLoaded) {
            return <Loader/>
        }

        const {decks} = this.props
        const displayGraphs = keyLocalStorage.deckListViewType === "graphs"

        return (
            <div style={{display: "flex", flexWrap: "wrap", justifyContent: "center"}}>
                {decks.map((deck) => {
                    return (
                        <div key={deck.id} style={{display: "flex"}}>
                                <DeckViewSmall deck={deck} saleInfo={deck.deckSaleInfo}/>
                            {displayGraphs && (
                                <div style={{display: screenStore.smallDeckView() ? undefined : "flex", flexWrap: "wrap"}}>
                                    <AercRadar deck={deck}/>
                                    <CardTypeRadar deck={deck}/>
                                </div>
                            )}
                        </div>
                    )
                })}
            </div>
        )
    }
}
