package rip.protocol.bridge.bukkit;

import net.minecraft.server.v1_7_R4.*;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import net.minecraft.util.com.mojang.authlib.properties.Property;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import rip.protocol.bridge.BridgeShared;
import rip.protocol.bridge.shared.disguise.Skin;
import rip.protocol.bridge.shared.grant.Grant;
import rip.protocol.bridge.shared.profile.Profile;
import rip.protocol.bridge.shared.ranks.Rank;
import rip.protocol.bridge.shared.utils.MojangUtils;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.protocol.plib.nametag.FrozenNametagHandler;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BukkitAPI {

    private static Pattern uuidPattern = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}");
    public static String LINE = ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------------------------------";

    public static boolean isUUID(String string) {
        return uuidPattern.matcher(string).find();
    }

    public static Player getPlayer(String string) {
        if (string == null) {
            return null;
        } else {
            return isUUID(string) ? Bukkit.getPlayer(UUID.fromString(string)) : Bukkit.getPlayer(string);
        }
    }

    public static Rank getRank(String name) {
        return BridgeShared.getRankManager().getRankByName(name);
    }

    public static Rank getRank(UUID uuid) {
        return BridgeShared.getRankManager().getRankByID(uuid);
    }

    public static Rank createRank(String name) {
        if(BridgeShared.getRankManager().getRankByName(name) != null) {
            return null;
        }
        Rank r = new Rank(UUID.randomUUID(), name, false);
        r.saveRank();
        BridgeShared.getRankManager().addRank(r);
        return r;
    }

    public static Profile getProfile(UUID uuid) {
        if(uuid == null) return Profile.getConsoleProfile();
        return BridgeShared.getProfileManager().getProfileByUUIDOrCreate(uuid);
    }

    public static Profile getProfile(CommandSender sender) {
        return (sender instanceof Player ? BridgeShared.getProfileManager().getProfileByUUIDOrCreate(((Player)sender).getUniqueId()) : Profile.getConsoleProfile());
    }

    public static Profile getProfile(Player player) {
        return BridgeShared.getProfileManager().getProfileByUUIDOrCreate(player.getUniqueId());
    }

    public static Profile getProfile(OfflinePlayer offlinePlayer) {
        return BridgeShared.getProfileManager().getProfileByUUIDOrCreate(offlinePlayer.getUniqueId());
    }

    public static Profile getProfile(String name) {
        return BridgeShared.getProfileManager().getProfileByUsernameOrCreate(name);
    }

    public static Profile getProfileNotCreate(UUID uuid) {
        return BridgeShared.getProfileManager().getProfileByUUID(uuid);
    }

    public static Profile getProfileNotCreate(Player player) {
        return getProfileNotCreate(player.getUniqueId());
    }

    public static Profile getProfileNotCreate(OfflinePlayer player) {
        return getProfileNotCreate(player.getUniqueId());
    }


    @Warning(reason = "This requires to be ran on a seperate thread, otherwise there will be lag on the main server thread.")
    public static Profile getProfileOrCreateNew(UUID uuid) {
        String name;
        try {
            name = MojangUtils.fetchName(uuid);
        }catch(Exception e) {
            return null;
        }
        return BridgeShared.getProfileManager().getNewProfileOrCreate(name, uuid);
    }

    public static Profile getProfileOrCreateNew(String username, UUID uuid) {
        return BridgeShared.getProfileManager().getNewProfileOrCreate(username, uuid);
    }

    public static Rank getPlayerRank(CommandSender player) {
        if(!(player instanceof Player)) {
            return getRank("Owner");
        }

        if(getProfile(player).getDisguisedRank() != null) {
            return getProfile(player).getDisguisedRank();
        }
        return BridgeShared.getProfileManager().getProfileByUUIDOrCreate(((Player)player).getUniqueId()).getCurrentGrant().getRank();
    }

    public static Rank getPlayerRank(Player player) {
        if(getProfile(player).getDisguisedRank() != null) {
            return getProfile(player).getDisguisedRank();
        }
        return BridgeShared.getProfileManager().getProfileByUUIDOrCreate(player.getUniqueId()).getCurrentGrant().getRank();
    }

    public static Rank getPlayerRank(UUID uuid) {

        if(getProfile(uuid).getDisguisedRank() != null) {
            return getProfile(uuid).getDisguisedRank();
        }
        return BridgeShared.getProfileManager().getProfileByUUIDOrCreate(uuid).getCurrentGrant().getRank();
    }

    public static Rank getPlayerRank(OfflinePlayer offlinePlayer) {

        if(getProfile(offlinePlayer).getDisguisedRank() != null) {
            return getProfile(offlinePlayer).getDisguisedRank();
        }
        return BridgeShared.getProfileManager().getProfileByUUIDOrCreate(offlinePlayer.getUniqueId()).getCurrentGrant().getRank();
    }

    public static Rank getPlayerRank(Profile profile) {
        if(profile.getUuid().toString().equals(Profile.getConsoleProfile().getUuid().toString())) {
            return getRank("Owner");
        }
        if(profile.getDisguisedRank() != null) {
            return profile.getDisguisedRank();
        }
        return profile.getCurrentGrant().getRank();
    }

    public static Rank getPlayerRank(Player player, boolean ignoreDisguise) {
        if(ignoreDisguise) return BridgeShared.getProfileManager().getProfileByUUIDOrCreate(player.getUniqueId()).getCurrentGrant().getRank();

        else if(getProfile(player).getDisguisedRank() != null) {
            return getProfile(player).getDisguisedRank();
        }
        return null;

    }

    public static Rank getPlayerRank(UUID uuid, boolean ignoreDisguise) {
        if(ignoreDisguise) return BridgeShared.getProfileManager().getProfileByUUIDOrCreate(uuid).getCurrentGrant().getRank();

        else if(getProfile(uuid).getDisguisedRank() != null) {
            return getProfile(uuid).getDisguisedRank();
        }
        return null;
    }

    public static Rank getPlayerRank(OfflinePlayer offlinePlayer, boolean ignoreDisguise) {
        if(ignoreDisguise) return BridgeShared.getProfileManager().getProfileByUUIDOrCreate(offlinePlayer.getUniqueId()).getCurrentGrant().getRank();

        else if(getProfile(offlinePlayer).getDisguisedRank() != null) {
            return getProfile(offlinePlayer).getDisguisedRank();
        }
        return null;
    }

    public static Rank getPlayerRank(Profile profile, boolean ignoreDisguise) {
        if(ignoreDisguise) return profile.getCurrentGrant().getRank();

        else if(profile.getDisguisedRank() != null) {
            return profile.getDisguisedRank();
        }
        return null;
    }

    public static String getColor(CommandSender sender) {
        if(!getProfile(sender).getColor().equals("")) {
            return getProfile(sender).getColor();
        }else {
            return getPlayerRank(sender).getColor();
        }
    }

    public static String getColor(Player player) {
        if(!getProfile(player).getColor().equals("")) {
            return getProfile(player).getColor();
        }else {
            return getPlayerRank(player).getColor();
        }
    }

    public static String getColor(OfflinePlayer player) {
        if(!getProfile(player).getColor().equals("")) {
            return getProfile(player).getColor();
        }else {
            return getPlayerRank(player).getColor();
        }
    }

    public static String getColor(UUID player) {
        if(!getProfile(player).getColor().equals("")) {
            return getProfile(player).getColor();
        }else {
            return getPlayerRank(player).getColor();
        }
    }

    public static String getColor(Profile profile) {
        if(!profile.getColor().equals("")) {
            return profile.getColor();
        }else {
            return getPlayerRank(profile).getColor();
        }
    }

    public static String getPrefix(Player player) {
        if(!getProfile(player).getPrefix().equals("")) {
            return getProfile(player).getPrefix();
        }else {
            return getPlayerRank(player).getPrefix();
        }
    }

    public static String getPrefix(OfflinePlayer player) {
        if(!getProfile(player).getPrefix().equals("")) {
            return getProfile(player).getPrefix();
        }else {
            return getPlayerRank(player).getPrefix();
        }
    }
    public static String getPrefix(UUID player) {
        if(!getProfile(player).getPrefix().equals("")) {
            return getProfile(player).getPrefix();
        }else {
            return getPlayerRank(player).getPrefix();
        }
    }

    public static String getPrefix(Profile profile) {
        if(!profile.getPrefix().equals("")) {
            return profile.getPrefix();
        }else {
            return getPlayerRank(profile).getPrefix();
        }
    }

    public static String getSuffix(Player player) {
        if(!getProfile(player).getSuffix().equals("")) {
            return getProfile(player).getSuffix();
        }else {
            return getPlayerRank(player).getSuffix();
        }
    }

    public static String getSuffix(OfflinePlayer player) {
        if(!getProfile(player).getSuffix().equals("")) {
            return getProfile(player).getSuffix();
        }else {
            return getPlayerRank(player).getSuffix();
        }
    }

    public static String getSuffix(UUID player) {
        if(!getProfile(player).getSuffix().equals("")) {
            return getProfile(player).getSuffix();
        }else {
            return getPlayerRank(player).getSuffix();
        }
    }

    public static String getSuffix(Profile profile) {
        if(!profile.getSuffix().equals("")) {
            return profile.getSuffix();
        }else {
            return getPlayerRank(profile).getSuffix();
        }
    }

    public static void setupDisguise(Profile profile) {
        setupDisguise(profile, false);
    }

    public static void setupDisguise(Profile profile, boolean clear) {
        Player player = Bukkit.getPlayer(profile.getUuid());
        if(player == null || profile.getDisguise() == null) return;

        Bukkit.getScheduler().runTaskLater(Bridge.getInstance(), () -> {
            try {
                CraftPlayer cp = (CraftPlayer) player;
                EntityPlayer entityPlayer = cp.getHandle();
                GameProfile gp = cp.getProfile();

                Field f = gp.getClass().getDeclaredField("name");
                f.setAccessible(true);

                f.set(gp, clear ? profile.getDisguise().getRealName() : profile.getDisguise().getDisguisedName());

                if(clear) {
                    profile.getDisguise().setSkin(Skin.getSkin(profile.getDisguise().getRealName()));
                    profile.setDisguisedRank(null);
                }


                gp.getProperties().removeAll("textures");
                gp.getProperties().put("textures", new Property("textures", profile.getDisguise().getSkin().getTexture(), profile.getDisguise().getSkin().getSignature()));
                profile.updateColor();
                FrozenNametagHandler.reloadPlayer(player);
                PacketPlayOutPlayerInfo removeInfoPacket = PacketPlayOutPlayerInfo.removePlayer(entityPlayer);
                PacketPlayOutPlayerInfo addInfoPacket = PacketPlayOutPlayerInfo.addPlayer(entityPlayer);

                PacketPlayOutPosition positionPacket = new PacketPlayOutPosition(entityPlayer.locX,
                        entityPlayer.locY,
                        entityPlayer.locZ,
                        entityPlayer.yaw,
                        entityPlayer.pitch, true);

                PacketPlayOutRespawn respawnPacket = new PacketPlayOutRespawn(entityPlayer.dimension,
                        entityPlayer.world.difficulty,
                        entityPlayer.world.worldData.getType(),
                        entityPlayer.playerInteractManager.getGameMode());

                // send packets
                Arrays.asList(removeInfoPacket, addInfoPacket, respawnPacket, positionPacket).forEach(packet -> sendPacket(player, packet));

                Bukkit.getOnlinePlayers().forEach(o -> {
                    o.hidePlayer(player);
                    o.showPlayer(player);
                });
                if(clear) {
                    profile.setDisguise(null);
                    profile.saveProfile();
                }
                player.updateInventory();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }, 20);


    }

    public static void sendPacket(Player player, Packet packet){
        CraftPlayer cp = (CraftPlayer) player;
        cp.getHandle().playerConnection.sendPacket(packet);
    }

    public static List<Grant> getActiveGrants(Player player) {
        return BridgeShared.getProfileManager().getProfileByUUIDOrCreate(player.getUniqueId()).getActiveGrants();
    }

    public static List<Grant> getActiveGrants(UUID uuid) {
        return BridgeShared.getProfileManager().getProfileByUUIDOrCreate(uuid).getActiveGrants();
    }

    public static List<Grant> getActiveGrants(OfflinePlayer offlinePlayer) {
        return BridgeShared.getProfileManager().getProfileByUUIDOrCreate(offlinePlayer.getUniqueId()).getActiveGrants();
    }

    public static List<Grant> getActiveGrants(Profile profile) {
        return profile.getActiveGrants();
    }

    public static List<Grant> getCurrentScopeRanks(Player player) {
        return BridgeShared.getProfileManager().getProfileByUUIDOrCreate(player.getUniqueId()).getActiveGrants().stream().filter(Grant::isGrantActiveOnScope).collect(Collectors.toList());
    }

    public static List<Grant> getCurrentScopeRanks(UUID uuid) {
        return BridgeShared.getProfileManager().getProfileByUUIDOrCreate(uuid).getActiveGrants().stream().filter(Grant::isGrantActiveOnScope).collect(Collectors.toList());
    }

    public static List<Grant> getCurrentScopeRanks(OfflinePlayer offlinePlayer) {
        return BridgeShared.getProfileManager().getProfileByUUIDOrCreate(offlinePlayer.getUniqueId()).getActiveGrants().stream().filter(Grant::isGrantActiveOnScope).collect(Collectors.toList());
    }

    public static List<Grant> getAllGrants(Player player) {
        return BridgeShared.getProfileManager().getProfileByUUIDOrCreate(player.getUniqueId()).getGrants();
    }

    public static List<Grant> getAllGrants(UUID uuid) {
        return BridgeShared.getProfileManager().getProfileByUUIDOrCreate(uuid).getGrants();
    }

    public static List<Grant> getAllGrants(OfflinePlayer offlinePlayer) {
        return BridgeShared.getProfileManager().getProfileByUUIDOrCreate(offlinePlayer.getUniqueId()).getGrants();
    }

    public static void sendToStaff(String message) {
        BridgeShared.sendLog(message);
        Bukkit.getOnlinePlayers().stream().filter(p -> getPlayerRank(p, true).isStaff()).forEach(p -> {
            ((Player) p).sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        });
    }

    public static void sendToStaff(List<String> message) {
        message.forEach(BridgeShared::sendLog);
        Bukkit.getOnlinePlayers().stream().filter(p -> getPlayerRank(p, true).isStaff()).forEach(p -> {
            message.forEach(string -> {
                ((Player) p).sendMessage(ChatColor.translateAlternateColorCodes('&', string));
            });
        });
    }

}
