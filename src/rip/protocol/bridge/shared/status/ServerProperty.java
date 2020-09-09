package rip.protocol.bridge.shared.status;

public enum ServerProperty {

    PROVIDERNAME, SERVERNAME, ONLINE, MAXIMUM, STATUS, MOTD, TPS, LASTUPDATED, DATA, GROUP, PLAYERS;

    public String getJedisId() {
        return "Bridge-data" + ":" + this.name().toLowerCase();
    }
}