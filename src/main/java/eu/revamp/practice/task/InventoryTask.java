package eu.revamp.practice.task;

import eu.revamp.practice.RevampPractice;
import eu.revamp.practice.kit.Kit;
import eu.revamp.practice.match.Match;
import eu.revamp.practice.player.PlayerState;
import eu.revamp.spigot.utils.chat.color.CC;
import eu.revamp.spigot.utils.item.ItemBuilder;
import eu.revamp.spigot.utils.player.PlayerUtils;
import lombok.AllArgsConstructor;
import eu.revamp.practice.player.LeaderboardPlayer;
import eu.revamp.practice.player.PracticeProfile;
import eu.revamp.practice.util.enums.Menus;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

@SuppressWarnings("Duplicates")
@AllArgsConstructor
public class InventoryTask extends BukkitRunnable {

    private final RevampPractice plugin;

    @Override
    public void run() {

        this.plugin.getManagerHandler().getKitManager().getKitsList().stream().filter(Kit::isUnranked).forEach(k -> {

            List<String> lore = new ArrayList<>();
            for (String itemLore : Menus.UNRANKED.getItemLore()) {
                lore.add(itemLore.replace("%queue%", String.valueOf(k.getUnrankedQueue().size())).replace("%rankedMatch%", String.valueOf(k.getRankedMatch().size())).replace("%rankedQueue%", String.valueOf(k.getRankedQueue().size())).replace("%match%", String.valueOf(k.getUnrankedMatch().size())).replace("%kit%", k.getName()));
            }
            Menus.UNRANKED.getInventory().setItem(getUnrankedIndex(k), new ItemBuilder(k.getDisplay().getType(), k.getUnrankedMatch().size()).setDurability(k.getDisplay().getDurability()).setName(Menus.UNRANKED.getItemPrefix() + k.getName()).setLore(lore).toItemStack());

        });

        this.plugin.getManagerHandler().getKitManager().getKitsList().stream().filter(Kit::isRanked).forEach(k -> {

            if (k.isUnranked()) {
                List<String> lore = new ArrayList<>();
                for (String itemLore : Menus.RANKED.getItemLore()) {
                    lore.add(itemLore.replace("%queue%", String.valueOf(k.getRankedQueue().size())).replace("%unrankedMatch%", String.valueOf(k.getUnrankedMatch().size())).replace("%unrankedQueue%", String.valueOf(k.getUnrankedQueue().size())).replace("%match%", String.valueOf(k.getRankedMatch().size())).replace("%kit%", k.getName()));
                }
                this.plugin.getManagerHandler().getKitManager().getKitsList().indexOf(k);
                Menus.RANKED.getInventory().setItem(getRankedIndex(k), new ItemBuilder(k.getDisplay().getType(), k.getRankedMatch().size()).setDurability(k.getDisplay().getDurability()).setName(Menus.RANKED.getItemPrefix() + k.getName()).setLore(lore).toItemStack());
            }

        });

        this.plugin.getManagerHandler().getKitManager().getKitsList().stream().filter(Kit::isEditable).forEach(k -> {

            List<String> lore = new ArrayList<>();
            for (String itemLore : Menus.EDITKIT.getItemLore()) {
                lore.add(itemLore.replace("%kit%", k.getName()));
            }
            Menus.EDITKIT.getInventory().setItem(getEditIndex(k), new ItemBuilder(k.getDisplay().getType(), 1).setDurability(k.getDisplay().getDurability()).setName(Menus.EDITKIT.getItemPrefix() + k.getName()).setLore(lore).toItemStack());
        });

        this.plugin.getManagerHandler().getKitManager().getKitsList().forEach(k -> {

            List<String> lore = new ArrayList<>();
            for (String itemLore : Menus.PARTYFFA.getItemLore()) {
                lore.add(itemLore.replace("%kit%", k.getName()));
            }
            Menus.PARTYFFA.getInventory().setItem(this.plugin.getManagerHandler().getKitManager().getKitsList().indexOf(k), new ItemBuilder(k.getDisplay().getType(), 1).setDurability(k.getDisplay().getDurability()).setName(Menus.PARTYFFA.getItemPrefix() + k.getName()).setLore(lore).toItemStack());
        });

        this.plugin.getManagerHandler().getKitManager().getKitsList().forEach(k -> {

            List<String> lore = new ArrayList<>();
            for (String itemLore : Menus.PARTYSPLIT.getItemLore()) {
                lore.add(itemLore.replace("%kit%", k.getName()));
            }
            Menus.PARTYSPLIT.getInventory().setItem(this.plugin.getManagerHandler().getKitManager().getKitsList().indexOf(k), new ItemBuilder(k.getDisplay().getType(), 1).setDurability(k.getDisplay().getDurability()).setName(Menus.PARTYSPLIT.getItemPrefix() + k.getName()).setLore(lore).toItemStack());
        });

        //ADDED PARTY DUEL && SPECTACTOR GUI
        List<Player> players = PlayerUtils.getOnlinePlayers();
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);
            if (practiceProfile.getParty() != null) {
                if (practiceProfile.getParty().getLeader() == player.getUniqueId()) {
                    List<String> lore = new ArrayList<>();
                    if (player.getName().endsWith("s") || player.getName().endsWith("S")) {
                        lore.add(CC.GREEN + "Click here to duel " + player.getName() + "' party.");
                    } else {
                        lore.add(CC.GREEN + "Click here to duel " + player.getName() + "'s party.");
                    }
                    Menus.OTHERPARTIES.getInventory().setItem(i, new ItemBuilder(Material.SKULL_ITEM, 1).setSkullOwner(player.getName()).setName(CC.YELLOW + player.getName()).setLore(lore).toItemStack());
                }
                if (practiceProfile.getPlayerState() == PlayerState.MATCH || practiceProfile.getPlayerState() == PlayerState.PARTY) {
                    Match match = practiceProfile.getPlayerState() == PlayerState.MATCH ? practiceProfile.getMatch() : practiceProfile.getParty().getMatch();
                    if (match != null) {
                        List<String> lore = new ArrayList<>();
                        lore.add(CC.GREEN + "Click here to spectate " + player.getName() + ".");
                        Menus.MATCHES.getInventory().setItem(i, new ItemBuilder(Material.SKULL_ITEM, 1).setSkullOwner(player.getName()).setName(CC.YELLOW + player.getName()).setLore(lore).toItemStack());
                    }
                }
            }
        }

/*
        PlayerUtils.getOnlinePlayers().forEach(player -> {
            PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);
            if (practiceProfile.getParty() != null) {
                //System.out.println("profile != null " + player.getName());
                if (practiceProfile.getParty().getLeader() == player.getUniqueId()) {
                    //System.out.println("mmmmmmmmmm " + player.getName());
                    List<String> lore = new ArrayList<>();
                    if (player.getName().endsWith("s") || player.getName().endsWith("S")) {
                        lore.add(CC.GREEN + "Click here to duel " + player.getName() + "' party.");
                    } else {
                        lore.add(CC.GREEN + "Click here to duel " + player.getName() + "'s party.");
                    }
                    //System.out.println("non ha senso lmfao");
                    Menus.OTHERPARTIES.getInventory().addItem(new ItemBuilder(Material.SKULL_ITEM, 1).setSkullOwner(player.getName()).setName(CC.YELLOW + player.getName()).setLore(lore).toItemStack());
                }
            }
            //System.out.println("runna");
        });*/
        //ADDED PARTY DUEL

        this.plugin.getManagerHandler().getKitManager().getKitsList().forEach(k -> {

            List<String> lore = new ArrayList<>();
            for (String itemLore : Menus.DUEL.getItemLore()) {
                lore.add(itemLore.replace("%kit%", k.getName()));
            }
            Menus.DUEL.getInventory().setItem(this.plugin.getManagerHandler().getKitManager().getKitsList().indexOf(k), new ItemBuilder(k.getDisplay().getType(), 1).setDurability(k.getDisplay().getDurability()).setName(Menus.DUEL.getItemPrefix() + k.getName()).setLore(lore).toItemStack());
        });

        this.plugin.getManagerHandler().getKitManager().getKitsList().forEach(k -> {

            List<String> lore = new ArrayList<>();
            for (String itemLore : Menus.TOURNAMENT.getItemLore()) {
                lore.add(itemLore.replace("%kit%", k.getName()));
            }
            Menus.TOURNAMENT.getInventory().setItem(this.plugin.getManagerHandler().getKitManager().getKitsList().indexOf(k), new ItemBuilder(k.getDisplay().getType(), 1).setDurability(k.getDisplay().getDurability()).setName(Menus.TOURNAMENT.getItemPrefix() + k.getName()).setLore(lore).toItemStack());
        });

        updateLeaderboards();
    }

    public int getUnrankedIndex(Kit kit) {
        int i = -1;
        for (Kit k : this.plugin.getManagerHandler().getKitManager().getKitsList()) {
            if (k.isUnranked()) {
                i += 1;
                if (k == kit) {
                    return i;
                }
            }
        }
        return 100;
    }

    public int getRankedIndex(Kit kit) {
        int i = -1;
        for (Kit k : this.plugin.getManagerHandler().getKitManager().getKitsList()) {
            if (k.isRanked()) {
                i += 1;
                if (k == kit) {
                    return i;
                }
            }
        }
        return 100;
    }

    public int getEditIndex(Kit kit) {
        int i = -1;
        for (Kit k : this.plugin.getManagerHandler().getKitManager().getKitsList()) {
            if (k.isEditable()) {
                i += 1;
                if (k == kit) {
                    return i;
                }
            }
        }
        return 100;
    }

    private void updateLeaderboards() {
        if (!this.plugin.getManagerHandler().getSettingsManager().isLeaderboardsGUI()) {
            return;
        }
        for (Kit kit : this.plugin.getManagerHandler().getKitManager().getKitsList()) {
            if (!kit.isRanked()) {
                continue;
            }
            List<LeaderboardPlayer> leaderboardPlayers = new ArrayList<>();

            this.plugin.getServer().getOnlinePlayers().forEach(p -> {
                PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(p);

                leaderboardPlayers.add(new LeaderboardPlayer(p.getName(), practiceProfile.getElo(kit)));
            });


            for (Document playerDocument : this.plugin.getManagerHandler().getMongoManager().getMongoDatabase().getCollection(kit.getName()).find()) {
                UUID uuid = UUID.fromString(playerDocument.getString("uuid"));

                if (this.plugin.getServer().getPlayer(uuid) == null) {
                    String username = playerDocument.getString("username");
                    int elo = playerDocument.getInteger("elo");

                    leaderboardPlayers.add(new LeaderboardPlayer(username, elo));
                }
            }
            int start = 0;
            int end = 9;

            leaderboardPlayers.sort(Comparator.comparing(LeaderboardPlayer::getElo));
            Collections.reverse(leaderboardPlayers);
            List<String> lore = new ArrayList<>();
            for (int i = start; i <= end; i++) {
                if (i < leaderboardPlayers.size()) {
                    LeaderboardPlayer leaderboardPlayer = leaderboardPlayers.get(i);

                    lore.add(this.plugin.getConfig().getString("leaderboards-lore").replace("%pos%", String.valueOf(i + 1)).replace("%player%", leaderboardPlayer.getName()).replace("%elo%", String.valueOf(leaderboardPlayer.getElo())));
                }
            }
            Menus.LEADERBOARDS.getInventory().setItem(getRankedIndex(kit), new ItemBuilder(kit.getDisplay()).setName(Menus.LEADERBOARDS.getItemPrefix() + kit.getName()).setLore(lore).toItemStack());
        }
    }
}
