import { observer } from "mobx-react"
import React from "react"
import { Utils } from "../config/Utils"
import { DeckSearchResult } from "../decks/models/DeckSearchResult"
import { DokRadar } from "../graphs/DokRadar"
import { Loader } from "../mui-restyled/Loader"
import { statsStore } from "../stats/StatsStore"

interface AercRadarProps {
    deck: DeckSearchResult
    style?: React.CSSProperties
}

@observer
export class AercRadar extends React.Component<AercRadarProps> {
    render() {
        const {deck, style} = this.props
        const {synergies} = deck
        const stats = statsStore.stats
        if (stats == null) {
            return <Loader/>
        }
        const hasAerc = synergies ?? deck
        const data = [
            {
                aerc: "Expected Aember (E)",
                deck: Utils.valueFromPercentiles(hasAerc.expectedAmber, stats.expectedAmberPercentiles),
            },
            {
                aerc: "Aember Control (A)",
                deck: Utils.valueFromPercentiles(hasAerc.amberControl, stats.amberControlPercentiles),
            },
            {
                aerc: "Artifact Ctrl (R)",
                deck: Utils.valueFromPercentiles(hasAerc.artifactControl ?? 0, stats.artifactControlPercentiles),
            },
            {
                aerc: "Creature Control (C)",
                deck: Utils.valueFromPercentiles(hasAerc.creatureControl, stats.creatureControlPercentiles),
            },
            {
                aerc: "Effective Power (P)",
                deck: Utils.valueFromPercentiles(hasAerc.effectivePower, stats.effectivePowerPercentiles),
            },
            {
                aerc: "Creature Protection",
                deck: Utils.valueFromPercentiles(hasAerc.creatureProtection ?? 0, stats.creatureProtectionPercentiles),
            },
            {
                aerc: "Disruption (D)",
                deck: Utils.valueFromPercentiles(hasAerc.disruption ?? 0, stats.disruptionPercentiles),
            },
            {
                aerc: "Efficiency (F)",
                deck: Utils.valueFromPercentiles(hasAerc.efficiency ?? 0, stats.efficiencyPercentiles),
            },
        ]

        return (
            <DokRadar
                data={data}
                keys={["deck"]}
                indexBy={"aerc"}
                name={"AERC Percentile Rankings"}
                maxValue={100}
                style={style}
            />
        )
    }
}
