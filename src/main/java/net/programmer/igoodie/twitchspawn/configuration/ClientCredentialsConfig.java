package net.programmer.igoodie.twitchspawn.configuration;

import com.google.gson.annotations.Expose;

public class ClientCredentialsConfig extends JSONConfig {

    @Expose public String twitchNickname;
    @Expose public String streamlabsToken;
    @Expose public String twitchChatToken;

    @Override
    public String getName() {
        return "client_credentials";
    }

    @Override
    protected void reset() {
        this.twitchNickname = "";
        this.streamlabsToken = "";
        this.twitchChatToken = "YOUR_CHAT_TOKEN_HERE - Can be generated from https://twitchapps.com/tmi/";
    }

}
