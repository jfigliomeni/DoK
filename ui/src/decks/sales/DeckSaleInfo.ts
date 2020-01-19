import { AuctionDto, AuctionStatus } from "../../auctions/AuctionDto"
import { userStore } from "../../user/UserStore"
import { DeckCondition } from "../../userdeck/UserDeck"
import { DeckLanguage } from "../DeckLanguage"

export interface DeckSaleInfo {
    forTrade: boolean
    forAuction: boolean

    forSaleInCountry?: string
    currencySymbol: string
    language?: DeckLanguage

    highestBid?: number
    buyItNow?: number
    startingBid?: number
    auctionEndDateTime?: string
    auctionId?: string
    nextBid?: number
    youAreHighestBidder?: boolean
    yourMaxBid?: number
    bidIncrement?: number
    auctionStatus?: AuctionStatus
    boughtBy?: string
    boughtWithBuyItNow?: boolean
    boughtNowOn?: string

    listingInfo?: string
    externalLink?: string
    condition: DeckCondition
    dateListed: string
    expiresAt?: string

    username: string
    publicContactInfo?: string
    discord?: string
}

export const deckSaleInfoFromAuctionDto = (auctionDto: AuctionDto): DeckSaleInfo | undefined => {

    const {
        buyItNow, forTrade, id, listingInfo, externalLink, condition, dateListedLocalDate, expiresAtLocalDate, currencySymbol, language, startingBid, status
    } = auctionDto
    const forAuction = status === AuctionStatus.ACTIVE

    const user = userStore.user

    if (user == null) {
        return undefined
    }
    return {
        forTrade,
        forAuction,

        forSaleInCountry: user.country,
        currencySymbol,
        language,

        auctionId: id,

        startingBid,
        listingInfo,
        externalLink,
        buyItNow,

        dateListed: dateListedLocalDate!,
        expiresAt: expiresAtLocalDate,

        condition: condition == null ? DeckCondition.NEW_IN_PLASTIC : condition,
        username: user.username,
        publicContactInfo: user.publicContactInfo,
        discord: user.discord
    }
}
