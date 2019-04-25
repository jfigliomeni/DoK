import { AppBar, Grid, Tabs } from "@material-ui/core"
import { GridProps } from "@material-ui/core/Grid"
import { observer } from "mobx-react"
import * as React from "react"
import { Route, RouteComponentProps, Switch } from "react-router"
import { spacing } from "../config/MuiConfig"
import { AboutSubPaths } from "../config/Routes"
import { LinkTab } from "../generic/LinkTab"
import { screenStore } from "../ui/ScreenStore"
import { uiStore } from "../ui/UiStore"
import { ContactMe } from "./ContactMe"
import { PatreonRewards } from "./PatreonRewards"
import { ReleaseNotes } from "./ReleaseNotes"
import { SasAndAerc } from "./SasAndAerc"
import { SellersAndDevs } from "./SellersAndDevs"

@observer
export class AboutPage extends React.Component<RouteComponentProps<{}>> {

    constructor(props: RouteComponentProps<{}>) {
        super(props)
        uiStore.setTopbarValues("About", "About", "What it's for and how it works")
    }

    render() {
        return (
            <div style={{margin: spacing(4), backgroundColor: "#FFF"}}>
                <AppBar position={"static"} color={"default"}>
                    <Tabs
                        value={this.props.location.pathname}
                        centered={screenStore.screenSizeMdPlus()}
                        variant={screenStore.screenSizeSm() ? "fullWidth" : undefined}
                    >
                        <LinkTab label="SAS and AERC" to={AboutSubPaths.sas} value={AboutSubPaths.sas}/>
                        <LinkTab label="Patron Rewards" to={AboutSubPaths.patreon} value={AboutSubPaths.patreon}/>
                        <LinkTab label="Contact Me" to={AboutSubPaths.contact} value={AboutSubPaths.contact}/>
                        <LinkTab label="Release Notes" to={AboutSubPaths.releaseNotes} value={AboutSubPaths.releaseNotes}/>
                        <LinkTab label="Sellers And Devs" to={AboutSubPaths.sellersAndDevs} value={AboutSubPaths.sellersAndDevs}/>
                    </Tabs>
                </AppBar>
                <div style={{padding: spacing(4)}}>
                    <Grid container={true} spacing={32} justify={"center"}>
                        <Switch>
                            <Route path={AboutSubPaths.sas} component={SasAndAerc}/>
                            <Route path={AboutSubPaths.patreon} component={PatreonRewards}/>
                            <Route path={AboutSubPaths.contact} component={ContactMe}/>
                            <Route path={AboutSubPaths.releaseNotes} component={ReleaseNotes}/>
                            <Route path={AboutSubPaths.sellersAndDevs} component={SellersAndDevs}/>
                        </Switch>
                    </Grid>
                </div>
            </div>
        )
    }
}

export const AboutGridItem = (props: GridProps) => (<Grid item={true} xs={12} md={6} xl={6} style={{maxWidth: 624}} {...props} />)
