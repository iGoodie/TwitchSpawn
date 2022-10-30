package net.programmer.igoodie.twitchspawn.configuration;

import net.programmer.igoodie.goodies.configuration.JsonConfiGoodie;
import net.programmer.igoodie.goodies.serialization.annotation.Goodie;
import net.programmer.igoodie.twitchspawn.network.Platform;

public class ClientCredentialsConfig extends JsonConfiGoodie {

    @Goodie
    public String twitchNickname = "";

    @Goodie
    public Platform platform = Platform.STREAMELEMENTS;

    @Goodie
    public String platformToken = "";

    @Goodie
    public String twitchChatToken = "YOUR_CHAT_TOKEN_HERE - Can be generated from https://twitchapps.com/tmi/";

}
