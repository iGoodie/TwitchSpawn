package net.programmer.igoodie.twitchspawn.tracer;

public enum Platform {

    STREAMLABS_SOCKET(
            "Streamlabs",
            "https://sockets.streamlabs.com"),
    STREAMELEMENTS_SOCKET(
            "StreamElements",
            "https://realtime.streamelements.com"),
    ;

    /* ----------------------------- */

    public String name;
    public String url;

    Platform(String name, String url) {
        this.name = name;
        this.url = url;
    }

    @Override
    public String toString() {
        return name;
    }

}
