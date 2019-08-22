<p align="center"><img src="https://cdn.discordapp.com/attachments/460909423045509140/460909450354622476/logo.png"></p>

<!-- Badges -->
<p align="center">
  <a href="https://www.twitch.tv/"><img src="https://img.shields.io/badge/api-twitch-b19dd8.svg"></a>
  <a href="https://streamlabs.com"><img src="https://img.shields.io/badge/api-streamlabs-32c3a2.svg"></a>
  <br/>
  <a href="https://minecraft.curseforge.com/projects/twitchspawn"><img src="http://cf.way2muchnoise.eu/full_273382_downloads.svg"></a>
  <a href="https://minecraft.curseforge.com/projects/twitchspawn"><img src="http://cf.way2muchnoise.eu/versions/273382.svg"></a>
</p>

Long waited update is finally here! :tada: 1.14 Adaptation is alive!

## Brief Summary
TwitchSpawn is a Minecraft mod designed for Twitch streamers using 3rd party streaming platforms!

It listens for live events related to your Twitch channel using various Socket APIs.
Then it handles those events with the rules handcrafted by you!

You can say hi to us by giving our Discord Server a visit! (https://discord.gg/KNxxdvN)

![Preview #1](preview/preview1.png)
![Preview #2](preview/preview2.png)

## How to use?
Complete user manual can be found on https://igoodie.gitbook.io/twitchspawn/

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
 DROP %iron_block{display:{Name:"\"Iron Golem Body\""}}% 2
 OR
 # Or summons a zombie on given coordinate
 SUMMON minecraft:zombie ~ ~+10 ~
 # By displaying one common message for any action selected!
 ALL DISPLAYING %["Get ready for spoils of battle!"]%
 ON Donation
 WITH amount IN RANGE [21, 999]

# Executes a Minecraft command as the streamer being the source!
EXECUTE %/gamerule keepInventory true%
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

# Drops a stick with NBT data, when a Twitch Follow is received!
DROP %minecraft:stick{display:{Name:"\"Stick of Truth!\""}}% 1
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
If you're facing any sort of problem, do not hasitate giving our Discord Server a visit!

(👾 Discord Invite: https://discordapp.com/invite/KNxxdvN)

![Error Display Preview](preview/error_preview.png)

## Facing an Issue?
- Join our Discord Server - https://discordapp.com/invite/KNxxdvN
- Contact iGoodie via Discord: iGoodie#1945
- Create an issue on Github: https://github.com/iGoodie/TwitchSpawn/issues
