import {MatchDto, MatchResult, RoundDto} from "../../lib/api/dto/TournamentPageData";
import RunningRoundTable from "./RunningRoundTable";
import React from "react";

export default function RoundTab(
    {
        round,
        submitMatchResult
    }: {
        round: RoundDto,
        submitMatchResult: (match: MatchDto, result: MatchResult | null) => void,
    }
) {
    return <div>
        <RunningRoundTable matches={round.matches}
                           submitMatchResult={(match, result) => {
                               submitMatchResult(match, result!!);
                           }}/>
        <div className={"mt-2 px-2 w-full flex justify-end"}>
            <button className={"bg-red-200 p-1 rounded-md mx-1 px-1"}>Delete</button>
            <button className={"bg-orange-200 p-1 rounded-md mx-1 px-1"}>Draw</button>
            <button className={"bg-blue-200 p-1 rounded-md mx-1"}>Finish</button>
        </div>
    </div>
}
