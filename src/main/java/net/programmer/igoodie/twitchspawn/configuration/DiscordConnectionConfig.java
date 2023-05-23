package net.programmer.igoodie.twitchspawn.configuration;

import net.programmer.igoodie.goodies.configuration.JsonConfiGoodie;
import net.programmer.igoodie.goodies.serialization.annotation.Goodie;

import java.util.List;

public class DiscordConnectionConfig extends JsonConfiGoodie {

    @Goodie
    protected String token;

    @Goodie
    protected List<String> editorIds;

    @Goodie
    protected String channelId;

    public String getToken() {
        return token;
    }

    public List<String> getEditorIds() {
        return editorIds;
    }

    public String getChannelId() {
        return channelId;
    }

}
