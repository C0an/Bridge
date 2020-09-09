package rip.protocol.bridge.shared.backend;

import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bson.Document;
import rip.protocol.bridge.BridgeShared;
import rip.protocol.bridge.shared.disguise.Disguise;
import rip.protocol.bridge.shared.disguise.Skin;
import rip.protocol.bridge.shared.grant.Grant;
import rip.protocol.bridge.shared.profile.Profile;
import rip.protocol.bridge.shared.punishment.Punishment;
import rip.protocol.bridge.shared.punishment.PunishmentType;
import rip.protocol.bridge.shared.ranks.Rank;
import rip.protocol.bridge.shared.utils.Callback;

import java.util.*;
import java.util.regex.Pattern;

public class MongoManager {

    @Getter private MongoClient mongoClient;
    @Getter private MongoDatabase database;
    @Getter private MongoCollection<Document> rankCollection;
    @Getter private MongoCollection<Document> profileCollection;
    @Getter private MongoCollection<Document> punishmentCollection;
    @Getter private MongoCollection<Document> skinCollection;



    public MongoManager() {
        String userPart = "";
        if (BridgeShared.isMongoAuth()) {
            userPart = BridgeShared.getMongoUsername() + ":" + BridgeShared.getMongoPassword() + "@";
        }
        mongoClient = new MongoClient(new MongoClientURI("mongodb://" + userPart + BridgeShared.getMongoHost() + ":" + BridgeShared.getMongoPort() + (BridgeShared.isMongoAuth() ? "/admin" : "")));

        try {
            database = mongoClient.getDatabase(BridgeShared.getMongoDatabase());
            rankCollection = database.getCollection("ranks");
            profileCollection = database.getCollection("profiles");
            punishmentCollection = database.getCollection("punishments");
            skinCollection = database.getCollection("skins");
        } catch (Exception ex) {
            BridgeShared.sendLog("§cFailed to initialize backend.");
            BridgeShared.sendLog(ex.getClass().getSimpleName() + " - " + ex.getMessage());
        }
    }

    /*/
        Rank - MongoDB
    /*/

    public void getRanksInDB(Callback<Set<UUID>> callback) {
        Set<UUID> ranks = new HashSet<>();
        for (Document document : rankCollection.find()) {
            ranks.add(UUID.fromString(document.get("uuid").toString()));
        }
        callback.call(ranks);
    }

    public void loadRank(UUID id, Callback<Rank> callback, boolean async) {
        if(async) {
            new Thread(() -> loadRank(id, callback, false)).start();
            return;
        }
        Rank rank;
        if(BridgeShared.getRankManager().getRankByID(id) != null){
            rank = BridgeShared.getRankManager().getRankByID(id);
        }
        else {
            rank = new Rank(id, false);
        }

        Document document = rankCollection.find(Filters.eq("uuid", id.toString())).first();

        if (document == null) {
            callback.call(null);
            return;
        }
        rank.setName(document.getString("name"));
        rank.setDisplayName(document.getString("displayName"));
        rank.setPriority(document.getInteger("priority"));
        rank.setStaff(document.getBoolean("staff"));
        rank.setHidden(document.getBoolean("hidden"));
        rank.setGrantable(document.getBoolean("grantable"));
        rank.setDefaultRank(document.getBoolean("defaultRank"));
        rank.setColor(document.getString("color"));
        rank.setPrefix(document.getString("prefix"));
        rank.setSuffix(document.getString("suffix"));
        rank.getPermissions().addAll(document.getList("permissions", String.class));
        callback.call(rank);
    }

    public void saveRank(Rank rank, Callback<Boolean> callback, boolean async) {
        if (async) {
            new Thread(() -> saveRank(rank, callback, false)).start();
            return;
        }
        List<String> inherits = new ArrayList<>();
        rank.getInherits().forEach(r->inherits.add(r.getUuid().toString()));

        Document document = new Document()
                .append("uuid", rank.getUuid().toString())
                .append("name", rank.getName())
                .append("displayName", rank.getDisplayName())
                .append("priority", rank.getPriority())
                .append("staff", rank.isStaff())
                .append("hidden", rank.isHidden())
                .append("grantable", rank.isGrantable())
                .append("defaultRank", rank.isDefaultRank())
                .append("color", rank.getColor())
                .append("prefix", rank.getPrefix())
                .append("suffix", rank.getSuffix())
                .append("permissions", rank.getPermissions())
                .append("inheritance", inherits);

        rankCollection.replaceOne(Filters.eq("uuid", rank.getUuid().toString()), document, new ReplaceOptions().upsert(true));
        callback.call(true);
    }

    public void removeRank(UUID id, Callback<Boolean> callback, boolean async) {
        if(async) {
            new Thread(() -> removeRank(id, callback, false)).start();
            return;
        }
        Rank rank = BridgeShared.getRankManager().getRankByID(id);
        if (rank == null) {
            callback.call(false);
            return;
        }

        rankCollection.deleteOne(Filters.eq("uuid", rank.getUuid().toString()));
        BridgeShared.getRankManager().getRanks().remove(rank);
        callback.call(true);
    }

    public void getRankInherits(Rank rank, Callback<List<UUID>> callback, boolean async) {
        if(async) {
            new Thread(() -> getRankInherits(rank, callback, false)).start();
            return;
        }
        Document document = rankCollection.find(Filters.eq("uuid", rank.getUuid().toString())).first();

        if (document == null) {
            callback.call(null);
            return;
        }
        List<UUID> inherits = new ArrayList<>();

        document.getList("inheritance", String.class).forEach(id-> inherits.add(UUID.fromString(id)));
        callback.call(inherits);
    }



    /*/
        Profile - MongoDB
    /*/

    public void getProfilesInDB(Callback<Set<UUID>> callback) {
        Set<UUID> profiles = new HashSet<>();
        for (Document document : profileCollection.find()) {
            profiles.add(UUID.fromString(document.get("uuid").toString()));
        }
        callback.call(profiles);
    }

    public void loadProfile(String input, Callback<Profile> callback, boolean async, LoadType loadType) {
        if(async) {
            new Thread(() -> loadProfile(input, callback, false, loadType)).start();
            return;
        }
        if(loadType == LoadType.UUID) {
            try {
                UUID uuid = UUID.fromString(input);
            }catch (Exception e) {
                callback.call(null);
                return;
            }
        }

        Document document = profileCollection.find(Filters.regex(loadType.getObjectName(), Pattern.compile("^" + input + "$", Pattern.CASE_INSENSITIVE))).first();
        if (document == null) {
            callback.call(null);
            return;
        }

        Profile profile = new Profile(document.getString("name"), UUID.fromString(document.getString("uuid")), false);
        List<String> perms = document.getList("permissions", String.class);
        List<Grant> grants = new ArrayList<>();

        document.getList("grants", String.class).forEach(grantStr-> grants.add(Grant.deserialize(grantStr)));

        profile.getPermissions().addAll(perms);
        profile.getGrants().addAll(grants);
        if(document.containsKey("prefix")) profile.setPrefix(document.getString("prefix"));
        if(document.containsKey("suffix")) profile.setSuffix(document.getString("suffix"));
        if(document.containsKey("color")) profile.setColor(document.getString("color"));
        if(document.containsKey("ip")) profile.setIP(document.getString("ip"));

        if(document.get("firstJoined") != null) profile.setFirstJoined(document.getLong("firstJoined"));
        if(document.get("lastJoined") != null) profile.setLastJoined(document.getLong("lastJoined"));
        if(document.get("lastQuit") != null) profile.setLastQuit(document.getLong("lastQuit"));
        if(document.get("connectedServer") != null) profile.setConnectedServer(document.getString("connectedServer"));

        try {
            if (document.containsKey("disguisedRank") && document.get("disguisedRank") != null && BridgeShared.getRankManager().getRankByID(UUID.fromString(document.getString("disguisedRank"))) != null)
                profile.setDisguisedRank(BridgeShared.getRankManager().getRankByID(UUID.fromString(document.getString("disguisedRank"))));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(document.get("disguise") != null) profile.setDisguise(new Gson().fromJson(document.getString("disguise"), Disguise.class));

        callback.call(profile);
    }


    public void saveProfile(Profile profile, Callback<Boolean> callback, boolean async) {
        if (async) {
            new Thread(() -> saveProfile(profile, callback, false)).start();
            return;
        }
        List<String> allGrants = new ArrayList<>();
        profile.getGrants().forEach(grant-> allGrants.add(Grant.serialize(grant).toString()));
        Document document = new Document()
                .append("uuid", profile.getUuid().toString())
                .append("name", profile.getUsername())
                .append("prefix", profile.getPrefix())
                .append("suffix", profile.getSuffix())
                .append("color", profile.getColor())
                .append("disguisedRank", profile.getDisguisedRank() == null ? null : profile.getDisguisedRank().getUuid().toString())
                .append("permissions", profile.getPermissions())
                .append("grants", allGrants)
                .append("ip", profile.getIP())

                .append("firstJoined", profile.getFirstJoined())
                .append("lastJoined", profile.getLastJoined())
                .append("lastQuit", profile.getLastQuit())
                .append("connectedServer", profile.getConnectedServer())
                .append("disguise", (profile.getDisguise() == null ? null : profile.getDisguise().toString()));

        profileCollection.replaceOne(Filters.eq("uuid", profile.getUuid().toString()), document, new ReplaceOptions().upsert(true));
        callback.call(true);
    }

    public void removeProfile(UUID uuid, Callback<Boolean> callback, boolean async) {
        if (async) {
            new Thread(() -> removeProfile(uuid, callback, false)).start();
            return;
        }
        profileCollection.deleteOne(Filters.eq("uuid", uuid.toString()));
        callback.call(true);
    }

    @SuppressWarnings("This should never be ran on the main-thread!")
    public List<Profile> getProfiles(){
        List<Profile> profiles = new ArrayList<>();
        MongoCursor<Document> cursor = BridgeShared.getMongoManager().getProfileCollection().find().iterator();
        for (;cursor.hasNext();) {
            Document document = cursor.next();
            BridgeShared.getMongoManager().loadProfile(document.getString("name"), profiles::add, false, MongoManager.LoadType.USERNAME);
        }
        return profiles;
    }

    public void getProfiles(String ip, Callback<List<Profile>> callback, boolean async) {
        if (async) {
            new Thread(() -> getProfiles(ip, callback, false)).start();
            return;
        }
        if(ip == null || ip.equals("N/A")) {
            callback.call(null);
            return;
        }
        boolean shouldGetAll = ip.toLowerCase().equals("*");
        FindIterable<Document> document = (shouldGetAll ? profileCollection.find() : profileCollection.find(Filters.eq("ip", ip)));
        List<Profile> profiles = new ArrayList<>();
        for (Document doc : document) {
            if(doc == null) {
                continue;
            }
            profiles.add(BridgeShared.getProfileManager().getProfileByUUIDOrCreate(UUID.fromString(doc.getString("uuid"))));
        }
        callback.call(profiles);
    }

    @AllArgsConstructor @Getter
    public enum LoadType {

        USERNAME("name"),
        IP("ip"),
        UUID("uuid");

        private String objectName;

    }

    /*
        Skins - MongoDB
     */

    /*/
        Profile - MongoDB
    /*/

    public void getSkinsInDB(Callback<ArrayList<UUID>> callback) {
        ArrayList<UUID> skins = new ArrayList<>();
        for (Document document : skinCollection.find()) {
            skins.add(UUID.fromString(document.get("uuid").toString()));
        }
        callback.call(skins);
    }

    public void loadSkin(UUID uuid, Callback<Skin> callback, boolean async) {
        if(async) {
            new Thread(() -> loadSkin(uuid, callback, false)).start();
            return;
        }

        Document document = skinCollection.find(Filters.regex("uuid", Pattern.compile("^" + uuid.toString() + "$", Pattern.CASE_INSENSITIVE))).first();
        if (document == null) {
            callback.call(null);
            return;
        }

        callback.call(new Skin(uuid, document.getString("skinName"), document.getString("texture"), document.getString("signature")));
    }


    public void saveSkin(Skin skin, Callback<Boolean> callback, boolean async) {
        if (async) {
            new Thread(() -> saveSkin(skin, callback, false)).start();
            return;
        }
        Document document = new Document()
                .append("uuid", skin.getUuid().toString())
                .append("skinName", skin.getSkinName())
                .append("texture", skin.getTexture())
                .append("signature", skin.getSignature());

        skinCollection.replaceOne(Filters.eq("uuid", skin.getUuid().toString()), document, new ReplaceOptions().upsert(true));
        callback.call(true);
    }

    public void removeSkin(UUID uuid, Callback<Boolean> callback, boolean async) {
        if (async) {
            new Thread(() -> removeSkin(uuid, callback, false)).start();
            return;
        }
        skinCollection.deleteOne(Filters.eq("uuid", uuid.toString()));
        callback.call(true);
    }

    /*/
        Punishment - MongoDB
     */

    public void getPunishmentsInDB(Callback<ArrayList<UUID>> callback) {
        ArrayList<UUID> punishments = new ArrayList<>();
        for (Document document : punishmentCollection.find()) {
            punishments.add(UUID.fromString(document.get("uuid").toString()));
        }
        callback.call(punishments);
    }

    public void loadPunishment(UUID id, Callback<Punishment> callback, boolean async) {
        if(async) {
            new Thread(() -> loadPunishment(id, callback, false)).start();
            return;
        }

        Document document = punishmentCollection.find(Filters.eq("uuid", id.toString())).first();

        if (document == null) {
            callback.call(null);
            return;
        }
            Punishment punishment = new Punishment(id,
                BridgeShared.getProfileManager().getProfileByUUIDOrCreate(UUID.fromString(document.getString("target"))),
                BridgeShared.getProfileManager().getProfileByUUIDOrCreate(UUID.fromString(document.getString("executor"))),
                (document.get("pardonedBy") == null ? null : BridgeShared.getProfileManager().getProfileByUUIDOrCreate(UUID.fromString(document.getString("pardonedBy")))),
                PunishmentType.valueOf(document.getString("punishmentType")),
                document.getBoolean("pardoned"),
                document.getBoolean("isIP"),
                document.getBoolean("silent"),
                document.getBoolean("clear"),
                document.getString("punishedServer"),
                document.getString("reason"),
                document.getString("pardonedServer"),
                document.getString("pardonedReason"),
                document.getLong("time"),
                document.getLong("duration"),
                document.getLong("pardonedAt")
        );
//        if(BridgeShared.getSystemType() == SystemType.BUKKIT) BridgeBukkitRef.handlePunishment(callback, callback.isPardoned());
        callback.call(punishment);
    }

    public void savePunishment(Punishment punishment, Callback<Boolean> callback, boolean async) {
        if (async) {
            new Thread(() -> savePunishment(punishment, callback, false)).start();
            return;
        }

        Document document = new Document()
                .append("uuid", punishment.getUuid().toString())
                .append("target", punishment.getTarget().getUuid().toString())
                .append("executor", punishment.getExecutor().getUuid().toString())
                .append("pardonedBy", (punishment.getPardonedBy() == null ? null : punishment.getPardonedBy().getUuid().toString()))
                .append("punishmentType", punishment.getPunishmentType().name())
                .append("pardoned", punishment.isPardoned())
                .append("isIP", punishment.isIP())
                .append("silent", punishment.isSilent())
                .append("clear", punishment.isClear())
                .append("punishedServer", punishment.getPunishedServer())
                .append("reason", punishment.getReason())
                .append("pardonedServer", punishment.getPardonedServer())
                .append("pardonedReason", punishment.getPardonedServer())
                .append("time", punishment.getTime())
                .append("duration", punishment.getDuration())
                .append("pardonedAt", punishment.getPardonedAt());

        punishmentCollection.replaceOne(Filters.eq("uuid", punishment.getUuid().toString()), document, new ReplaceOptions().upsert(true));
        callback.call(true);
    }

    public void removePunishment(UUID id, Callback<Boolean> callback, boolean async) {
        if(async) {
            new Thread(() -> removePunishment(id, callback, false)).start();
            return;
        }

        punishmentCollection.deleteOne(Filters.eq("uuid", id.toString()));
        if(BridgeShared.getPunishmentManager().getPunishmentByID(id) != null) BridgeShared.getPunishmentManager().getPunishments().remove(BridgeShared.getPunishmentManager().getPunishmentByID(id));
        callback.call(true);
    }

}
