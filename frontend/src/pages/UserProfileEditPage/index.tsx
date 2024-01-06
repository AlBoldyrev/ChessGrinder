import Gravatar, {GravatarType} from "components/Gravatar";
import {useQuery} from "@tanstack/react-query";
import userRepository from "lib/api/repository/UserRepository";
import React, {useEffect} from "react";
import {Link, useNavigate} from "react-router-dom";
import {useForm} from "react-hook-form";
import {useLoc} from "strings/loc";
import {requirePresent} from "lib/util/common";
import {useAuthenticatedUser} from "contexts/AuthenticatedUserContext";

export default function UserProfileEditPage() {
    let authenticatedUser = useAuthenticatedUser();
    let navigate = useNavigate()
    let loc = useLoc()

    useEffect(() => {
        if (!authenticatedUser) {
            navigate("/login")
        }
    }, [authenticatedUser, navigate]);


    const currentUserName = authenticatedUser?.username;

    let userQuery = useQuery({
                queryKey: ["username", currentUserName],
                queryFn: async () => {
//                 console.log(authenticatedUser?.name + "@@@"); // это - никнейм
//                 console.log(currentUserName + "!!!!"); //Это именно почта!
                    //Из репозитория возвращается именно user!
                    return await userRepository.getUser(currentUserName!!);
                    //TODO вопрос Владимиру: почему надо ставить !! в конце, иначе выдает ошибку?
                },
            })

            const {handleSubmit} = useForm();
            //TODO отрефакторить
            if (!authenticatedUser) {
                    return <>You are not logged in</>
                }

    async function saveUserData(data: { [key: string]: string}) {
        let userPageData = requirePresent(userQuery.data, "User not loaded");
        userPageData.username = data.username;
        userPageData.name = data.name;
        //Password?!

//         let user = userPageData.user;
        //мб это надо сначала спарсить из data?
//         await userRepository.updateUser(userPageData); //Пока что выдает ошибку 405
    //TODO
    //Сначала с фронта (из формочки) надо получить экземпляр измененного пользователя,
    //затем данные надо спарсить и засунуть их в репозиторий
        navigate(`/user/${currentUserName}`);
    }

    return <div className={"p-3"}>
        <div className="flex py-2">
            <h1 className={"font-semibold uppercase"}>{loc("Settings")}</h1>
        </div>
        <form className={"grid gap-2"} onSubmit={handleSubmit(saveUserData)}>
            <div>
                <Gravatar text={authenticatedUser.username || authenticatedUser.id} type={GravatarType.Robohash} size={100}
                          className={"rounded-full"}/>
            </div>

            <div className={"grid grow text-left gap-2"}>
                <input type={"text"} className={"font-semibold uppercase truncate border-b-2"}
                       defaultValue={authenticatedUser.name || authenticatedUser.username || authenticatedUser.id || "Unknown"}
                       title={loc("Full name")}
                       placeholder={loc("Full name")}
                />
                <input type={"text"} className={"text-sm text-gray-500 border-b-2"}
                       defaultValue={authenticatedUser.username}
                       title={loc("Username")}
                       placeholder={loc("Username")}
                />
                <div className="p-2"></div>
                <h3 className={"text-sm uppercase font-semibold"}>{loc("Change password")}</h3>
                <div className={"grid gap-2 p-2 border"}>
                    <input type={"password"} className={"font-semibold uppercase truncate border-b-2"}
                           defaultValue={"*********"}
                           title={loc("Old password")}
                           placeholder={loc("Old password")}
                    />
                    <input type={"password"} className={"font-semibold uppercase truncate border-b-2"}
                           title={loc("New password")}
                           placeholder={loc("New password")}
                    />
                    <input type={"password"} className={"font-semibold uppercase truncate border-b-2"}
                           title={loc("Confirm new password")}
                           placeholder={loc("Confirm new password")}
                    />
                </div>
            </div>
            <div className={"grid gap-2"}>
                <div className={"grid gap-2"}>
                    <div className="flex gap-2 justify-end">
                     {/*TODO действие привязывается именно к форме onSubmit={handleSubmit(saveTournament)}*/}
                        <button type={"submit"} className="btn-primary uppercase">
                            {loc("Save")}
                        </button>
                        <Link to={`/user/${currentUserName}`}>
                            <button className="btn-light uppercase">{loc("Cancel")}</button>
                        </Link>
                    </div>
                    <div className={"flex justify-end"}>
                        <button className="btn-danger uppercase">{loc("Delete profile")}</button>
                    </div>
                </div>
            </div>
        </form>
    </div>
}
