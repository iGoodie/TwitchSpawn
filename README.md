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

TwitchSpawn is an in-dev mod, which is designed for Twitch streamers using Streamlabs (for now, it will be extended for more networks like Streamlabs, G4G etc.). The mod provides supply drops to the streamer, as long as his/her viewers donate. Rewards are hand defined by the streamer!

## Configuration

There are some required configs (Found in config/TwitchSpawn/config.json) before the mod works properly. 

First of them is access_token field. It can be found at streamerlabs.com

![Streamlabs Account Panel](https://cdn.discordapp.com/attachments/157967568722853888/341273171040665601/unknown.png)

Secondly, minecraft and twitch nicks of the streamer (streamer_mc_nick and streamer_twitch_nick fields)

And finally, rewards array should be filled as desired. (minimum_currency fields are evaluated by the default currency code of the streamer on Streamlabs.)

## How to use?

You should use some in-game commands to start/stop the mod. These commands are;

*   **/twitchspawn start** - Starts the mod with the loaded configs
*   **/twitchspawn stop** - Stops the mod
*   **/twitchspawn status** - Displays the status of the mod (ON/OFF)
*   **/twitchspawn reloadcfg** - Reloads all the configs without requiring restart
*   **/twitchspawn test \<name\> \<amount\>** - Simulates a donation for test purposes

![In-game SS](https://cdn.discordapp.com/attachments/329962349081526273/340121198027472896/unknown.png)

## Future Goals

* Twitch bits are going to be considered.

* Tipeeestream tracer is going to be implemented

* Reward gathering system (Blocks and items) is going to be designed & implemented

* More than item drops! For example entity spawning, structure generating or even scriptable changes in-game! (Need to integrate scripting API before)
