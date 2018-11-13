<p align="center"><img src="https://cdn.discordapp.com/attachments/460909423045509140/460909450354622476/logo.png"></p>

<!-- Badges -->
<p align="center">
  <a href="https://www.twitch.tv/"><img src="https://img.shields.io/badge/api-twitch-b19dd8.svg"></a>
  <a href="https://streamlabs.com"><img src="https://img.shields.io/badge/api-streamlabs-32c3a2.svg"></a>
  <br/>
  <a href="https://minecraft.curseforge.com/projects/twitchspawn"><img src="http://cf.way2muchnoise.eu/full_273382_downloads.svg"></a>
  <a href="https://minecraft.curseforge.com/projects/twitchspawn"><img src="http://cf.way2muchnoise.eu/versions/273382.svg"></a>
</p>



## Brief Summary

TwitchSpawn is an in-dev mod, which is designed for Twitch streamers using Streamlabs. 
The mod provides supply drops to the streamer as his/her viewers donate or subscribe. 
Rewards are hand defined by the streamer in a very basic config file structure!

## Configuration
(Detailed info about how to start can be found in [TwitchSpawn Wiki page](https://github.com/iGoodie/TwitchSpawn/wiki))

(Detailed info about `config.json` can be also found in [TwitchSpawn Wiki page](https://github.com/iGoodie/TwitchSpawn/wiki/Config.json-Details))

There are some required configs (Found in config/TwitchSpawn/config.json) before the mod works properly.

API tokens are the very first thing you would like to add. 
There are 2 different tokens can be gathered from Streamlabs; `API Access Token` and `Socket API Token`. TwitchSpawn uses them to communicate with Streamlabs!

Login to your [Streamlabs](https://streamlabs.com/) account.
Navigate to [API Settings](https://streamlabs.com/dashboard#/apisettings) page from the left menu.
Finally switch the tab to "API Tokens", and voilà, you can copy `access_token` and `socket_api_token` from here.

![Streamlabs API Tokens Tab](https://cdn.discordapp.com/attachments/460909423045509140/506632938067197953/help1.png)

Moving on, nicknames should be filled with streamer's nicknames. Otherwise, streamer will not have sufficient permissions to execute `/twitchspawn` commands.
Fortunately, it is so easy to edit! Just change the values of `streamer_mc_nick` and `streamer_twitch_nick` fields to your nicknames, like so;
```
"streamer_mc_nick": "iGoodie",
"streamer_twitch_nick": "iGoodiex"
```

And finally, rewards should be declared. There are currently 5 event rewarding system; ,`streamlabs donations`, `twitch follows`, `twitch hosts` `twitch bits` and `twitch subscriptions`.
An examplar rewards config will be created upon the very first load of the mod. The reward entries should be in ascending order.
Following `bit_rewards` example is a valid one:
```json
"bit_rewards": [
 {
  "minimum_bit": 0,
  "items": [ "minecraft:stick", "minecraft:apple" ]
 },
 {
  "minimum_bit": 100,
  "items": [ "minecraft:diamond_block" ]
 }
]
```
If you want to have no reward for an event, just leave the reward array empty, like so;
```json
"donation_rewards": []
```

## How to use?

You should use some in-game commands to start/stop the mod. These commands are;

*   **/twitchspawn start** - Starts the mod with the loaded configs
*   **/twitchspawn stop** - Stops the mod
*   **/twitchspawn status** - Displays the status of the mod (ON/OFF)
*   **/twitchspawn reloadcfg** - Reloads all the configs without requiring restart
*   **/twitchspawn test \<name\> \<amount\> [type]** - Simulates an event for test purposes

![In-game SS](https://cdn.discordapp.com/attachments/329962349081526273/340121198027472896/unknown.png)

## Future Goals

* Mixer & Youtube alerts are also handled properly. *(Still TwitchSpawn? Or MoreThanTwitchSpawn?)*

* Reward gathering system (Blocks and items) is going to be designed & implemented

* More than item drops! For example entity spawning, structure generating or even scriptable changes in-game! (Need to integrate scripting API before)
