import React, {useMemo} from "react";
import {MemberList} from "./MemberList";
import {TournamentsList} from "./TournamentsList";
import {useQuery} from "@tanstack/react-query";
import {MemberDto, TournamentDto} from "lib/api/dto/MainPageData";
import mainPageRepository from "lib/pageRepository/MainPageRepository";

function MainPage() {
    let {data, refetch} = useQuery({
            queryKey: ["mainPage"],
            queryFn:
            mainPageRepository.getData
        }
    );

    async function createTournament() {
        await mainPageRepository.postTournament()
        await refetch()
    }

    let members: MemberDto[] = useMemo(() => {
        if (data?.members && data.members.length > 0) {
            return data.members;
        } else {
            return [
                {
                    id: "ab1234567",
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
                    id: "vs234823476",
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
            ];
        }
    }, [data])

    let tournaments: TournamentDto[] = useMemo(() => data?.tournaments || [], [data])

    return <>
        <MemberList members={members}/>
        <TournamentsList tournaments={tournaments} createTournament={createTournament}/>
    </>
}

export default MainPage
