# [ajLeaderboards](https://about.ajg0702.us/project/ajLeaderboards)

ajLeaderboards is a plugin to allow you to create leaderboards for basically anything using PlaceholderAPI placeholders.

![Features](https://ajg0702.us/img/plugins/header.php?r=1&text=Features)

- Supports server versions 1.8.x-1.21.x & 26.1.x and will (almost certainly) work fine on new MC versions without requiring an update.
- Can be used to make leaderboards from basically any plugin, it just needs to have a PAPI placeholder that returns a number.
- Can show player prefixes/suffixes (from Vault) on the leaderboard
- Display leaderboards anywhere that PlaceholderAPI is supported (holograms / display entities, NPCs, etc.)
- Display leaderboards on signs (built-in and fully customizable)
- Display leaderboards with NPCs (using PAPI, [see wiki](https://wiki.ajg0702.us/ajleaderboards/setup/setup#7-optional-display-the-leaderboard-using-npcs))
- Players' heads above signs
- Players' heads on Armor Stands
- MySQL supported (to make sure leaderboards are synced, the target placeholder must also be synced)
- ["Extra" placeholders](https://wiki.ajg0702.us/ajLeaderboards/setup/extras) (allows you to display any placeholders' text alongside the leaderboard)
- Timed leaderboards that reset automatically without effecting all-time stats (hourly, daily, monthly, etc.)
- Folia Support
- More features to come! (request them)

![Screenshots](https://ajg0702.us/img/plugins/header.php?text=Screenshots)

![Leaderboard using holograms](https://ajg0702.us/img/plugins/ajleaderboards/1.png) <br/>
A leaderboard displayed using holograms

![NPC leaderboard](https://ajg0702.us/img/plugins/ajleaderboards/2.png) <br/>
A NPC leaderboard using [Citizens](https://www.spigotmc.org/resources/citizens.13811/)

![Sign leaderboard with heads](https://ajg0702.us/img/plugins/ajleaderboards/3.png?r=2) <br/>
A sign leaderboard (using the built-in signs) with heads.
(also supports Armor Stands, and the heads are optional)

![Interactive hologram leaderboards](https://ajg0702.us/img/plugins/ajleaderboards/4.gif) <br/>
Interactive hologram leaderboards using [DecentHolograms](https://modrinth.com/plugin/decentholograms)' pages feature. <br/>
Basically, each timed type is a separate page, and the pages are switched by clicking.

![Setup](https://ajg0702.us/img/plugins/header.php?text=Setup)

**You must have [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) installed!**

Setup is pretty straightforward.

You just use the `/ajLeaderboards add` command to start tracking a placeholder that returns a numberical value (a number).
Then, as players join, they will be added to the leaderboard.

To display the leaderboard, you use the [PlaceholderAPI LB placeholders](https://wiki.ajg0702.us/ajleaderboards/setup/placeholders),
or use the [built-in signs](https://wiki.ajg0702.us/ajLeaderboards/setup/#4-optional-display-the-leaderboard-using-signs).

For a more detailed (and explained) setup process, including the exact requirements for the placeholder you put in `/ajlb add`,
check out the [setup guide on the wiki](https://wiki.ajg0702.us/ajleaderboards/setup/setup).

![Support](https://ajg0702.us/img/plugins/header.php?text=Support)

Before trying to get support, please [read the FAQ](https://wiki.ajg0702.us/ajLeaderboards/faq)

After reading the FAQ, if you have any questions, suggestions, or issues with the plugin, please contact me on any of the below methods:

- [Discord](https://discord.gg/GXv2F5sXCx)
- [Spigot PM](https://www.spigotmc.org/conversations/add?to=ajgeiss0702&title=ajLeaderboards%20support)
- [Spigot discussion section](https://www.spigotmc.org/threads/ajleaderboards.471179/)
- [Email](mailto:plugin-support@ajg0702.us)

(Discord is preferred and the quickest way to get a response)

AI Support is also available on the Discord server, which you can get instant support from. It is able to answer the vast majority of questions and solve most issues that people ask about in support. <br/>
AI Support is always optional, and you do not need to use it if you don't want to. <br/>
**PLEASE** use my AI support instead of a public chatbot such as ChatGPT or Gemini. Public ones make stuff up a **lot** more often than mine does, since they do not have direct access to the wiki.
My AI support has direct access to the wiki, which means it has the most up-to-date information and recommendations.

![Pre-Releases](https://ajg0702.us/img/plugins/header.php?text=Pre-Releases)

Pre-Releases (also known as dev builds) are builds of the plugin from the `dev` branch,
which may contain unstable and/or untested changes. <br/>
You can download them on [Modrinth](https://modrinth.com/plugin/ajleaderboards/versions)
and [Voxel.shop](https://voxel.shop/product/2726/ajleaderboards/updates) (formerly Polymart).
They are not available on Spigot because Spigot does not allow a separate release channel for them.


Make sure you download it from the "Versions" or "Updates" tab, because if you download from the main download button,
you will probably get the latest full release instead of the latest Pre-Release. <br/>
The version you download should have a build number on the end of the version name (e.g. `2.10.1-b320`).
If this build number is missing from the version name or the jar name, it is not a Pre-Release.


<details>
<summary>bStats</summary>

This plugin collects anonymous usage information via [bStats](https://bstats.org/plugin/bukkit/ajLeaderboards/9338). It can be disabled in bStats' config file.

![bStats](https://bstats.org/signatures/bukkit/ajLeaderboards.svg)

</details>

Some icons made by authors from [www.flaticon.com](https://www.flaticon.com)
