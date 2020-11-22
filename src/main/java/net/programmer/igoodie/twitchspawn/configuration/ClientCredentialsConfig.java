package net.programmer.igoodie.twitchspawn.configuration;

import com.google.gson.annotations.Expose;

public class ClientCredentialsConfig extends JSONConfig {

    @Expose public String twitchNickname;
    @Expose public String streamlabsToken;

    @Override
    public String getName() {
        return "client_credentials";
    }

    @Override
    protected void reset() {
        this.twitchNickname = "";
        this.streamlabsToken = "";
    }

}
