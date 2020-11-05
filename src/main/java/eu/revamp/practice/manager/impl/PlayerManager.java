package eu.revamp.practice.manager.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import eu.revamp.practice.kit.CustomKit;
import eu.revamp.practice.kit.Kit;
import eu.revamp.practice.party.Party;
import eu.revamp.practice.player.PracticeProfile;
import eu.revamp.practice.util.enums.Items;
import eu.revamp.practice.manager.Manager;
import eu.revamp.practice.manager.ManagerHandler;
import eu.revamp.spigot.utils.chat.color.CC;
import eu.revamp.spigot.utils.item.ItemBuilder;
import eu.revamp.spigot.utils.serialize.BukkitSerilization;
import org.bson.Document;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

public class PlayerManager extends Manager {

    public PlayerManager(ManagerHandler managerHandler) {
        super(managerHandler);
    }

    public void resetPlayer(Player player) {
        if (player == null || !player.isOnline()) return;

        PracticeProfile practiceProfile = this.managerHandler.getProfileManager().getProfile(player);

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.updateInventory();

        player.getActivePotionEffects().clear();
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setExhaustion(0);
        player.setSaturation(5);
        player.setLevel(0);
        player.setExp(0f);
        player.setFireTicks(0);
        practiceProfile.setLastEnderpearl(0);
        practiceProfile.setBardEnergy(0);
        practiceProfile.setLastBardBuff(0);
        practiceProfile.setLastArcherSpeedBuff(0);
        practiceProfile.setLastArcherJumpBuff(0);
        practiceProfile.setLastRogueBackstab(0);
        practiceProfile.setLastRogueSpeedBuff(0);
        practiceProfile.setLastRogueJumpBuff(0);
        practiceProfile.getPotionEffectsList().clear();

        player.getActivePotionEffects().forEach(p -> player.removePotionEffect(p.getType()));
    }

    public void giveItems(Player player, boolean hide) {
        if (player == null || !player.isOnline()) return;

        resetPlayer(player);

        PracticeProfile practiceProfile = this.managerHandler.getProfileManager().getProfile(player);

        switch (practiceProfile.getPlayerState()) {
            case LOBBY: {
                player.getInventory().setItem(Items.UNRANKED.getSlot(), Items.UNRANKED.getItem());
                player.getInventory().setItem(Items.RANKED.getSlot(), Items.RANKED.getItem());
                player.getInventory().setItem(Items.PARTY.getSlot(), Items.PARTY.getItem());
                player.getInventory().setItem(Items.SETTINGS.getSlot(), Items.SETTINGS.getItem());
                player.getInventory().setItem(Items.EDITKIT.getSlot(), Items.EDITKIT.getItem());
                player.getInventory().setItem(Items.LEADERBOARDS.getSlot(), Items.LEADERBOARDS.getItem());
                break;
            }
            case QUEUE: {
                player.getInventory().setItem(Items.LEAVE_QUEUE.getSlot(), Items.LEAVE_QUEUE.getItem());
                break;
            }
            case PARTY: {
                Party party = practiceProfile.getParty();

                if (party.getLeader() == player.getUniqueId()) {
                    player.getInventory().setItem(Items.PARTY_EVENTS.getSlot(), Items.PARTY_EVENTS.getItem());
                    player.getInventory().setItem(Items.HCF_SELECTOR.getSlot(), Items.HCF_SELECTOR.getItem());
                    player.getInventory().setItem(Items.LEADER_PARTY_INFO.getSlot(), Items.LEADER_PARTY_INFO.getItem());
                    player.getInventory().setItem(Items.DISBAND_PARTY.getSlot(), Items.DISBAND_PARTY.getItem());
                } else {
                    player.getInventory().setItem(Items.PLAYER_PARTY_INFO.getSlot(), Items.PLAYER_PARTY_INFO.getItem());
                    player.getInventory().setItem(Items.LEAVE_PARTY.getSlot(), Items.LEAVE_PARTY.getItem());
                }
                break;
            }
            case EVENT:
            case SPECTATING_EVENT: {
                player.getInventory().setItem(Items.LEAVE_EVENT.getSlot(), Items.LEAVE_EVENT.getItem());
                break;
            }
            case TOURNAMENT: {
                player.getInventory().setItem(Items.LEAVE_TOURNAMENT.getSlot(), Items.LEAVE_TOURNAMENT.getItem());
                break;
            }
            case SPECTATING: {
                player.getInventory().setItem(Items.STOP_SPECTATING.getSlot(), Items.STOP_SPECTATING.getItem());
                break;
            }
        }
        player.updateInventory();
        player.setGameMode(GameMode.SURVIVAL);

        if (hide) {
            if (!this.managerHandler.getSettingsManager().isShowPlayers()) {
                hideAll(player);
                hideAll1(player);
            } else {
                showAll(player);
            }
        }
    }

    public void teleportSpawn(Player player) {
        if (managerHandler.getSettingsManager().getSpawn() != null)
            player.teleport(managerHandler.getSettingsManager().getSpawn());
    }

    public void save(Player player) {
        PracticeProfile practiceProfile = this.managerHandler.getProfileManager().getProfile(player);

        this.managerHandler.getKitManager().getKitsList().forEach(kit -> {
            if (managerHandler.getMongoManager().collectionExists(kit.getName())) {
                MongoCollection<Document> mongoCollection = managerHandler.getMongoManager().getMongoDatabase().getCollection(kit.getName());

                Map<String, Object> documentMap = new HashMap<>();
                documentMap.put("uuid", player.getUniqueId().toString());
                documentMap.put("username", player.getName());
                documentMap.put("elo", practiceProfile.getElo(kit));

                for (int i = 1; i <= 7; i++) {
                    CustomKit customKit = practiceProfile.getCustomKit(kit, i);

                    if (customKit != null) {
                        documentMap.put("kit-" + i + "-name", customKit.getName());
                        documentMap.put("kit-" + i + "-inventory", BukkitSerilization.toBase64(customKit.getInventory()));
                        documentMap.put("kit-" + i + "-armor", BukkitSerilization.itemStackArrayToBase64(customKit.getArmor()));
                    }
                }

                Document document = mongoCollection.find(Filters.eq("uuid", player.getUniqueId().toString())).first();

                if (document != null) mongoCollection.deleteOne(document);

                mongoCollection.insertOne(new Document(documentMap));
            }
        });
    }

    public void openEditLayoutInventory(Player player) {
        Inventory inventory = this.managerHandler.getPlugin().getServer().createInventory(null, 36, CC.translate(this.managerHandler.getPlugin().getConfig().getString("menus.EDITLAYOUT.name")));

        PracticeProfile practiceProfile = this.managerHandler.getProfileManager().getProfile(player);
        practiceProfile.setEditLayoutInventory(inventory);

        player.openInventory(inventory);
    }

    public void updateEditLayout(Player player, Kit kit) {
        Map<String, ItemStack> itemMap = new HashMap<>();

        PracticeProfile practiceProfile = this.managerHandler.getProfileManager().getProfile(player);

        Inventory inventory = practiceProfile.getEditLayoutInventory();

        this.managerHandler.getPlugin().getConfig().getConfigurationSection("menus.EDITLAYOUT.items").getKeys(false).forEach(i -> {
            String[] material = managerHandler.getPlugin().getConfig().getString("menus.EDITLAYOUT.items." + i + ".id").split(":");
            String name = CC.translate(managerHandler.getPlugin().getConfig().getString("menus.EDITLAYOUT.items." + i + ".name"));

            itemMap.put(i, new ItemBuilder(Material.getMaterial(Integer.parseInt(material[0]))).setDurability((short) Integer.parseInt(material[1])).setName(name).toItemStack());
        });

        for (int i = 1; i <= 7; i++) {
            String saveKitName = itemMap.get("save-kit").clone().getItemMeta().getDisplayName().replace("%kit%", kit.getName()).replace("%number%", String.valueOf(i));
            ItemStack saveKit = new ItemBuilder(itemMap.get("save-kit").clone()).setName(saveKitName).toItemStack();
            inventory.setItem(i, saveKit);

            String loadKitName = itemMap.get("load-kit").clone().getItemMeta().getDisplayName().replace("%kit%", kit.getName()).replace("%number%", String.valueOf(i));
            ItemStack loadKit = new ItemBuilder(itemMap.get("load-kit").clone()).setName(loadKitName).toItemStack();

            String renameKitName = itemMap.get("rename-kit").clone().getItemMeta().getDisplayName().replace("%kit%", kit.getName()).replace("%number%", String.valueOf(i));
            ItemStack renameKit = new ItemBuilder(itemMap.get("rename-kit").clone()).setName(renameKitName).toItemStack();

            String removeKitName = itemMap.get("remove-kit").clone().getItemMeta().getDisplayName().replace("%kit%", kit.getName()).replace("%number%", String.valueOf(i));
            ItemStack removeKit = new ItemBuilder(itemMap.get("remove-kit").clone()).setName(removeKitName).toItemStack();

            if (practiceProfile.getCustomKit(kit, i) != null) {
                inventory.setItem(i + 9, loadKit);
                inventory.setItem(i + 18, renameKit);
                inventory.setItem(i + 27, removeKit);
            } else {
                inventory.setItem(i + 9, new ItemStack(Material.AIR));
                inventory.setItem(i + 18, new ItemStack(Material.AIR));
                inventory.setItem(i + 27, new ItemStack(Material.AIR));
            }
        }
    }

    public void openPartyEvents(Player player) {
        Inventory inventory = this.managerHandler.getPlugin().getServer().createInventory(null, 9 * this.managerHandler.getPlugin().getConfig().getInt("menus.PARTYEVENTS.rows"), CC.translate(this.managerHandler.getPlugin().getConfig().getString("menus.PARTYEVENTS.name")));

        Map<String, ItemStack> itemMap = new HashMap<>();
        this.managerHandler.getPlugin().getConfig().getConfigurationSection("menus.PARTYEVENTS.items").getKeys(false).forEach(i -> {
            String[] material = managerHandler.getPlugin().getConfig().getString("menus.PARTYEVENTS.items." + i + ".id").split(":");
            String name = CC.translate(managerHandler.getPlugin().getConfig().getString("menus.PARTYEVENTS.items." + i + ".name"));

            itemMap.put(i, new ItemBuilder(Material.getMaterial(Integer.parseInt(material[0]))).setDurability((short) Integer.parseInt(material[1])).setName(name).toItemStack());
        });

        inventory.setItem(this.managerHandler.getPlugin().getConfig().getInt("menus.PARTYEVENTS.items.ffa.slot"), itemMap.get("ffa"));
        inventory.setItem(this.managerHandler.getPlugin().getConfig().getInt("menus.PARTYEVENTS.items.split.slot"), itemMap.get("split"));
        inventory.setItem(this.managerHandler.getPlugin().getConfig().getInt("menus.PARTYEVENTS.items.duel.slot"), itemMap.get("duel"));

        player.openInventory(inventory);
    }

    public void openPartyDuel(Player player) {
        Inventory inventory = this.managerHandler.getPlugin().getServer().createInventory(null, 9 * this.managerHandler.getPlugin().getConfig().getInt("menus.PARTYDUEL.rows"), CC.translate(this.managerHandler.getPlugin().getConfig().getString("menus.PARTYDUEL.name")));

        Map<String, ItemStack> itemMap = new HashMap<>();
        this.managerHandler.getPlugin().getConfig().getConfigurationSection("menus.PARTYDUEL.items").getKeys(false).forEach(i -> {
            String[] material = managerHandler.getPlugin().getConfig().getString("menus.PARTYDUEL.items." + i + ".id").split(":");
            String name = CC.translate(managerHandler.getPlugin().getConfig().getString("menus.PARTYDUEL.items." + i + ".name"));

            itemMap.put(i, new ItemBuilder(Material.getMaterial(Integer.parseInt(material[0]))).setDurability((short) Integer.parseInt(material[1])).setName(name).toItemStack());
        });

        inventory.setItem(this.managerHandler.getPlugin().getConfig().getInt("menus.PARTYDUEL.items.regular.slot"), itemMap.get("regular"));
        inventory.setItem(this.managerHandler.getPlugin().getConfig().getInt("menus.PARTYDUEL.items.hcf.slot"), itemMap.get("hcf"));

        player.openInventory(inventory);
    }

    public void openHCFSelector(Player player) {
        Inventory inventory = this.managerHandler.getPlugin().getServer().createInventory(null, 9 * this.managerHandler.getPlugin().getConfig().getInt("menus.HCFKITS.rows"), CC.translate(this.managerHandler.getPlugin().getConfig().getString("menus.HCFKITS.name")));

        PracticeProfile practiceProfile = this.managerHandler.getProfileManager().getProfile(player);
        practiceProfile.setHcfKitSelectorInventory(inventory);

        player.openInventory(inventory);
    }

    public void updateHCFInventory(Player player) {
        PracticeProfile practiceProfile = this.managerHandler.getProfileManager().getProfile(player);

        Inventory inventory = practiceProfile.getHcfKitSelectorInventory();
        inventory.clear();

        Map<String, ItemStack> itemMap = new HashMap<>();
        this.managerHandler.getPlugin().getConfig().getConfigurationSection("menus.HCFKITS.items").getKeys(false).forEach(i -> {
            String[] material = managerHandler.getPlugin().getConfig().getString("menus.HCFKITS.items." + i + ".id").split(":");
            String name = CC.translate(managerHandler.getPlugin().getConfig().getString("menus.HCFKITS.items." + i + ".name"));

            itemMap.put(i, new ItemBuilder(Material.getMaterial(Integer.parseInt(material[0]))).setDurability((short) Integer.parseInt(material[1])).setName(name).toItemStack());
        });

        Party party = practiceProfile.getParty();

        party.getMembers().forEach(u -> {
            Player member = this.managerHandler.getPlugin().getServer().getPlayer(u);
            PracticeProfile memberProfile = this.managerHandler.getProfileManager().getProfile(member);

            ItemStack clone = itemMap.get("player").clone();
            inventory.addItem(new ItemBuilder(clone.getType()).setDurability(clone.getDurability()).setName(clone.getItemMeta().getDisplayName().replace("%player%", member.getName()).replace("%kit%", memberProfile.getHcfKit().getName())).toItemStack());
        });
    }

    public PotionEffect getPotionEffect(Player player, PotionEffectType potionEffectType) {
        for (PotionEffect potionEffect : player.getActivePotionEffects()) {
            if (potionEffect.getType().equals(potionEffectType)) {
                return potionEffect;
            }
        }
        return null;
    }

    public void hideAll(Player player) {
        this.managerHandler.getPlugin().getServer().getOnlinePlayers().forEach(player::hidePlayer);
    }

    public void hideAll1(Player player) {
        this.managerHandler.getPlugin().getServer().getOnlinePlayers().forEach(p -> {
            p.hidePlayer(player);
        });
    }

    public void showAll(Player player) {
        this.managerHandler.getPlugin().getServer().getOnlinePlayers().forEach(player::showPlayer);
    }
}
