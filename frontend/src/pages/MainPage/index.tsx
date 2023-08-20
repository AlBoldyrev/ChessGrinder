import React, {useMemo} from "react";
import {MemberList} from "./MemberList";
import {TournamentsList} from "./TournamentsList";
import {useQuery} from "@tanstack/react-query";
import axios, {AxiosRequestConfig} from "axios";
import MainPageData, {MemberDto, TournamentDto} from "lib/api/dto/MainPageData";

function MainPage() {
    let data = useQuery(
        ["mainPage"],
        async () => {
            const {data} = await axios.get(
                "http://localhost:8080/pages/main",
                {
                    headers: {
                        "Authorization": "Basic dm92YTpzaGVmZXI="
                    }
                } as AxiosRequestConfig
            );
            return data as MainPageData;
        });

    let members: MemberDto[] = useMemo(() => data?.data?.members || [
        {
            name: "Alexander Boldyrev",
            badges: [
                {
                    title: "",
                    imageUrl: "💥",
                    description: "Огонь-огонечек",
                },
                {
                    title: "",
                    imageUrl: "🎃",
                    description: "За участие в хэллоуин-вечеринке 2019",
                },
            ]
        } as MemberDto,
        {
            name: "Vladimir Shefer",
            badges: [
                {
                    title: "",
                    imageUrl: "🍒",
                    description: "Ягодный никнейм",
                },
                {
                    title: "",
                    imageUrl: "🎯",
                    description: "За партию с самой высокой точностью на неделе.",
                },
            ]
        } as MemberDto,
    ], [data])

    let tournaments: TournamentDto[] = useMemo(() => data?.data?.tournaments || [
        {
            id: "uuid-uuid-1",
            name: "Tournament 1",
            date: "2023-07-10"
        },
        {
            id: "uuid-uuid-2",
            name: "Tournament 2",
            date: "2023-07-17"
        },
        {
            id: "uuid-uuid-3",
            name: "Tournament 3",
            date: "2023-07-24"
        },
    ], [data])

    return <>
        <MemberList members={members}/>
        <TournamentsList tournaments={tournaments}/>
    </>
}

export default MainPage
