<p align="center"><img src="https://raw.githubusercontent.com/iGoodie/TwitchSpawn/master/.github/assets/logo.png"></p>

<!-- Badges -->
<p align="center">
  <a href="https://www.twitch.tv/"><img src="https://img.shields.io/badge/api-twitch-b19dd8.svg"></a>
  <a href="https://streamlabs.com"><img src="https://img.shields.io/badge/api-streamlabs-32c3a2.svg"></a>
  <a href="https://streamelements.com"><img src="https://img.shields.io/badge/api-streamelements-f72c00.svg"></a>
  <br/>
  <a href="https://fabricmc.net/"><img src="https://img.shields.io/badge/modloader-fabric-dbd0b4.svg"></a>
  <a href="https://www.minecraft.net/"><img src="https://img.shields.io/badge/minecraft-26.3-62b47a.svg"></a>
  <br/>
  <a href="https://minecraft.curseforge.com/projects/twitchspawn"><img src="http://cf.way2muchnoise.eu/full_273382_downloads.svg"></a>
  <a href="https://minecraft.curseforge.com/projects/twitchspawn"><img src="http://cf.way2muchnoise.eu/versions/273382.svg"></a>
  <br/>
  <a href="https://twitter.com/iGoodiex"><img src="https://img.shields.io/twitter/follow/iGoodiex?style=social"></a>
  <a href="https://www.patreon.com/iGoodie"><img src="https://img.shields.io/endpoint.svg?url=https%3A%2F%2Fshieldsio-patreon.herokuapp.com%2Figoodie"></a>
</p>

TwitchSpawn now runs on **Fabric** for **Minecraft 26.3**! :tada:
Your configs, rules and TSL scripts carry over from the Forge version (see the [26.3 notes](#upgrading-rules-to-263) for item and gamerule syntax changes). (See the [user manual](https://igoodie.gitbook.io/twitchspawn/)!)

## Brief Summary
TwitchSpawn is a Minecraft mod designed for Twitch streamers using 3rd party streaming platforms!

It listens for live events related to your Twitch channel using various Socket APIs.
Then it handles those events with the rules handcrafted by you!

You can say hi to us by giving our Discord Server a visit! (https://discord.gg/KNxxdvN)

![Preview #1](preview/preview1.png)
![Preview #2](preview/preview2.png)

## Requirements
| | Version |
|---|---|
| Minecraft | 26.3 |
| [Fabric Loader](https://fabricmc.net/use/) | 0.19.5 or newer |
| [Fabric API](https://modrinth.com/mod/fabric-api) | Any 26.3 build (developed against `0.164.0+26.3`) |
| Java | 25 or newer |

> **Note:** TwitchSpawn no longer runs on Forge. Use the older Forge releases if you need Forge.

## Installation
1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 26.3.
2. Put both the [Fabric API](https://modrinth.com/mod/fabric-api) jar and the `twitchspawn_26.3-<version>.jar` into your `mods` folder.
3. Launch the game once. TwitchSpawn creates its configs in `config/TwitchSpawn/`.
4. Fill in `config/TwitchSpawn/credentials.toml` with your streaming platform token. Then run `/twitchspawn start` in-game.

The mod is needed on the server, and on each streamer's client for the status indicator, chat cooldown overlay and `OS_RUN` actions.

## How to use?
Complete user manual can be found on https://igoodie.gitbook.io/twitchspawn/

The manual was written for the Forge version. Apart from installation and the syntax changes below, it still applies. Configs live in `config/TwitchSpawn/` as before.

### Upgrading rules to 26.3
TwitchSpawn doesn't overwrite your existing `config/TwitchSpawn/*.tsl` files, so rules from an older version need these changes by hand:

| | Before (1.18) | Now (26.3) |
|---|---|---|
| Item names, enchantments etc. in `DROP` / `CHANGE` | `%stick{display:{Name:"\"Stick of Truth!\""}}%` | `%stick[custom_name="Stick of Truth!"]%` |
| | `diamond_sword{Enchantments:[{id:sharpness,lvl:5}]}` | `diamond_sword[enchantments={sharpness:5}]` |
| Gamerules in `EXECUTE` | `/gamerule keepInventory true` | `/gamerule keep_inventory true` |

Rules that still use the old `{...}` item NBT now fail with a syntax error when configs load, instead of quietly dropping the item without its name or enchantments.
`EXECUTE` commands are only checked when they run, so check those against the [current command syntax](https://minecraft.wiki/w/Commands).
`DROP` amounts above the max stack size are dropped as several full stacks.

## Features
### 1. All the events!
Thanks to the power of **SocketIO**, the mod is now able to respond to a wide variety of events!
*Donations, follows, subscriptions, resubs, bits* and many more events including for Youtube and Mixer as well!
List of supported streaming platforms:
- Streamlabs - (https://dev.streamlabs.com/docs/socket-api)
- StreamElements - (https://developers.streamelements.com/websockets)

(See 📜 TSL Events & Predicates - https://igoodie.gitbook.io/twitchspawn/twitchspawn-language/tsl-events-and-predicates)

### 2. Your own, readable rules!
The mod now comes with its own language to understand you: **TwitchSpawn Language (TSL)**!
With TSL, declaring event handling rules (rule sets) is piece of cake! It is easily understandable.
(E.g following sequence is a valid TSL script: `DROP minecraft:diamond ON Twitch Follow`)

(See 📜 TSL Basics - https://igoodie.gitbook.io/twitchspawn/twitchspawn-language/tsl-basics)

```coffeescript
# Drops 2 sticks on 0 to 20 unit donation
DROP minecraft:stick 2
 ON Donation
 WITH amount IN RANGE [0,20]

EITHER # Selects one random action
 # Either drops a diamond block
 DROP diamond_block 1
 OR
 # Or drops an iron block named "Iron Golem Body"
 DROP %iron_block[custom_name="Iron Golem Body"]% 2
 OR
 # Or summons a zombie on given coordinate
 SUMMON minecraft:zombie ~ ~+10 ~
 # By displaying one common message for any action selected!
 ALL DISPLAYING %["Get ready for spoils of battle!"]%
 ON Donation
 WITH amount IN RANGE [21, 999]

# Executes a Minecraft command as the streamer being the source!
EXECUTE %/gamerule keep_inventory true%
 DISPLAYING %[
  {text:"${actor}", color:"red"},
  {text:" turned immortality on!", color:"white"},
 ]%
 ON Donation
 WITH amount >= 1000

 # Instantly does two actions! Throws leggings and boots from the inventory!
BOTH INSTANTLY
 THROW leggings AND THROW boots
 DISPLAYING %["You forgot to wear your pants!"]%
 ON Twitch Subscription
 WITH months >= 2

# Drops a stick with a custom name, when a Twitch Follow is received!
DROP %minecraft:stick[custom_name="Stick of Truth!"]% 1
 ON Twitch Follow
```

### 3. One server, multiple streamers!
The mod is capable of parsing more than one ruleset,
which makes it possible for multiple streamers to use TwitchSpawn on the same server!

(See 📄 credentials.toml - https://igoodie.gitbook.io/twitchspawn/reference/configurations/credentials.toml)

Exemplar credentials.toml:
```toml
moderatorsTwitch = [ "Redowar" ]
moderatorsMinecraft = [ "Redowar" ]

[[streamers]]
	minecraftNick = "iGoodie"
	twitchNick = "iGoodiex"
	platform = "Streamlabs"
	token = "YOUR_SOCKET_TOKEN_HERE"

[[streamers]]
	minecraftNick = "iGoodie"
	twitchNick = "iGoodiex"
	platform = "StreamElements"
	token = "YOUR_SOCKET_TOKEN_HERE"
```

### 4. Way better customizability
You can customize the text that is shown on an action,
with an easy JSON format and well known Minecraft Text Component syntax!

(See 💿 Minecraft JSON Text Components - https://github.com/skylinerw/guides/blob/master/java/text%20component.md)

(See 📘 Customizing Messages - https://igoodie.gitbook.io/twitchspawn/basics/customizing-messages)

Exemplar messages.title.json:
```json
{
  "donation": [
    {
      "text": "${actor}",
      "color": "aqua"
    },
    {
      "text": " donated you ${amount_f}${currency}",
      "color": "white"
    }
  ],
  "twitch follow" : [ ... ],
  "twitch subscription" : [ ... ],
  ...
}
```
### 5. More reliable than before!
Unlike the previous (1.12.x) versions, errors will not cause Minecraft to crash with no report.
Instead it is aimed to show errors to the user as much as possible.
Config errors are logged, and shown in chat once you join a world.
On a dedicated server, the server refuses to start until the config errors are fixed.
If you're facing any sort of problem, do not hasitate giving our Discord Server a visit!

(👾 Discord Invite: https://discordapp.com/invite/KNxxdvN)

## Building from source
TwitchSpawn builds with [Fabric Loom](https://github.com/FabricMC/fabric-loom) and Gradle 9.7.1. It needs JDK 25; Gradle downloads it automatically if it isn't installed.

```sh
./gradlew build        # Builds build/libs/twitchspawn_26.3-<version>.jar and runs the tests
./gradlew test         # Runs the unit tests only
./gradlew runClient    # Starts a development Minecraft client with the mod
./gradlew runServer    # Starts a development dedicated server with the mod
```

The release jar bundles Night Config and the Socket.IO client, since Fabric does not provide them.

### Releasing
Every push and pull request is built and tested by the [Build workflow](.github/workflows/build.yml).

To publish a release, set `mod_version` in `gradle.properties`, then push a matching tag (`v2.0.0` or `2.0.0`), or create a GitHub release with that tag.
The [Release workflow](.github/workflows/release.yml) builds the mod and attaches `twitchspawn_<minecraft>-<version>.jar` to the release. It creates the release if it doesn't exist yet.
If the tag doesn't match `mod_version`, the workflow fails without attaching anything.

### Tests
Tests are in `src/test/java` and run on JUnit 5 through `fabric-loader-junit`, so they can use real Minecraft 26.3 classes and registries.
They cover:
- **TSL**: tokenizing, comments, parsing every action keyword, the item component syntax, predicates, syntax errors, and checking that the bundled default ruleset is valid
- **Networking**: encoding and decoding every packet's `StreamCodec`, and their payload ids
- **Translations**: every lang file has the same keys and formats correctly with vanilla `%1$s` placeholders
- **Mod metadata**: Fabric Loader resolves `fabric.mod.json`, the entrypoints, the Minecraft version range, and bundled assets
- **Configs**: generating default configs, correcting credentials, loading-error messages
- **Utilities**: percentages, randomizer, `${expression}` evaluation, cooldowns, event queue ordering

## Translations
* My Guildies: Elanor & Vaelios - German and French translations :tada:
* [SE7-KN8](https://github.com/SE7-KN8) - German translation of `test` command module keys

If you would like to contribute to the translations, consider modifying
[languages](https://github.com/iGoodie/TwitchSpawn/tree/master/src/main/resources/assets/twitchspawn/lang)
and creating a PR!

## Special Thanks
Special thanks to each one of those beautiful people:
* [Redowar](https://www.twitch.tv/redowar) - For igniting the initial idea of dropping (*actually Spawn'ing :p*) stuff in-game, dev-version testing and his amazing company!
* [ToastNomNomNom](https://www.twitch.tv/toastnomnomnom) - For reaching me, motivating me for a whole new paradigm and helping me do the alpha testings!
* [Köfteistköfte](https://www.youtube.com/user/kofteistkofte) - For his amazing OS_RUN action idea and testings!
* [AdmiralLemon](https://www.twitch.tv/theadmirallemon) - For his bug reports and helps on debugging!
* [Darkphan](https://www.twitch.tv/darkphan) - For his precious Github issues and suggestions!
* [iskall85](https://www.twitch.tv/iskall85) - For reaching me and motivating me for a 1.12.x refactor/port!
* [TheGreatGildersneeze](https://www.twitch.tv/thegreatgildersneeze) - For his amazing TwitchSpawn server and correction on a misleading documentation page!
* [JimilT92](https://github.com/JimiIT92) - For his amazing insight on placeholder expressions and detailed issues!
* [BisUmTo](https://www.twitch.tv/bisumto) - For his priceless bug-reports and endless effort on debugging them!
* [Diaval](https://github.com/thediaval) - For his epic memes and pair programming sessions!
* [Azelza](https://www.twitch.tv/azelzagaming) - For his unconditional support and love to the Spawn Project!
* [Atanend](https://www.twitch.tv/atanend), [DesimieS](https://www.twitch.tv/desimies), [Katayanka](https://www.twitch.tv/kata1925), [Grishnack](https://www.twitch.tv/grishnackx) and [Taul](https://www.twitch.tv/taulnyb) - For their assist in alpha testing the TSL2.0 early features!
* My Guildies: Elanor & Vaelios - For preparing German and French translations!
* And every single person in our [Discord Server](https://discord.gg/KNxxdvN) for keeping us motivated on this project!

## Facing an Issue?
- Join our Discord Server - https://discordapp.com/invite/KNxxdvN
- Contact iGoodie via Discord: iGoodie#1945
- Create an issue on Github: https://github.com/iGoodie/TwitchSpawn/issues
