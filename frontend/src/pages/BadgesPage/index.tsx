import {useQuery} from "@tanstack/react-query";
import badgeRepository from "lib/api/repository/BadgeRepository";
import ConditionalOnUserRole, {Conditional} from "components/Conditional";
import {useForm} from "react-hook-form";
import {UserRoles} from "lib/api/dto/MainPageData";
import {randomString} from "lib/util/Random";
import Gravatar, {GravatarType} from "components/Gravatar";

function BadgeImage(
    {
        imageUrl,
        title,
        size
    }: {
        imageUrl: string,
        title: string,
        size: number
    }
) {
    return <div style={{width: size, height: size}}
                className={`rounded-full overflow-hidden grid bg-blue-500`}>
        <Conditional on={!imageUrl}>
            <Gravatar text={title} type={GravatarType.Wavatar} size={size}/>
        </Conditional>
        <Conditional on={imageUrl.length === 1}>
            <span
                style={{
                    fontSize: Math.round(size * 0.85 - 10),
                    lineHeight: size + "px",
                }}
                className={`w-full h-full font-bold inline-block text-center`}>
                {imageUrl}
            </span>
        </Conditional>
        <Conditional on={imageUrl?.length > 1}>
            <Gravatar text={imageUrl} type={GravatarType.Wavatar} size={size}/>
        </Conditional>
    </div>;
}

export default function BadgesPage() {
    const {register, handleSubmit, watch} = useForm();

    let badgesQuery = useQuery({
        queryKey: ["badges"],
        queryFn: async () => {
            return badgeRepository.getBadges()
        }
    })

    async function createBadge(data: any) {
        await badgeRepository.createBadge({
            id: randomString(15),
            description: data.description,
            title: data.title,
            imageUrl: data.imageUrl,
        })
        await badgesQuery.refetch()
    }

    return <>
        Badges page
        {
            <Conditional on={!badgesQuery.isSuccess || !(badgesQuery.data?.values?.length !== 0 || false)}>
                <div>No badges</div>
            </Conditional>
        }
        <div className={"grid grid-cols-12"}>
            {
                badgesQuery.data?.values?.map(badge => {
                    return <div key={badge.id} title={badge.description} className={"col-span-3 grid oveflow-hidden content-start"}>
                        {/*<span className={"text-3xl"}>{badge.imageUrl}</span>*/}
                        <div className={"rounded-full overflow-hidden flex justify-center"}>
                            <BadgeImage imageUrl={badge.imageUrl} title={badge.title} size={100}/>
                        </div>
                        <span>{badge.title}</span>
                        <div className={"max-w-full overflow-hidden"}>
                            <span
                                className={"text-sm text-gray-500 break-words text-ellipsis"}>{badge.description}
                            </span>
                        </div>
                    </div>
                })
            }
        </div>
        <ConditionalOnUserRole role={UserRoles.ADMIN}>
            <form className={"grid p-4"}
                  onSubmit={handleSubmit(createBadge)}
            >
                <h3 className={"font-bold"}>Create badge</h3>
                <div className={"flex"}>
                    <div>
                        <BadgeImage imageUrl={(watch("imageUrl", "") as string)} title={(watch("title", "") as string)}
                                    size={100}/>
                    </div>
                    <div className={"grid"}>
                        <input type={"text"}
                               placeholder={"Image Url"} /*defaultValue={"⭐"}*/ {...register("imageUrl")}/>
                        <input type={"text"} placeholder={"Title"}  {...register("title")}/>
                        <input type={"text"} placeholder={"Description"}  {...register("description")}/>
                    </div>
                </div>
                <button
                    type={"submit"}
                    className={"btn bg-primary"}
                >
                    Create badge
                </button>
            </form>
        </ConditionalOnUserRole>
    </>
}
