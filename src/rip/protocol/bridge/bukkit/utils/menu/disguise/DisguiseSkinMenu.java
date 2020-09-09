package rip.protocol.bridge.bukkit.utils.menu.disguise;

import lombok.AllArgsConstructor;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import net.minecraft.util.com.mojang.authlib.properties.Property;
import net.minecraft.util.org.apache.commons.io.IOUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.*;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import rip.protocol.bridge.bukkit.Bridge;
import rip.protocol.bridge.bukkit.BukkitAPI;
import rip.protocol.bridge.shared.disguise.Disguise;
import rip.protocol.bridge.shared.disguise.Skin;
import rip.protocol.bridge.shared.profile.Profile;
import rip.protocol.bridge.shared.ranks.Rank;
import rip.protocol.hcf.Foxtrot;
import rip.protocol.plib.menu.Button;
import rip.protocol.plib.menu.pagination.PaginatedMenu;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.regex.Pattern;

public class DisguiseSkinMenu extends PaginatedMenu {

    private String nickName;
    private Rank rank;

    public DisguiseSkinMenu(String nickName, Rank rank) {
        this.nickName = nickName;
        this.rank = rank;
    }

    @Override
    public String getPrePaginatedTitle(Player player) {
        return ChatColor.GOLD + "Select a Skin";
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttonMap = new HashMap<>();

        int i = 0;
        for (Skin skin : Skin.getSkins()) {
            buttonMap.put(i, new Button() {
                @Override
                public String getName(Player player) {
                    return ChatColor.YELLOW + skin.getSkinName();
                }

                @Override
                public List<String> getDescription(Player player) {
                    return Collections.singletonList("");
                }

                @Override
                public Material getMaterial(Player player) {
                    return Material.SKULL_ITEM;
                }

                @Override
                public void clicked(Player player, int slot, ClickType clickType) {
                    player.closeInventory();
                    complete(player, skin);
                }
            });
            i++;
        }

        return buttonMap;


    }


    @Override
    public Map<Integer, Button> getGlobalButtons(Player player) {
        Map<Integer, Button> buttonMap = new HashMap<>();
        buttonMap.put(3, new Button() {
            @Override
            public String getName(Player player) {
                return ChatColor.YELLOW + "Custom Skin";
            }

            @Override
            public List<String> getDescription(Player player) {
                return Arrays.asList(
                        ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "------------------------",
                        ChatColor.YELLOW + "Type in a custom skin to disguise as.",
                        ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "------------------------"
                );
            }

            @Override
            public Material getMaterial(Player player) {
                return Material.SIGN;
            }

            @Override
            public void clicked(Player player, int slot, ClickType clickType) {
                player.closeInventory();
                ConversationFactory factory = new ConversationFactory(Bridge.getInstance()).withModality(true).withPrefix(new NullConversationPrefix()).withFirstPrompt(new StringPrompt() {

                    public String getPromptText(ConversationContext context) {
                        return ChatColor.GREEN + "Type in the username of the players skin you wish to have.";
                    }

                    @Override
                    public Prompt acceptInput(ConversationContext cc, String s) {

                        if(!Pattern.compile("^\\w{1,16}$").matcher(s).matches()) {
                            player.sendMessage(ChatColor.RED + "That is not a valid username.");
                            new DisguiseSkinMenu(nickName, rank).openMenu(player);
                            return Prompt.END_OF_CONVERSATION;
                        }



                        new Thread(() -> {
                            Skin skin = Skin.getSkin(s);
                            if(skin == null) {
                                try {
                                    JSONParser jsonParser = new JSONParser();
                                    String reponse = getResponse("https://api.minetools.eu/uuid/" + s);
                                    JSONObject parsed = (JSONObject) jsonParser.parse(reponse);

                                    String uuid = (String) parsed.get("id");
                                    reponse = getResponse("https://api.minetools.eu/profile/" + uuid);
                                    parsed = (JSONObject) jsonParser.parse(reponse);
                                    JSONObject raw = (JSONObject) parsed.get("raw");
                                    JSONObject properties = (JSONObject) ((JSONArray) raw.get("properties")).get(0);
                                    String value = (String) properties.get("value");
                                    String signature = (String) properties.get("signature");

                                    skin = new Skin(s, value, signature);
                                    skin.save();



                                }catch (Exception e) {
                                    player.sendMessage(ChatColor.RED + "No such player with the username \"" + s + "\" exists.");
                                    new DisguiseSkinMenu(nickName, rank).openMenu(player);
                                }
                            }

                            complete(player, skin);

                        }).start();

                        return Prompt.END_OF_CONVERSATION;
                    }

                }).withLocalEcho(false).withEscapeSequence("/no").withTimeout(10).thatExcludesNonPlayersWithMessage("Go away evil console!");

                Conversation con = factory.buildConversation(player);
                player.beginConversation(con);
            }
        });

        buttonMap.put(5, new Button() {
            @Override
            public String getName(Player player) {
                return ChatColor.YELLOW + "Random Skin";
            }

            @Override
            public List<String> getDescription(Player player) {
                return Arrays.asList(
                        ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "------------------------",
                        ChatColor.YELLOW + "Click to disguise as a random skin.",
                        ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "------------------------"
                );
            }

            @Override
            public Material getMaterial(Player player) {
                return Material.SKULL_ITEM;
            }

            @Override
            public void clicked(Player player, int slot, ClickType clickType) {
                player.closeInventory();
                Random random = new Random();
                Skin skin = Skin.getSkins().get(random.nextInt(Skin.getSkins().size()));
                complete(player, skin);
            }
        });
        return buttonMap;
    }

    public void complete(Player player, Skin skin) {
        Profile profile = BukkitAPI.getProfile(player);
        if(profile == null) {
            player.sendMessage(ChatColor.RED + "Failed to complete the task.");
            return;
        }



        CraftPlayer cp = (CraftPlayer) player;
        EntityPlayer entityPlayer = cp.getHandle();
        GameProfile gp = cp.getProfile();

        Property property = gp.getProperties().get("textures").iterator().next();

        if(Skin.getSkin(player.getName()) == null) {
            Skin playerSkin = new Skin(player.getName(), property.getValue(), property.getSignature());
            playerSkin.save();
        }


        profile.setDisguisedRank(rank);
        profile.setDisguise(new Disguise(player.getName(), nickName, skin));
        profile.saveProfile();

        BukkitAPI.setupDisguise(profile);
        player.sendMessage(ChatColor.GREEN + "You are now known as " + rank.getColor() + nickName + ChatColor.GREEN + " with the skin: " + ChatColor.WHITE + skin.getSkinName() + ChatColor.GREEN + "!");
    }

    public static String getResponse(String _url){
        try {
            URL url = new URL(_url);
            URLConnection con = url.openConnection();
            InputStream in = con.getInputStream();
            String encoding = con.getContentEncoding();
            encoding = encoding == null ? "UTF-8" : encoding;
            return IOUtils.toString(in, encoding);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
