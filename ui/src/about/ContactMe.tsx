import { Typography } from "@material-ui/core"
import { observer } from "mobx-react"
import * as React from "react"
import { spacing } from "../config/MuiConfig"
import { Routes } from "../config/Routes"
import { InfoListCard } from "../generic/InfoListCard"
import { KeyButton } from "../mui-restyled/KeyButton"
import { LinkButton } from "../mui-restyled/LinkButton"
import { patreonStore } from "../patreon/PatreonStore"
import { PatronButton } from "../patreon/PatronButton"
import { AboutGridItem } from "./AboutPage"

@observer
export class ContactMe extends React.Component {

    componentDidMount(): void {
        patreonStore.findTopPatrons()
    }

    render() {
        return (
            <>
                <AboutGridItem>
                    <InfoListCard
                        style={{maxWidth: 608}}
                        title={"Decks of Keyforge"}
                        infos={[
                            "The most robust deck and card search available for Keyforge.",
                            "SAS is a unique rating system that reflects approximate deck power.",
                            "List decks for sale or trade, and use the search features to find available decks.",
                            "Wishlist and mark decks as funny, and see what everyone else thinks too!",
                            <span style={{display: "flex", alignItems: "center"}}>
                                <Typography style={{marginRight: spacing(1)}}>Join the discussion on</Typography>
                                <KeyButton
                                    style={{margin: spacing(1)}}
                                    color={"inherit"}
                                    href={"https://discord.gg/T5taTHm"}
                                    variant={"contained"}
                                >
                                    Discord
                                </KeyButton>
                            </span>,
                            <span>
                                <Typography>For comments and suggestions contact CorayThan on Reddit, or send an email to</Typography>
                                <a href={"mailto:decksofkeyforge@gmail.com"}>decksofkeyforge@gmail.com</a>
                            </span>,
                            <div style={{paddingTop: spacing(1)}}>
                                <PatronButton primary={true} variant={"contained"}/>
                            </div>
                        ]}
                    />
                </AboutGridItem>
                <AboutGridItem>
                    <InfoListCard title={"Thanks and Kudos"} infos={[
                        "Decks of Keyforge wouldn't be possible without the support of this great community. To everyone who has given advice, " +
                        "reported bugs, or sent me their appreciation thank you!",
                        "I also want to specifically thank the site's most generous patrons:",
                        <div>
                            {patreonStore.topPatrons.map((patronUsername) => (
                                <Typography key={patronUsername}>{patronUsername}</Typography>
                            ))}
                        </div>,
                        "Without their support, as well as the support of all the other patrons, this site would not be possible. If you aren't already a " +
                        "patron, please consider becoming one to support the site and get some exclusive benefits on the site!",
                    ]}/>
                </AboutGridItem>
                <AboutGridItem>
                    <InfoListCard title={"Legal Stuff"} infos={[
                        "Decks of Keyforge is not associated with KeyForge or Fantasy Flight Games.",
                        "You sell, trade, and purchase decks at your own risk. We do not verify the authenticity or trustworthiness of any sellers, " +
                        "buyers, or transactions.",
                        "The SAS rating system is a copyrighted property of Decks of Keyforge, but I'm always interested in hearing about ways you'd " +
                        "like to help make it better or collaborate! But please don't steal the system, obviously.",
                        "Also, the SAS rating system isn't perfect, and is subject to change at any time. We are not responsible for any perceived " +
                        "or real loss of value due to changes to the system.",
                        <LinkButton size={"small"} to={Routes.privacyPolicy}>Privacy Policy</LinkButton>
                    ]}/>
                </AboutGridItem>
            </>
        )
    }
}
