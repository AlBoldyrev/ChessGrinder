import {TournamentDto} from "./MainPageData";

export interface MatchParticipantDto {
    userId: string,
    name: string
}

export interface MatchDto {
    id: string
    /**
     * Undefined if match is not played yet
     */
    result: MatchResult | undefined
    white: ParticipantDto | null
    /**
     * Null if the match is buy
     */
    black: ParticipantDto | null
}

export type MatchResult = "WHITE_WIN" | "BLACK_WIN" | "DRAW" | "BUY" | "MISS"

export interface RoundDto {
    isFinished: boolean
    matches: MatchDto[]
}

export interface ParticipantDto {
    id: string
    name: string
    userId?: string | null | undefined
    userFullName?: string | null | undefined
    score: number
    buchholz: number
    isMissing: boolean
    place: number
}

export interface TournamentPageData {
    participants: ParticipantDto[]
    rounds: RoundDto[],
    tournament: TournamentDto,
}
