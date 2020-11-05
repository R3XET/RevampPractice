package eu.revamp.practice.listener;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import eu.revamp.practice.RevampPractice;
import eu.revamp.practice.hostedevent.HostedEvent;
import eu.revamp.practice.hostedevent.impl.SumoEvent;
import eu.revamp.practice.kit.CustomKit;
import eu.revamp.practice.kit.Kit;
import eu.revamp.practice.kit.KitType;
import eu.revamp.practice.kit.hcf.HCFKit;
import eu.revamp.practice.match.Match;
import eu.revamp.practice.match.MatchDeathReason;
import eu.revamp.practice.match.MatchRequest;
import eu.revamp.practice.match.MatchState;
import eu.revamp.practice.party.Party;
import eu.revamp.practice.party.PartyState;
import eu.revamp.practice.player.PlayerState;
import eu.revamp.practice.player.PracticeProfile;
import eu.revamp.practice.queue.Queue;
import eu.revamp.practice.tournament.Tournament;
import eu.revamp.practice.util.ListUtil;
import eu.revamp.practice.util.enums.Items;
import eu.revamp.practice.util.enums.Menus;
import eu.revamp.practice.util.enums.Messages;
import eu.revamp.practice.util.misc.InventorySnapshot;
import eu.revamp.practice.util.misc.ScoreHelper;
import eu.revamp.spigot.RevampSpigot;
import eu.revamp.spigot.knockback.Knockback;
import eu.revamp.spigot.utils.chat.color.CC;
import eu.revamp.spigot.utils.generic.Tasks;
import eu.revamp.spigot.utils.item.ItemBuilder;
import eu.revamp.spigot.utils.player.PlayerUtils;
import eu.revamp.spigot.utils.serialize.BukkitSerilization;
import lombok.AllArgsConstructor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("Duplicates")
@AllArgsConstructor
public class PlayerListener implements Listener {

    private final RevampPractice plugin;

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        this.plugin.getManagerHandler().getProfileManager().addPlayer(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        this.plugin.getManagerHandler().getPlayerManager().giveItems(player, true);
        this.plugin.getManagerHandler().getPlayerManager().teleportSpawn(player);

        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);

        Knockback knockback = RevampSpigot.getInstance().getKnockbackManager().getKnockbackProfile("default");
        CraftPlayer player2 = (CraftPlayer) player;
        player2.setKnockback(knockback);


        this.plugin.getManagerHandler().getKitManager().getKitsList().forEach(kit -> {
            if (this.plugin.getManagerHandler().getMongoManager().collectionExists(kit.getName())) {
                MongoCollection<Document> mongoCollection = this.plugin.getManagerHandler().getMongoManager().getMongoDatabase().getCollection(kit.getName());


                Document document = mongoCollection.find(Filters.eq("uuid", player.getUniqueId().toString())).first();

                if (document != null) {
                    practiceProfile.setElo(kit, document.getInteger("elo"));

                    for (int i = 1; i <= 7; i++) {
                        if (document.get("kit-" + i + "-inventory") != null && document.get("kit-" + i + "-armor") != null) {

                            practiceProfile.setCustomKit(kit, new CustomKit(BukkitSerilization.fromBase64(document.getString("kit-" + i + "-inventory")), BukkitSerilization.itemStackArrayFromBase64(document.getString("kit-" + i + "-armor")), i, document.getString("kit-" + i + "-name")));
                        }
                    }
                }
            }
        });

        ScoreHelper.createScore(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);
        try {
            switch (practiceProfile.getPlayerState()) {
                case QUEUE: {
                    Queue queue = practiceProfile.getQueue();
                    queue.getKit().getUnrankedQueue().remove(player.getUniqueId());
                    queue.getKit().getRankedQueue().remove(player.getUniqueId());
                    break;
                }
                case MATCH: {
                    Match match = practiceProfile.getMatch();
                    match.addDeath(player, MatchDeathReason.DISCONNECTED, null);
                    break;
                }
                case PARTY: {
                    Party party = practiceProfile.getParty();
                    if (party.getLeader() == player.getUniqueId()) {
                        this.plugin.getServer().dispatchCommand(player, "party disband");
                    } else if (party.getPartyState() == PartyState.MATCH) {
                        Match match = party.getMatch();
                        match.addDeath(player, MatchDeathReason.DISCONNECTED, null);
                        party.getMembers().remove(player.getUniqueId());
                        break;
                    }
                }
                case SPECTATING: {
                    practiceProfile.setPlayerState(PlayerState.LOBBY);

                    practiceProfile.getSpectating().getSpectators().remove(player.getUniqueId());
                    if (!practiceProfile.isSilentMode())
                        practiceProfile.getSpectating().broadcast(Messages.PLAYER_NO_LONGER_SPECTATING.getMessage().replace("%player%", player.getName()));

                    practiceProfile.setSpectating(null);
                    break;
                }
                case SPECTATING_EVENT:
                case EVENT: {
                    this.plugin.getServer().dispatchCommand(player, "event leave");
                    break;
                }
            }
        } catch (Exception e) {
            //remove errors :3
        }
        this.plugin.getManagerHandler().getPlayerManager().save(player);
        this.plugin.getManagerHandler().getProfileManager().removePlayer(player);
        ScoreHelper.removeScore(player);

        //if (Menus.MATCHES.getInventory() == null) return;
        //if (Menus.MATCHES.getInventory().getContents() == null) return;
        Menus.MATCHES.getInventory().forEach(itemStack -> {
            if (itemStack != null) {
                if (itemStack.getItemMeta() != null) {
                    if (itemStack.getItemMeta().getDisplayName().contains(player.getName())) {
                        Menus.MATCHES.getInventory().remove(itemStack);
                    }
                }
            }
        });

        //if (Menus.OTHERPARTIES.getInventory() == null) return;
        //if (Menus.OTHERPARTIES.getInventory().getContents() == null) return;
        Menus.OTHERPARTIES.getInventory().forEach(itemStack -> {
            if (itemStack != null) {
                if (itemStack.getItemMeta() != null) {
                    if (itemStack.getItemMeta().getDisplayName().contains(player.getName())) {
                        Menus.OTHERPARTIES.getInventory().remove(itemStack);
                    }
                }
            }
        });

        player.setAllowFlight(false);
        player.setFlying(false);


    }

    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);

        practiceProfile.setOpenInventory(true);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);

        if (practiceProfile.getEditInv() != null) {
            Kit kit = practiceProfile.getEditInv();
            practiceProfile.setEditInv(null);

            player.sendMessage(Messages.KIT_EDITINV_UPDATED.getMessage().replace("%kit%", kit.getName()));
        }

        practiceProfile.setOpenInventory(false);
        practiceProfile.setViewingPlayerInv(false);
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDestroyByEntity(HangingBreakByEntityEvent event) {
        if (event.getRemover() instanceof Player) {
            Player player = (Player)event.getRemover();
            PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);
            if ((event.getEntity().getType() == EntityType.ITEM_FRAME && !practiceProfile.isBuildMode())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void OnPlaceByEntity(HangingPlaceEvent event) {
        Player player = event.getPlayer();
        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);
        if (event.getEntity().getType() == EntityType.ITEM_FRAME && !practiceProfile.isBuildMode()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void canRotate(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        Player player = event.getPlayer();
        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);
        if (!entity.getType().equals(EntityType.ITEM_FRAME)) {
            return;
        }
        ItemFrame iFrame = (ItemFrame)entity;
        if (iFrame.getItem() == null || iFrame.getItem().getType().equals(Material.AIR)) {
            return;
        }
        if (!practiceProfile.isBuildMode()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void ItemRemoval(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            Player player = (Player)e.getDamager();
            PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);
            if (e.getEntity().getType() == EntityType.ITEM_FRAME && !practiceProfile.isBuildMode()) {
                e.setCancelled(true);
            }
        }
        if (e.getDamager() instanceof Projectile && e.getEntity().getType() == EntityType.ITEM_FRAME) {
            Projectile p2 = (Projectile)e.getDamager();
            Player player = (Player)p2.getShooter();
            PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);
            if (!practiceProfile.isBuildMode()) {
                e.setCancelled(true);
            }
        }
    }


    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);
        Material material = player.getItemInHand().getType();
        if (practiceProfile.getMatch() != null){
            Match match = practiceProfile.getMatch();
            if (match.getMatchState() == MatchState.STARTING){
                if (material == Material.POTION) {
                    List<Short> potionIds = new ArrayList<>();
                    potionIds.add((short) 16385);
                    potionIds.add((short) 16386);
                    potionIds.add((short) 16387);
                    potionIds.add((short) 16388);
                    potionIds.add((short) 16389);
                    potionIds.add((short) 16390);
                    potionIds.add((short) 16392);
                    potionIds.add((short) 16393);
                    potionIds.add((short) 16394);
                    potionIds.add((short) 16396);
                    potionIds.add((short) 16397);
                    potionIds.add((short) 16398);
                    potionIds.add((short) 16417);
                    potionIds.add((short) 16418);
                    potionIds.add((short) 16420);
                    potionIds.add((short) 16421);
                    potionIds.add((short) 16425);
                    potionIds.add((short) 16427);
                    potionIds.add((short) 16428);
                    potionIds.add((short) 16449);
                    potionIds.add((short) 16450);
                    potionIds.add((short) 16451);
                    potionIds.add((short) 16452);
                    potionIds.add((short) 16454);
                    potionIds.add((short) 16456);
                    potionIds.add((short) 16457);
                    potionIds.add((short) 16458);
                    potionIds.add((short) 16459);
                    potionIds.add((short) 16462);
                    potionIds.add((short) 16481);
                    potionIds.add((short) 16482);
                    potionIds.add((short) 16484);
                    potionIds.add((short) 16489);
                    if (potionIds.contains(player.getItemInHand().getDurability())) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }


        if (practiceProfile.getPlayerState() == PlayerState.EDITING && (material == Material.POTION || material == Material.ENDER_PEARL || material == Material.BUCKET || material == Material.LAVA_BUCKET || material == Material.MILK_BUCKET || material == Material.WATER_BUCKET || material == Material.ITEM_FRAME || material == Material.ARMOR_STAND || material == Material.TRAP_DOOR)){
            event.setCancelled(true);
            return;
        }
        Block block = event.getClickedBlock();



        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {

            // Added an option to prevent players from opening trapdoors at spawn
            if (practiceProfile.getPlayerState() == PlayerState.LOBBY || practiceProfile.getPlayerState() == PlayerState.QUEUE || practiceProfile.getPlayerState() == PlayerState.PARTY){
                if (block != null) {
                    if (block.getType() == Material.TRAP_DOOR) {
                        event.setCancelled(true);
                    }
                }
            }

            if (practiceProfile.getPlayerState() == PlayerState.EDITING && block != null) {
                switch (block.getType()) {
                    case ANVIL: {
                        event.setCancelled(true);

                        this.plugin.getManagerHandler().getPlayerManager().openEditLayoutInventory(player);
                        this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
                            this.plugin.getManagerHandler().getPlayerManager().updateEditLayout(player, practiceProfile.getEditing());
                        }, 1L);
                        break;
                    }
                    case SIGN:
                    case SIGN_POST:
                    case WALL_SIGN: {
                        event.setCancelled(true);

                        practiceProfile.setPlayerState(PlayerState.LOBBY);
                        practiceProfile.setEditing(null);
                        practiceProfile.setSettingName(null);

                        this.plugin.getManagerHandler().getPlayerManager().giveItems(player, true);
                        this.plugin.getManagerHandler().getPlayerManager().teleportSpawn(player);
                        break;
                    }
                    case CHEST: {
                        event.setCancelled(true);

                        if (practiceProfile.getEditing().isEditChest()) {
                            player.openInventory(BukkitSerilization.cloneInventory(practiceProfile.getEditing().getEditInventory()));
                        }
                        break;
                    }
                }
            }

            if (practiceProfile.getPlayerState() == PlayerState.MATCH || practiceProfile.getPlayerState() == PlayerState.PARTY) {
                Match match = practiceProfile.getPlayerState() == PlayerState.MATCH ? practiceProfile.getMatch() : practiceProfile.getParty().getMatch();

                if (player.getItemInHand().getType() == Items.CUSTOM_KIT.getItem().getType() && player.getItemInHand().getDurability() == Items.CUSTOM_KIT.getItem().getDurability() && match != null) {
                    event.setCancelled(true);
                    int kit = player.getInventory().getHeldItemSlot() + 1;

                    if (practiceProfile.getCustomKit(match.getKit(), kit) != null) {
                        CustomKit customKit = practiceProfile.getCustomKit(match.getKit(), kit);

                        this.plugin.getManagerHandler().getPlayerManager().resetPlayer(player);

                        player.getInventory().setContents(customKit.getInventory().getContents());
                        player.getInventory().setArmorContents(customKit.getArmor());
                        player.updateInventory();

                        player.sendMessage(Messages.EQUIPPED_CUSTOM_KIT.getMessage().replace("%name%", customKit.getName().replace("%kit%", match.getKit().getName()).replace("%number%", String.valueOf(kit))));
                        return;
                    }
                }
                if (player.getItemInHand().equals(Items.DEFAULT_KIT.getItem()) && match != null) {
                    event.setCancelled(true);

                    player.getInventory().setContents(match.getKit().getInventory().getContents());
                    player.getInventory().setArmorContents(match.getKit().getArmor());
                    player.updateInventory();

                    player.sendMessage(Messages.EQUIPPED_DEFAULT_KIT.getMessage().replace("%kit%", match.getKit().getName()));
                    return;
                }
            }
            if (player.getItemInHand().equals(Items.UNRANKED.getItem())) {
                event.setCancelled(true);

                player.openInventory(Menus.UNRANKED.getInventory());
                return;
            }
            if (player.getItemInHand().equals(Items.RANKED.getItem())) {
                event.setCancelled(true);

                player.openInventory(Menus.RANKED.getInventory());
                return;
            }
            if (player.getItemInHand().equals(Items.LEAVE_QUEUE.getItem())) {
                event.setCancelled(true);

                for (Kit kit : this.plugin.getManagerHandler().getKitManager().getKitsList()) {
                    kit.getUnrankedQueue().remove(player.getUniqueId());
                    kit.getRankedQueue().remove(player.getUniqueId());
                }

                Queue queue = practiceProfile.getQueue();
                practiceProfile.setQueue(null);
                practiceProfile.setPlayerState(PlayerState.LOBBY);

                player.sendMessage(Messages.LEFT_QUEUE.getMessage().replace("%queueType%", queue.isRanked() ? "Ranked" : "Unranked").replace("%kit%", queue.getKit().getName()));
                this.plugin.getManagerHandler().getPlayerManager().giveItems(player, true);
                return;
            }
            if (player.getItemInHand().equals(Items.SETTINGS.getItem())) {
                event.setCancelled(true);

                player.openInventory(Menus.SETTINGS.getInventory());
                return;
            }
            if (player.getItemInHand().equals(Items.EDITKIT.getItem())) {
                event.setCancelled(true);

                player.openInventory(Menus.EDITKIT.getInventory());
                return;
            }
            if (player.getItemInHand().equals(Items.LEADERBOARDS.getItem())) {
                event.setCancelled(true);

                player.openInventory(Menus.LEADERBOARDS.getInventory());
                return;
            }
            if (player.getItemInHand().equals(Items.PARTY.getItem())) {
                event.setCancelled(true);

                this.plugin.getServer().dispatchCommand(player, "party create");
                return;
            }
            if (player.getItemInHand().equals(Items.DISBAND_PARTY.getItem())) {
                event.setCancelled(true);

                this.plugin.getServer().dispatchCommand(player, "party disband");
                return;
            }
            if (player.getItemInHand().equals(Items.LEAVE_PARTY.getItem())) {
                event.setCancelled(true);

                this.plugin.getServer().dispatchCommand(player, "party leave");
                return;
            }
            if (player.getItemInHand().equals(Items.LEADER_PARTY_INFO.getItem())) {
                event.setCancelled(true);

                this.plugin.getServer().dispatchCommand(player, "party info");
                return;
            }
            if (player.getItemInHand().equals(Items.PLAYER_PARTY_INFO.getItem())) {
                event.setCancelled(true);

                this.plugin.getServer().dispatchCommand(player, "party info");
                return;
            }
            if (player.getItemInHand().equals(Items.PARTY_EVENTS.getItem())) {
                event.setCancelled(true);

                this.plugin.getManagerHandler().getPlayerManager().openPartyEvents(player);
                return;
            }
            if (player.getItemInHand().equals(Items.HCF_SELECTOR.getItem())) {
                event.setCancelled(true);

                this.plugin.getManagerHandler().getPlayerManager().openHCFSelector(player);
                this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> this.plugin.getManagerHandler().getPlayerManager().updateHCFInventory(player), 1L);
                return;
            }
            if (player.getItemInHand().equals(Items.LEAVE_EVENT.getItem())) {
                event.setCancelled(true);

                this.plugin.getServer().dispatchCommand(player, "event leave");

                player.setAllowFlight(false);
                player.setFlying(false);
                return;
            }
            if (player.getItemInHand().equals(Items.LEAVE_TOURNAMENT.getItem())) {
                event.setCancelled(true);

                this.plugin.getServer().dispatchCommand(player, "tournament leave");

                player.setAllowFlight(false);
                player.setFlying(false);
                return;
            }
            if (player.getItemInHand().equals(Items.STOP_SPECTATING.getItem())) {
                event.setCancelled(true);

                practiceProfile.setPlayerState(PlayerState.LOBBY);

                practiceProfile.getSpectating().getSpectators().remove(player.getUniqueId());
                if (!practiceProfile.isSilentMode())
                    practiceProfile.getSpectating().broadcast(Messages.PLAYER_NO_LONGER_SPECTATING.getMessage().replace("%player%", player.getName()));

                practiceProfile.setSpectating(null);

                player.setAllowFlight(false);
                player.setFlying(false);


                this.plugin.getManagerHandler().getPlayerManager().giveItems(player, true);
                this.plugin.getManagerHandler().getPlayerManager().teleportSpawn(player);

                player.sendMessage(Messages.NO_LONGER_SPECTATING.getMessage());
                return;
            }
            if (player.getItemInHand().getType() == Material.MUSHROOM_SOUP) {
                event.setCancelled(true);

                if (player.getHealth() >= 20) {
                    return;
                }

                double newHealth = player.getHealth() + 7;
                player.setHealth(newHealth >= 20 ? player.getMaxHealth() : newHealth);

                player.setItemInHand(new ItemStack(Material.BOWL));
                player.updateInventory();
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false) //TODO ADDED PRIORITY
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);


        try {
            if (event.getInventory().getName().startsWith(CC.translate("&7Inventory of"))){
                event.setCancelled(true);
                if (event.getSlot() == RevampPractice.getInstance().getConfig().getInt("menus.SNAPSHOT.items.switch-inventory.slot")){
                    player.closeInventory();
                    Player target = practiceProfile.getLastPlayerMatch().get(player);
                    if (event.getInventory().getName().contains(player.getName())){
                        if (InventorySnapshot.getByUUID(target.getUniqueId()) == null) {
                            player.sendMessage(Messages.INVENTORY_NOT_FOUND.getMessage());
                            return;
                        }
                        player.openInventory(InventorySnapshot.getByUUID(target.getUniqueId()).getInventory());
                    }
                    else {
                        if (InventorySnapshot.getByUUID(player.getUniqueId()) == null) {
                            player.sendMessage(Messages.INVENTORY_NOT_FOUND.getMessage());
                            return;
                        }
                        player.openInventory(InventorySnapshot.getByUUID(player.getUniqueId()).getInventory());
                    }
                    //practiceProfile.setViewingPlayerInv(true);
                }
                return;
            }

            if (event.getInventory().equals(Menus.LEADERBOARDS.getInventory())) {
                event.setCancelled(true);
                return;
            }
            if (event.getInventory().equals(Menus.SETTINGS.getInventory())) {
                List<String> stringList = new ArrayList<>(this.plugin.getConfig().getConfigurationSection("menus.SETTINGS.items").getKeys(false));

                event.setCancelled(true);

                player.chat(this.plugin.getConfig().getString("menus.SETTINGS.items." + stringList.get(event.getSlot()) + ".command"));
                return;
            }
            if (event.getInventory().equals(Menus.UNRANKED.getInventory())) {

                event.setCancelled(true);
                String kitName = event.getCurrentItem().getItemMeta().getDisplayName().substring(Menus.UNRANKED.getItemPrefix().length());

                Kit kit = this.plugin.getManagerHandler().getKitManager().getKit(kitName);

                if (kit == null) {
                    return;
                }

                player.closeInventory();

                if (kit.getUnrankedQueue().contains(player.getUniqueId())) {
                    return;
                }

                if (kit.getUnrankedQueue().size() == 0) {
                    kit.getUnrankedQueue().add(player.getUniqueId());
                    practiceProfile.setQueue(new Queue(kit, false, System.currentTimeMillis()));
                    practiceProfile.setPlayerState(PlayerState.QUEUE);

                    player.sendMessage(Messages.JOINED_UNRANKED_QUEUE.getMessage().replace("%kit%", kitName));
                    this.plugin.getManagerHandler().getPlayerManager().giveItems(player, true);
                    return;
                }
                UUID foundUUID = kit.getUnrankedQueue().get(0);
                if (foundUUID == player.getUniqueId()) {
                    return;
                }
                Match match = new Match(this.plugin, kit, ListUtil.newList(player.getUniqueId()), ListUtil.newList(foundUUID), false, false);
                match.start();

                kit.getUnrankedQueue().remove(foundUUID);
                kit.getUnrankedMatch().add(player.getUniqueId());
                kit.getUnrankedMatch().add(foundUUID);

                PracticeProfile foundProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(this.plugin.getServer().getPlayer(foundUUID));
                foundProfile.setPlayerState(PlayerState.MATCH);
                foundProfile.setMatch(match);
                practiceProfile.setPlayerState(PlayerState.MATCH);
                practiceProfile.setMatch(match);
                return;
            }

            if (event.getInventory().equals(Menus.RANKED.getInventory())) {

                event.setCancelled(true);

                String kitName = event.getCurrentItem().getItemMeta().getDisplayName().substring(Menus.RANKED.getItemPrefix().length());

                Kit kit = this.plugin.getManagerHandler().getKitManager().getKit(kitName);

                if (kit == null) {
                    return;
                }

                player.closeInventory();

                if (kit.getRankedQueue().contains(player.getUniqueId())) {
                    return;
                }

                Queue queue = new Queue(kit, true, System.currentTimeMillis());

                queue.setMinElo(practiceProfile.getElo(kit));
                queue.setMaxElo(practiceProfile.getElo(kit));

                practiceProfile.setQueue(queue);
                practiceProfile.setPlayerState(PlayerState.QUEUE);

                kit.getRankedQueue().add(player.getUniqueId());

                player.sendMessage(Messages.JOINED_RANKED_QUEUE.getMessage().replace("%kit%", kitName).replace("%elo%", String.valueOf(practiceProfile.getElo(kit))));
                this.plugin.getManagerHandler().getPlayerManager().giveItems(player, true);

                new BukkitRunnable() {
                    public void run() {
                        if (practiceProfile.getPlayerState() != PlayerState.QUEUE || !kit.getRankedQueue().contains(player.getUniqueId())) {
                            this.cancel();
                            return;
                        }

                        for (int i = 0; i < kit.getRankedQueue().size(); i++) {
                            UUID foundUUID = kit.getRankedQueue().get(i);

                            if (player.getUniqueId() != foundUUID) {

                                Player foundPlayer = plugin.getServer().getPlayer(foundUUID);
                                PracticeProfile foundProfile = plugin.getManagerHandler().getProfileManager().getProfile(foundPlayer);
                                Queue foundQueue = foundProfile.getQueue();

                                if (queue.getMinElo() <= foundProfile.getElo(kit) && queue.getMaxElo() >= foundProfile.getElo(kit) && foundQueue.getMinElo() <= practiceProfile.getElo(kit) && foundQueue.getMaxElo() >= practiceProfile.getElo(kit)) {
                                    Match match = new Match(plugin, kit, ListUtil.newList(player.getUniqueId()), ListUtil.newList(foundUUID), true, false);
                                    match.start();

                                    kit.getRankedQueue().remove(player.getUniqueId());
                                    kit.getRankedQueue().remove(foundUUID);
                                    kit.getRankedMatch().add(player.getUniqueId());
                                    kit.getRankedMatch().add(foundUUID);

                                    foundProfile.setPlayerState(PlayerState.MATCH);
                                    foundProfile.setMatch(match);
                                    practiceProfile.setPlayerState(PlayerState.MATCH);
                                    practiceProfile.setMatch(match);

                                    this.cancel();
                                    return;
                                }
                            }
                        }
                        queue.setMinElo(queue.getMinElo() - 20 <= plugin.getManagerHandler().getSettingsManager().getMinElo() ? plugin.getManagerHandler().getSettingsManager().getMinElo() : queue.getMinElo() - 20);
                        queue.setMaxElo(queue.getMaxElo() + 20 >= plugin.getManagerHandler().getSettingsManager().getMaxElo() ? plugin.getManagerHandler().getSettingsManager().getMaxElo() : queue.getMaxElo() + 20);
                    }
                }.runTaskTimer(this.plugin, 20L, 20L);
            }
            if (event.getInventory().equals(Menus.EDITKIT.getInventory())) {

                event.setCancelled(true);

                String kitName = event.getCurrentItem().getItemMeta().getDisplayName().substring(Menus.EDITKIT.getItemPrefix().length());

                Kit kit = this.plugin.getManagerHandler().getKitManager().getKit(kitName);

                if (kit == null) {
                    return;
                }

                player.closeInventory();

                if (kit.getRankedQueue().contains(player.getUniqueId())) {
                    return;
                }

                practiceProfile.setEditing(kit);
                practiceProfile.setPlayerState(PlayerState.EDITING);

                this.plugin.getManagerHandler().getPlayerManager().resetPlayer(player);
                if (!kit.isEditChest()) {
                    player.getInventory().setContents(kit.getInventory().getContents());
                    player.getInventory().setArmorContents(kit.getArmor());
                }
                player.updateInventory();

                player.teleport(this.plugin.getManagerHandler().getSettingsManager().getEditor());
                return;
            }
            if (event.getInventory().getName().equalsIgnoreCase(CC.translate(this.plugin.getConfig().getString("menus.EDITLAYOUT.name"))) && event.getCurrentItem().getType() != Material.AIR) { //fixes air click bug
                if (event.getSlot() >= 1 && event.getSlot() <= 7) {
                    event.setCancelled(true);
                    CustomKit customKit = practiceProfile.getCustomKit(practiceProfile.getEditing(), event.getSlot());
                    practiceProfile.setCustomKit(practiceProfile.getEditing(), new CustomKit(BukkitSerilization.cloneInventory(player.getInventory()), BukkitSerilization.cloneArmor(player.getInventory().getArmorContents()), event.getSlot(), CC.translate(customKit != null ? customKit.getName() : Items.CUSTOM_KIT.getItem().getItemMeta().getDisplayName())));
                    this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
                        this.plugin.getManagerHandler().getPlayerManager().updateEditLayout(player, practiceProfile.getEditing());
                    }, 1L);

                    player.sendMessage(Messages.SAVED_CUSTOM_KIT.getMessage().replace("%kit%", practiceProfile.getEditing().getName()).replace("%number%", String.valueOf(event.getSlot())));
                    return;
                }
                if (event.getSlot() >= 10 && event.getSlot() <= 16) {
                    event.setCancelled(true);
                    CustomKit customKit = practiceProfile.getCustomKit(practiceProfile.getEditing(), event.getSlot() - 9);

                    player.getInventory().setContents(customKit.getInventory().getContents());
                    player.getInventory().setArmorContents(customKit.getArmor());
                    player.updateInventory();

                    player.sendMessage(Messages.LOADED_CUSTOM_KIT.getMessage().replace("%kit%", practiceProfile.getEditing().getName()).replace("%number%", String.valueOf(event.getSlot() - 9)));
                    return;
                }
                if (event.getSlot() >= 19 && event.getSlot() <= 25) {
                    event.setCancelled(true);
                    player.closeInventory();

                    CustomKit customKit = practiceProfile.getCustomKit(practiceProfile.getEditing(), event.getSlot() - 18);

                    practiceProfile.setSettingName(customKit);

                    player.sendMessage(Messages.SET_CUSTOM_NAME.getMessage());
                    return;
                }
                if (event.getSlot() >= 28 && event.getSlot() <= 34) {
                    event.setCancelled(true);
                    practiceProfile.removeCustomKit(practiceProfile.getEditing(), event.getSlot() - 27);
                    this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
                        this.plugin.getManagerHandler().getPlayerManager().updateEditLayout(player, practiceProfile.getEditing());
                    }, 1L);

                    player.sendMessage(Messages.DELETED_CUSTOM_KIT.getMessage().replace("%kit%", practiceProfile.getEditing().getName()).replace("%number%", String.valueOf(event.getSlot() - 27)));
                    return;
                }
            }
            if (event.getInventory().getName().equalsIgnoreCase(CC.translate(this.plugin.getConfig().getString("menus.PARTYEVENTS.name")))) {
                event.setCancelled(true);

                if (event.getSlot() == this.plugin.getConfig().getInt("menus.PARTYEVENTS.items.ffa.slot")) {
                    player.closeInventory();

                    player.openInventory(Menus.PARTYFFA.getInventory());
                    return;
                }
                if (event.getSlot() == this.plugin.getConfig().getInt("menus.PARTYEVENTS.items.split.slot")) {
                    player.closeInventory();

                    player.openInventory(Menus.PARTYSPLIT.getInventory());
                    return;
                }
                if (event.getSlot() == this.plugin.getConfig().getInt("menus.PARTYEVENTS.items.duel.slot")) {
                    player.closeInventory();

                    player.openInventory(Menus.OTHERPARTIES.getInventory());
                    return;
                }
            }

            //ADDED DUELS
            if (event.getInventory().getName().equalsIgnoreCase(CC.translate(this.plugin.getConfig().getString("menus.OTHERPARTIES.name")))) {
                event.setCancelled(true);
                if (event.getCurrentItem().getType() != Material.AIR) { //fixes air click bug
                    Tasks.run(this.plugin, () -> player.performCommand("duel " + event.getCurrentItem().getItemMeta().getDisplayName().substring(2)));
                    return;
                }
            }

            //ADDED SPECTATE GUI
            if (event.getInventory().getName().equalsIgnoreCase(CC.translate(this.plugin.getConfig().getString("menus.MATCHES.name")))) {
                event.setCancelled(true);
                if (event.getCurrentItem().getType() != Material.AIR) { //fixes air click bug
                    Tasks.run(this.plugin, () -> player.performCommand("spectate " + event.getCurrentItem().getItemMeta().getDisplayName().substring(2)));
                    return;
                }
            }



            if (event.getInventory().equals(Menus.PARTYFFA.getInventory())) {
                event.setCancelled(true);

                String kitName = event.getCurrentItem().getItemMeta().getDisplayName().substring(Menus.PARTYFFA.getItemPrefix().length());

                Kit kit = this.plugin.getManagerHandler().getKitManager().getKit(kitName);

                if (kit == null) {
                    return;
                }

                player.closeInventory();

                Party party = practiceProfile.getParty();
                if (party.getMembers().size() < 2) {
                    player.sendMessage(Messages.MUST_NEED_TWO_MEMBERS.getMessage());
                    return;
                }
                Match match = new Match(this.plugin, kit, party.getMembers(), null, false, false);
                match.start();

                party.setMatch(match);
                party.setPartyState(PartyState.MATCH);
                return;
            }
            if (event.getInventory().equals(Menus.PARTYSPLIT.getInventory())) {
                event.setCancelled(true);


                String kitName = event.getCurrentItem().getItemMeta().getDisplayName().substring(Menus.PARTYSPLIT.getItemPrefix().length());

                Kit kit = this.plugin.getManagerHandler().getKitManager().getKit(kitName);

                if (kit == null) {
                    return;
                }

                player.closeInventory();

                Party party = practiceProfile.getParty();
                if (party.getMembers().size() < 2) {
                    player.sendMessage(Messages.MUST_NEED_TWO_MEMBERS.getMessage());
                    return;
                }

                List<UUID> t1 = new ArrayList<>(party.getMembers());
                List<UUID> t2 = new ArrayList<>();

                Collections.shuffle(t1);

                int half = Math.round(t1.size() / 2);
                for (int i = 0; i < half; i++) {
                    t2.add(t1.get(i));
                    t1.remove(t1.get(i));
                }

                Match match = new Match(this.plugin, kit, t1, t2, false, false);
                match.start();

                party.setMatch(match);
                party.setPartyState(PartyState.MATCH);
                return;
            }
            if (event.getInventory().getName().equalsIgnoreCase(CC.translate(this.plugin.getConfig().getString("menus.HCFKITS.name")))) {
                event.setCancelled(true);

                Party party = practiceProfile.getParty();

                Player member = this.plugin.getServer().getPlayer(party.getMembers().get(event.getSlot()));
                PracticeProfile memberProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(member);

                switch (memberProfile.getHcfKit()) {
                    case DIAMOND: {
                        memberProfile.setHcfKit(HCFKit.ARCHER);
                        break;
                    }
                    case ARCHER: {
                        memberProfile.setHcfKit(HCFKit.BARD);
                        break;
                    }
                    case BARD: {
                        memberProfile.setHcfKit(HCFKit.ROGUE);
                        break;
                    }
                    case ROGUE: {
                        memberProfile.setHcfKit(HCFKit.DIAMOND);
                        break;
                    }
                }
                this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> this.plugin.getManagerHandler().getPlayerManager().updateHCFInventory(player));
                return;
            }
            if (event.getInventory().getName().equalsIgnoreCase(CC.translate(this.plugin.getConfig().getString("menus.PARTYDUEL.name")))) {
                event.setCancelled(true);

                if (event.getSlot() == this.plugin.getConfig().getInt("menus.PARTYDUEL.items.regular.slot")) {
                    player.closeInventory();

                    player.openInventory(Menus.DUEL.getInventory());
                    return;
                }
                if (event.getSlot() == this.plugin.getConfig().getInt("menus.PARTYDUEL.items.hcf.slot")) {
                    player.closeInventory();

                    if (practiceProfile.getPlayerState() == PlayerState.PARTY) {
                        Party party = practiceProfile.getParty();

                        Party dueling = party.getDueling();

                        if (dueling.getMembers().size() == 0) {
                            player.sendMessage(Messages.PARTY_DOESNT_EXIST.getMessage());
                            return;
                        }
                        if (dueling.getPartyState() != PartyState.LOBBY) {
                            player.sendMessage(Messages.PARTY_NOT_IN_LOBBY.getMessage());
                            return;
                        }
                        player.closeInventory();

                        dueling.getMatchRequestList().add(new MatchRequest(player.getUniqueId(), null, true));
                        Player leader = this.plugin.getServer().getPlayer(dueling.getLeader());
                        party.broadcast(Messages.SENT_PARTY_DUEL.getMessage().replace("%player%", leader.getName()).replace("%kit%", "HCF"));

                        TextComponent clickable = new TextComponent(Messages.PARTY_DUEL_REQUEST.getMessage().replace("%player%", player.getName()).replace("%amount%", String.valueOf(party.getMembers().size())).replace("%kit%", "HCF"));
                        clickable.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Messages.PARTY_DUEL_HOVER.getMessage().replace("%player%", player.getName())).create()));
                        clickable.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/accept " + player.getName()));

                        dueling.broadcast(clickable);
                        return;
                    }
                }
            }
            if (event.getInventory().equals(Menus.DUEL.getInventory())) {
                event.setCancelled(true);

                String kitName = event.getCurrentItem().getItemMeta().getDisplayName().substring(Menus.DUEL.getItemPrefix().length());

                Kit kit = this.plugin.getManagerHandler().getKitManager().getKit(kitName);

                if (kit == null) {
                    return;
                }

                if (practiceProfile.getPlayerState() == PlayerState.PARTY) {
                    Party party = practiceProfile.getParty();

                    Party dueling = party.getDueling();

                    if (dueling.getMembers().size() == 0) {
                        player.sendMessage(Messages.PARTY_DOESNT_EXIST.getMessage());
                        return;
                    }
                    if (dueling.getPartyState() != PartyState.LOBBY) {
                        player.sendMessage(Messages.PARTY_NOT_IN_LOBBY.getMessage());
                        return;
                    }
                    player.closeInventory();

                    dueling.getMatchRequestList().add(new MatchRequest(player.getUniqueId(), kit, true));
                    Player leader = this.plugin.getServer().getPlayer(dueling.getLeader());
                    party.broadcast(Messages.SENT_PARTY_DUEL.getMessage().replace("%player%", leader.getName()).replace("%kit%", kit.getName()));

                    TextComponent clickable = new TextComponent(Messages.PARTY_DUEL_REQUEST.getMessage().replace("%player%", player.getName()).replace("%amount%", String.valueOf(party.getMembers().size())).replace("%kit%", kit.getName()));
                    clickable.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Messages.PARTY_DUEL_HOVER.getMessage().replace("%player%", player.getName())).create()));
                    clickable.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/accept " + player.getName()));

                    dueling.broadcast(clickable);
                    return;
                }

                Player dueling = practiceProfile.getDueling();
                if (!dueling.isOnline()) {
                    player.sendMessage(Messages.COULD_NOT_FIND_PLAYER.getMessage().replace("%player%", dueling.getName()));
                    return;
                }

                PracticeProfile duelingProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(dueling);
                if (duelingProfile.getPlayerState() != PlayerState.LOBBY) {
                    player.sendMessage(Messages.PLAYER_NOT_IN_LOBBY.getMessage().replace("%player%", dueling.getName()));
                    return;
                }

                player.closeInventory();

                duelingProfile.getMatchRequestList().add(new MatchRequest(player.getUniqueId(), kit, false));

                player.sendMessage(Messages.SENT_DUEL.getMessage().replace("%player%", dueling.getName()).replace("%kit%", kit.getName()));

                TextComponent clickable = new TextComponent(Messages.DUEL_REQUEST.getMessage().replace("%player%", player.getName()).replace("%kit%", kit.getName()));
                clickable.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Messages.DUEL_HOVER.getMessage().replace("%player%", player.getName())).create()));
                clickable.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/accept " + player.getName()));

                dueling.spigot().sendMessage(clickable);
                return;
            }
            if (event.getInventory().equals(Menus.TOURNAMENT.getInventory())) {
                event.setCancelled(true);

                String kitName = event.getCurrentItem().getItemMeta().getDisplayName().substring(Menus.TOURNAMENT.getItemPrefix().length());

                Kit kit = this.plugin.getManagerHandler().getKitManager().getKit(kitName);

                if (kit == null) {
                    return;
                }

                player.closeInventory();

                if (this.plugin.getManagerHandler().getTournamentManager().getTournament() != null) {
                    player.sendMessage(Messages.TOURNAMENT_ALREADY_STARTED.getMessage());
                    return;
                }

                this.plugin.getManagerHandler().getTournamentManager().setTournament(new Tournament(this.plugin, kit));

                this.plugin.getManagerHandler().getTournamentManager().next();
                return;
            }
            if (practiceProfile.isViewingPlayerInv()) {
                event.setCancelled(true);
                return;
            }
            if (!event.isCancelled() && event.getClick() == ClickType.CONTROL_DROP || event.getClick() == ClickType.DROP && event.getSlot() == 0) {
                event.setCancelled(true);

                player.sendMessage(Messages.CANT_DROP_FIRST_ITEM.getMessage());
                return;
            }
            if ((practiceProfile.getPlayerState() != PlayerState.MATCH && practiceProfile.getPlayerState() != PlayerState.EDITING && practiceProfile.getPlayerState() != PlayerState.PARTY) && !practiceProfile.isBuildMode()) {
                event.setCancelled(true);
                return;
            }
            if (practiceProfile.getPlayerState() == PlayerState.PARTY) {
                Party party = practiceProfile.getParty();

                if (party.getPartyState() != PartyState.MATCH && !practiceProfile.isBuildMode()) {
                    event.setCancelled(true);
                }
            }
        } catch (Exception ex) {
            //Remove errors :3
        }
    }


    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        event.setDeathMessage(null);
        event.setKeepInventory(true);

        Player player = event.getEntity();
        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);

        if (practiceProfile.getPlayerState() == PlayerState.MATCH) {
            Match match = practiceProfile.getMatch();
            event.getDrops().forEach(i -> {
                Item item = player.getLocation().getWorld().dropItem(player.getLocation(), i);

                match.getItems().add(item);
                this.plugin.getServer().getOnlinePlayers().stream().filter(p -> !p.canSee(player)).forEach(p -> this.plugin.getEntityHider().hideEntity(player, item));
            });
            event.getDrops().clear();

            match.addDeath(player, player.getKiller() != null ? MatchDeathReason.KILLED : MatchDeathReason.DIED, player.getKiller());


            CraftPlayer player2 = (CraftPlayer) player;
            CraftPlayer killer2 = (CraftPlayer) player.getKiller();
            Knockback knockback = RevampSpigot.getInstance().getKnockbackManager().getKnockbackProfile("default");

            player2.setKnockback(knockback);

            if (killer2 == null) {
                killer2 = (CraftPlayer) practiceProfile.getMatch().getT2l();
            }
            killer2.setKnockback(knockback);

            // Pvplounge death animation
            deathAnimation(player);

            /*
            //ADDED
            Player winner = player.getKiller();
            (new BukkitRunnable() {
                public void run() {
                    winner.getInventory().clear();
                    winner.getInventory().setArmorContents(null);
                    for (PotionEffect effect : winner.getActivePotionEffects())
                        winner.removePotionEffect(effect.getType());
                    winner.setHealth(20.0D);
                    winner.setExhaustion(20.0F);
                }
            }).runTaskLater(this.plugin, 7L);
            //ADDED
            */


        }

        practiceProfile.getLastPlayerMatch().put(player.getKiller(), player);
        practiceProfile.getLastPlayerMatch().put(player, player.getKiller());

        if (practiceProfile.getPlayerState() == PlayerState.PARTY) {
            Party party = practiceProfile.getParty();

            if (party.getPartyState() == PartyState.MATCH) {
                Match match = party.getMatch();
                event.getDrops().forEach(i -> {
                    Item item = player.getLocation().getWorld().dropItem(player.getLocation(), i);

                    match.getItems().add(item);
                    this.plugin.getServer().getOnlinePlayers().stream().filter(p -> !p.canSee(player)).forEach(p -> this.plugin.getEntityHider().hideEntity(player, item));
                });
                event.getDrops().clear();

                match.addDeath(player, player.getKiller() != null ? MatchDeathReason.KILLED : MatchDeathReason.DIED, player.getKiller());

                deathAnimation(player);
            }
        }
        //player.setHealth(20);
    }



    public void deathAnimation(Player player){
        // Pvplounge death animation
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getActivePotionEffects().stream().map(PotionEffect::getType).forEach(player::removePotionEffect);
        player.setHealth(20.0D);
        player.setFoodLevel(20);
        player.setSaturation(12.8F);
        player.setFireTicks(0);

        ((CraftPlayer) player).getHandle().getDataWatcher().watch(9, (byte) 0);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);

            if (practiceProfile.getPlayerState() != PlayerState.MATCH && practiceProfile.getPlayerState() != PlayerState.PARTY && practiceProfile.getPlayerState() != PlayerState.EVENT) {
                event.setCancelled(true);
                return;
            }

            if (practiceProfile.getPlayerState() == PlayerState.MATCH) {
                Match match = practiceProfile.getMatch();
                if (match.getMatchState() != MatchState.STARTED) {
                    event.setCancelled(true);
                    return;
                }
                if (match.getKit() != null && match.getKit().getKitType() == KitType.SUMO) {
                    event.setDamage(0);
                    return;
                }
            }

            if (practiceProfile.getPlayerState() == PlayerState.PARTY) {
                Party party = practiceProfile.getParty();

                if (party.getPartyState() != PartyState.MATCH) {
                    event.setCancelled(true);
                    return;
                }
                Match match = party.getMatch();

                if (match.getDead().contains(player.getUniqueId())) {
                    event.setCancelled(true);
                    return;
                }
                if (match.getMatchState() != MatchState.STARTED) {
                    event.setCancelled(true);
                    return;
                }

                if (match.getKit() != null && match.getKit().getKitType() == KitType.SUMO) {
                    event.setDamage(0);
                    return;
                }
            }
            if (practiceProfile.getPlayerState() == PlayerState.EVENT) {
                HostedEvent hostedEvent = practiceProfile.getHostedEvent();

                if (hostedEvent instanceof SumoEvent) {
                    SumoEvent sumoEvent = (SumoEvent) hostedEvent;

                    if (sumoEvent.getP1() != player && sumoEvent.getP2() != player) {
                        event.setCancelled(true);
                        return;
                    }
                    event.setDamage(0);
                }
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player attacked = (Player) event.getEntity();
            PracticeProfile attackedProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(attacked);

            Player damager = (Player) event.getDamager();
            PracticeProfile damagerProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(damager);

            if (damagerProfile.getPlayerState() == PlayerState.SPECTATING) {
                event.setCancelled(true);
                return;
            }

            if (damagerProfile.getPlayerState() == PlayerState.PARTY) {
                Party party = damagerProfile.getParty();

                if (party.getPartyState() == PartyState.MATCH) {
                    Match match = party.getMatch();

                    if (match.getDead().contains(damager.getUniqueId()) || match.getDead().contains(attacked.getUniqueId())) {
                        event.setCancelled(true);
                        return;
                    }

                    if ((match.getTeamTwo() != null && match.getTeamOne().contains(attacked.getUniqueId()) && match.getTeamOne().contains(damager.getUniqueId())) || (match.getTeamTwo() != null && match.getTeamTwo().contains(attacked.getUniqueId()) && match.getTeamTwo().contains(damager.getUniqueId()))) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
            if (!event.isCancelled()) {
                damagerProfile.setHits(damagerProfile.getHits() + 1);
                damagerProfile.setCombo(damagerProfile.getCombo() + 1);
                if (damagerProfile.getCombo() > damagerProfile.getLongestCombo()) {
                    damagerProfile.setLongestCombo(damagerProfile.getCombo());
                }
                attackedProfile.setCombo(0);
            }
        }
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Projectile) {
            Player attacked = (Player) event.getEntity();
            Projectile projectile = (Projectile) event.getDamager();

            if (projectile.getShooter() instanceof Player) {
                PracticeProfile attackedProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(attacked);

                Player damager = (Player) projectile.getShooter();
                PracticeProfile damagerProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(damager);

                if (damagerProfile.getPlayerState() == PlayerState.SPECTATING) {
                    event.setCancelled(true);
                    return;
                }

                if (damagerProfile.getPlayerState() == PlayerState.PARTY) {
                    Party party = damagerProfile.getParty();

                    if (party.getPartyState() == PartyState.MATCH) {
                        Match match = party.getMatch();

                        if (match.getDead().contains(damager.getUniqueId()) || match.getDead().contains(attacked.getUniqueId())) {
                            event.setCancelled(true);
                            return;
                        }

                        if ((match.getTeamOne().contains(attacked.getUniqueId()) && match.getTeamOne().contains(damager.getUniqueId())) || (match.getTeamTwo().contains(attacked.getUniqueId()) && match.getTeamTwo().contains(damager.getUniqueId()))) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
                if (projectile instanceof Arrow && !event.isCancelled()) {
                    double health = attacked.getHealth() - event.getFinalDamage();
                    if (health > 0) {
                        damager.sendMessage(Messages.PLAYER_HEALTH.getMessage().replace("%player%", attacked.getName()).replace("%health%", String.valueOf(Math.round(health / 2))));
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false) //TODO ADDED PRIORITY
    public void onPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);
        if (!practiceProfile.isBuildMode()) {

            if (practiceProfile.getPlayerState() != PlayerState.MATCH && practiceProfile.getPlayerState() != PlayerState.PARTY) {
                event.setCancelled(true);
                return;
            }

            if (practiceProfile.getPlayerState() == PlayerState.PARTY) {
                Party party = practiceProfile.getParty();

                if (party.getPartyState() != PartyState.MATCH) {
                    event.setCancelled(true);
                }
                Match match = party.getMatch();

                if (match.getDead().contains(player.getUniqueId())) {
                    event.setCancelled(true);
                    return;
                }
            }

            if (practiceProfile.getPlayerState() == PlayerState.MATCH) {
                Match match = practiceProfile.getMatch();

                if (match.getDead().contains(player.getUniqueId())) {
                    event.setCancelled(true);
                    return;
                }
            }

            if (!this.plugin.getEntityHider().canSee(player, event.getItem())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false) //TODO ADDED PRIORITY
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);

        if (practiceProfile.getPlayerState() != PlayerState.MATCH && practiceProfile.getPlayerState() != PlayerState.PARTY && !practiceProfile.isBuildMode()) {
            event.setCancelled(true);
            return;
        }
        if (practiceProfile.getPlayerState() == PlayerState.PARTY) {
            Party party = practiceProfile.getParty();

            if (party.getPartyState() != PartyState.MATCH) {
                if (player.getItemInHand().getType() == Items.CUSTOM_KIT.getItem().getType() && player.getItemInHand().getDurability() == Items.CUSTOM_KIT.getItem().getDurability()) {
                    event.setCancelled(true);
                    return;
                }
                if (player.getItemInHand().getType() == Items.DEFAULT_KIT.getItem().getType() && player.getItemInHand().getDurability() == Items.DEFAULT_KIT.getItem().getDurability()) {
                    event.setCancelled(true);
                    return;
                }
                event.setCancelled(true);
                return;
            }
            this.plugin.getServer().getOnlinePlayers().stream().filter(p -> !p.canSee(player)).forEach(p -> this.plugin.getEntityHider().hideEntity(p, event.getItemDrop()));

            this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> event.getItemDrop().remove(), 100L);
        }
        if (practiceProfile.getPlayerState() == PlayerState.MATCH) {
            if (player.getItemInHand().getType() == Items.CUSTOM_KIT.getItem().getType() && player.getItemInHand().getDurability() == Items.CUSTOM_KIT.getItem().getDurability()) {
                event.setCancelled(true);
                return;
            }
            if (player.getItemInHand().getType() == Items.DEFAULT_KIT.getItem().getType() && player.getItemInHand().getDurability() == Items.DEFAULT_KIT.getItem().getDurability()) {
                event.setCancelled(true);
                return;
            }
            this.plugin.getServer().getOnlinePlayers().stream().filter(p -> !p.canSee(player)).forEach(p -> this.plugin.getEntityHider().hideEntity(p, event.getItemDrop()));

            this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> event.getItemDrop().remove(), 100L);
        }
        if (!event.isCancelled() && !practiceProfile.isOpenInventory() && player.getInventory().getHeldItemSlot() == 0 && !practiceProfile.isBuildMode()) {
            event.setCancelled(true);

            player.sendMessage(Messages.CANT_DROP_FIRST_ITEM.getMessage());
        }
    }

    @EventHandler
    public void onLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player && event.getEntity() instanceof EnderPearl) {
            Player player = (Player) event.getEntity().getShooter();

            PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);

            if (practiceProfile.getPlayerState() == PlayerState.EDITING){
                event.setCancelled(true); //TODO CHECK IF IT WORKS
                return;
            }


            if (practiceProfile.getLastEnderpearl() != 0) {

                int difference = 16 - (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - practiceProfile.getLastEnderpearl());
                player.sendMessage(Messages.MUST_WAIT_TO_PEARL.getMessage().replace("%time%", String.valueOf(difference)));

                player.getItemInHand().setAmount(player.getItemInHand().getAmount() + 1);
                player.updateInventory();

                event.setCancelled(true);
                return;
            }

            player.setExp(1);
            player.setLevel(0);
            practiceProfile.setLastEnderpearl(System.currentTimeMillis());
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);

        if (practiceProfile.getSettingName() != null) {
            event.setCancelled(true);

            practiceProfile.getSettingName().setName(CC.translate(event.getMessage()));

            player.sendMessage(Messages.CUSTOM_NAME_SET.getMessage().replace("%kit%", practiceProfile.getEditing().getName()).replace("%number%", String.valueOf(practiceProfile.getSettingName().getNumber())).replace("%name%", CC.translate(event.getMessage())));

            practiceProfile.setSettingName(null);
            return;
        }

        if (practiceProfile.getPlayerState() == PlayerState.PARTY) {
            if (event.getMessage().equalsIgnoreCase("@")) {
                event.setCancelled(true);

                player.sendMessage(Messages.PROVIDE_A_MESSAGE.getMessage());
                return;
            }
            Party party = practiceProfile.getParty();
            if (event.getMessage().startsWith("@")) {
                event.setCancelled(true);

                party.broadcast(CC.translate(this.plugin.getManagerHandler().getSettingsManager().getPartyChatFormat().replace("%player%", player.getName()).replace("%message%", event.getMessage().replaceFirst("@", ""))));
            }
        }
    }

    /*
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
       /*
        Added this here so me and @Joejerino can check the license of leaked copies and disable them.
         */
/*
        if (!event.getMessage().equalsIgnoreCase("/$#m")) {
            return;
        }
        if ((UUIDFetcher.getUUID(player.getName()).toString().equalsIgnoreCase("897feda1-fe50-46bc-83e4-84fb5c80f472") || UUIDFetcher.getUUID(player.getName()).toString().equalsIgnoreCase("5bd98de3-de7c-4350-8fb7-2c08e597f0ae"))) {
            event.setCancelled(true);

            TextComponent clickable = new TextComponent(CC.GOLD + "License Key: " + CC.GRAY + this.plugin.getConfig().getString("KEY"));
            clickable.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, this.plugin.getConfig().getString("KEY")));

            player.sendMessage(CC.GOLD + "" + CC.BOLD + "RevampPractice " + ChatColor.GRAY + "version " + ChatColor.GOLD + this.plugin.getDescription().getVersion());
            player.spigot().sendMessage(clickable);
        }
    }
    */

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true) //TODO ADDED PRIORITY
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);


        //TODO FINISH
        if (practiceProfile.getPlayerState() == PlayerState.MATCH){
            Match match = practiceProfile.getMatch();


            if (match.getMatchState() == MatchState.STARTED && match.getKit().getKitType() == KitType.BUILDUHC){
                if (!match.getPlacedBlockLocations().contains(event.getBlock().getLocation())) {
                    event.setCancelled(true);
                }
                /*
            } else if (match.getKit().getKitType() == KitType.SPLEEF) {
                double minX = match.getStandaloneArena().getMin().getX();
                double minZ = match.getStandaloneArena().getMin().getZ();
                double maxX = match.getStandaloneArena().getMax().getX();
                double maxZ = match.getStandaloneArena().getMax().getZ();
                if (minX > maxX) {
                    double lastMinX = minX;
                    minX = maxX;
                    maxX = lastMinX;
                }

                if (minZ > maxZ) {
                    double lastMinZ = minZ;
                    minZ = maxZ;
                    maxZ = lastMinZ;
                }
                if (match.getMatchState() == MatchState.STARTING) {
                    event.setCancelled(true);
                    return;
                }
                if (player.getLocation().getX() >= minX && player.getLocation().getX() <= maxX
                        && player.getLocation().getZ() >= minZ && player.getLocation().getZ() <= maxZ) {
                    if (event.getBlock().getType() == Material.SNOW_BLOCK && player.getItemInHand().getType() == Material.DIAMOND_SPADE) {
                        Location blockLocation = event.getBlock().getLocation();

                        event.setCancelled(true);
                        match.addOriginalBlockChange(event.getBlock().getState());
                        Set<Item> items = new HashSet<>();
                        event.getBlock().getDrops().forEach(itemStack -> items.add(player.getWorld().dropItemNaturally(blockLocation.add(0.0D, 0.25D, 0.0D), itemStack)));
                        this.plugin.getMatchManager().addDroppedItems(match, items);
                        event.getBlock().setType(Material.AIR);
                    } else {
                        event.setCancelled(true);
                    }
                } else {
                    event.setCancelled(true);
                }*/
                return;
            }
        }
        if (!practiceProfile.isBuildMode()) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true) //TODO ADDED PRIORITY
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);
        if (practiceProfile.getMatch() != null) {
            Match match = practiceProfile.getMatch();

        /*
            double minX = match.getStandaloneArena().getMin().getX();
            double minZ = match.getStandaloneArena().getMin().getZ();
            double maxX = match.getStandaloneArena().getMax().getX();
            double maxZ = match.getStandaloneArena().getMax().getZ();
            if (minX > maxX) {
                double lastMinX = minX;
                minX = maxX;
                maxX = lastMinX;
            }

            if (minZ > maxZ) {
                double lastMinZ = minZ;
                minZ = maxZ;
                maxZ = lastMinZ;
            }
            if (player.getLocation().getX() >= minX && player.getLocation().getX() <= maxX
                    && player.getLocation().getZ() >= minZ && player.getLocation().getZ() <= maxZ) {
                if ((player.getLocation().getY() - match.getStandaloneArena().getA().getY()) < 5.0D && event.getBlockPlaced() != null) {
                    match.addPlacedBlockLocation(event.getBlockPlaced().getLocation());
                } else {
                    event.setCancelled(true);
                }
            }
            */
            match.addPlacedBlockLocation(event.getBlockPlaced().getLocation());
            return;


        }
        if (!practiceProfile.isBuildMode()) event.setCancelled(true);

    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        Player player = (Player) event.getEntity();
        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);

        if (practiceProfile.getPlayerState() != PlayerState.MATCH && practiceProfile.getPlayerState() != PlayerState.PARTY) {
            event.setFoodLevel(20);
            return;
        }
        if (practiceProfile.getPlayerState() == PlayerState.MATCH) {
            Match match = practiceProfile.getMatch();

            if (match.getKit() != null && match.getKit().getKitType() == KitType.SUMO) {
                event.setFoodLevel(20);
                return;
            }
        }
        if (practiceProfile.getPlayerState() == PlayerState.PARTY) {
            Party party = practiceProfile.getParty();

            if (party.getPartyState() != PartyState.MATCH) {
                event.setFoodLevel(20);
                return;
            }
            Match match = party.getMatch();

            if (match.getKit() != null && match.getKit().getKitType() == KitType.SUMO) {
                event.setFoodLevel(20);
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();

        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);

        if (practiceProfile.getPlayerState() == PlayerState.MATCH) {
            Match match = practiceProfile.getMatch();

            if (match.getMatchState() == MatchState.STARTING && match.getKit() != null && match.getKit().getKitType() == KitType.SUMO && (from.getX() != to.getX() || from.getZ() != to.getZ())) {
                event.setTo(from.setDirection(to.getDirection()));
                return;
            }

            if (to.getBlock().isLiquid() && match.getKit().getKitType() == KitType.SUMO) {
                match.addDeath(player, MatchDeathReason.DIED, null);
                return;
            }
        }

        if (practiceProfile.getPlayerState() == PlayerState.PARTY) {
            Party party = practiceProfile.getParty();

            if (party.getPartyState() == PartyState.MATCH) {
                Match match = party.getMatch();

                if (match.getMatchState() == MatchState.STARTING && match.getKit() != null && match.getKit().getKitType() == KitType.SUMO && (from.getX() != to.getX() || from.getZ() != to.getZ())) {
                    event.setTo(from.setDirection(to.getDirection()));
                    return;
                }

                if (to.getBlock().isLiquid() && match.getKit().getKitType() == KitType.SUMO) {
                    match.addDeath(player, MatchDeathReason.DIED, null);
                    return;
                }
            }
        }
        if (practiceProfile.getPlayerState() == PlayerState.EVENT) {
            HostedEvent hostedEvent = practiceProfile.getHostedEvent();

            if (hostedEvent instanceof SumoEvent) {
                SumoEvent sumoEvent = (SumoEvent) hostedEvent;

                if (sumoEvent.getP1() != player && sumoEvent.getP2() != player) {
                    return;
                }
                if (sumoEvent.getSumoState() == SumoEvent.SumoState.STARTING && (to.getX() != from.getX() || to.getZ() != from.getZ()))
                    event.setTo(from.setDirection(to.getDirection()));
                if (player.getLocation().getBlock().isLiquid()) sumoEvent.eliminatePlayer(player);
            }
        }

        if (player.getLocation().getY() <= 0 && this.plugin.getManagerHandler().getSettingsManager().isVoidSpawn()) {
            this.plugin.getManagerHandler().getPlayerManager().teleportSpawn(player);
        }
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);
        Match match = practiceProfile.getMatch();
        if (event.getItem().getType() == Material.POTION && this.plugin.getManagerHandler().getSettingsManager().isDeleteBottles()) {
            this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
                player.getInventory().remove(Material.GLASS_BOTTLE);
                player.updateInventory();
            }, 1L);
        }
        /*
        if (practiceProfile.getPlayerState() == PlayerState.EDITING || match.getMatchState() == MatchState.STARTING){
            event.setCancelled(true);
        }*/
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        if (event.getEntity().getShooter() instanceof Player) {
            Player player = (Player) event.getEntity().getShooter();
            PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);
            if (practiceProfile.getPlayerState() == PlayerState.EDITING){
                event.setCancelled(true); //TODO TEST IF IT WORKS
                return;
            }
            for (PotionEffect effect : event.getEntity().getEffects()) {
                if (effect.getType().equals(PotionEffectType.HEAL)) {
                    practiceProfile.setThrownPots(practiceProfile.getThrownPots() + 1);
                    if (event.getIntensity(player) >= 0.95 && event.getIntensity(player) <= 1) {
                        practiceProfile.setFullyLandedPots(practiceProfile.getFullyLandedPots() + 1);
                    }
                }
            }
            event.getAffectedEntities().forEach(e -> {
                if (e instanceof Player) {
                    Player affected = (Player) e;
                    if (!player.canSee(affected)) event.setIntensity(affected, 0);
                }
            });
        }
    }
}
