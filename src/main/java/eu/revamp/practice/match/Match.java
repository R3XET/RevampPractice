package eu.revamp.practice.match;

import eu.revamp.practice.RevampPractice;
import eu.revamp.practice.arena.Arena;
import eu.revamp.practice.kit.CustomKit;
import eu.revamp.practice.kit.Kit;
import eu.revamp.practice.kit.KitType;
import eu.revamp.practice.party.Party;
import eu.revamp.practice.party.PartyState;
import eu.revamp.practice.task.player.BardTask;
import eu.revamp.practice.util.EloUtil;
import eu.revamp.practice.util.enums.Items;
import eu.revamp.practice.util.enums.Messages;
import eu.revamp.practice.util.misc.InventorySnapshot;
import eu.revamp.practice.util.misc.ScoreHelper;
import eu.revamp.practice.util.reflection.BukkitReflection;
import eu.revamp.spigot.RevampSpigot;
import eu.revamp.spigot.knockback.Knockback;
import eu.revamp.spigot.utils.chat.color.CC;
import eu.revamp.spigot.utils.item.ItemBuilder;
import io.netty.util.internal.ConcurrentSet;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import eu.revamp.practice.player.PlayerState;
import eu.revamp.practice.player.PracticeProfile;
import eu.revamp.practice.tournament.Tournament;
import eu.revamp.practice.tournament.TournamentState;
import lombok.Setter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

@SuppressWarnings("Duplicates")
@Getter @Setter
@RequiredArgsConstructor
public class Match {

    private final RevampPractice plugin;
    private final Kit kit;
    private final List<UUID> teamOne;
    private final List<UUID> teamTwo;
    private final boolean ranked;
    private final boolean tournament;

    private Player t1l;
    private Player t2l;

    private PracticeProfile t1lp;
    private PracticeProfile t2lp;

    private List<UUID> spectators = new ArrayList<>();
    private List<UUID> dead = new ArrayList<>();
    private List<Item> items = new ArrayList<>();
    private List<UUID> exempt = new ArrayList<>();

    private Arena arena;
    private long startTime = 0;
    private long endTime = 0;

    private MatchState matchState = MatchState.STARTING;

    private boolean party;

    private final Set<BlockState> originalBlockChanges = new ConcurrentSet<>();
    private final Set<Location> placedBlockLocations = new ConcurrentSet<>();

    public void addOriginalBlockChange(BlockState blockState) {
        this.originalBlockChanges.add(blockState);
    }

    public void removeOriginalBlockChange(BlockState blockState) {
        this.originalBlockChanges.remove(blockState);
    }

    public void addPlacedBlockLocation(Location location) {
        this.placedBlockLocations.add(location);
    }

    public void removePlacedBlockLocation(Location location) {
        this.placedBlockLocations.remove(location);
    }

    public void start() {

        t1l = this.plugin.getServer().getPlayer(teamOne.get(0));
        if (teamTwo != null) t2l = this.plugin.getServer().getPlayer(teamTwo.get(0));

        t1lp = this.plugin.getManagerHandler().getProfileManager().getProfile(t1l);
        if (teamTwo != null) t2lp = this.plugin.getManagerHandler().getProfileManager().getProfile(t2l);

        party = t1lp.getPlayerState() == PlayerState.PARTY || teamTwo != null && t2lp.getPlayerState() == PlayerState.PARTY;

        arena = (kit != null ? this.plugin.getManagerHandler().getArenaManager().randomArena(kit) : this.plugin.getManagerHandler().getArenaManager().randomArena(KitType.HCF));

        arena.getL1().getChunk().load();
        teamOne.forEach(u -> {
            Player player = this.plugin.getServer().getPlayer(u);
            PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);

            if (teamTwo != null) {
                player.sendMessage(ranked ? Messages.RANKED_MATCH_FOUND.getMessage().replace("%player%", t2l.getName() + (party ? "'s Team" : "")).replace("%elo%", String.valueOf(t2lp.getElo(kit))).replace("%kit%", kit != null ? kit.getName() : "HCF") : Messages.UNRANKED_MATCH_FOUND.getMessage().replace("%player%", t2l.getName() + (party ? "'s Team" : "")).replace("%kit%", kit != null ? kit.getName() : "HCF"));
                hide(player);
            } else {
                player.sendMessage(Messages.PARTY_FFA_STARTING.getMessage());
            }

            this.plugin.getManagerHandler().getPlayerManager().resetPlayer(player);
            this.plugin.getManagerHandler().getPlayerManager().hideAll(player);

            if (kit != null) {
                boolean hasKits = false;
                for (int i = 1; i <= 7; i++) {
                    if (practiceProfile.getCustomKit(kit, i) != null) {
                        hasKits = true;

                        CustomKit customKit = practiceProfile.getCustomKit(kit, i);

                        ItemStack clone = Items.CUSTOM_KIT.getItem().clone();
                        String name = customKit.getName().replace("%kit%", kit.getName()).replace("%number%", String.valueOf(i));
                        player.getInventory().setItem(i - 1, new ItemBuilder(clone.getType()).setDurability(clone.getDurability()).setName(name).toItemStack());
                    }
                }
                if (!hasKits) {
                    player.getInventory().setContents(kit.getInventory().getContents());
                    player.getInventory().setArmorContents(kit.getArmor());
                } else {
                    player.getInventory().setItem(8, Items.DEFAULT_KIT.getItem());
                }

                CraftPlayer player2 = (CraftPlayer) player;
                Knockback knockback;
                if ((RevampSpigot.getInstance().getKnockbackManager().getKnockbackProfile(kit.getName()) != null)) {
                    knockback = RevampSpigot.getInstance().getKnockbackManager().getKnockbackProfile(kit.getName());
                }
                else {
                    knockback = RevampSpigot.getInstance().getKnockbackManager().getKnockbackProfile("default");
                }
                player2.setKnockback(knockback);

            } else {
                player.getInventory().setContents(practiceProfile.getHcfKit().getInventory().getContents());
                player.getInventory().setArmorContents(practiceProfile.getHcfKit().getArmor());

                switch (practiceProfile.getHcfKit()) {
                    case BARD: {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 1));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 1));

                        new BardTask(this.plugin, player).runTaskTimer(this.plugin, 0L, 0L);
                        break;
                    }
                    case ARCHER: {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
                        break;
                    }
                    case ROGUE: {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 2));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 2));
                        break;
                    }
                }
            }
            player.updateInventory();

            player.teleport(arena.getL1());

            if (kit != null) {
                player.setMaximumNoDamageTicks(kit.getDamageTicks());
                if (kit.getName().equalsIgnoreCase("Combo")) {
                    player.setMaximumNoDamageTicks(kit.getDamageTicks());
                }
            }

            ScoreHelper scoreHelper = ScoreHelper.getByPlayer(player);

            teamOne.forEach(u1 -> {
                Player player1 = this.plugin.getServer().getPlayer(u1);

                player.showPlayer(player1);
                if (teamTwo != null) {
                    scoreHelper.addFriendly(player1);
                } else {
                    scoreHelper.addEnemy(player1);

                    player.showPlayer(player1);
                }
            });

            if (teamTwo != null) teamTwo.forEach(u1 -> {
                Player player1 = this.plugin.getServer().getPlayer(u1);

                player.showPlayer(player1);
                scoreHelper.addEnemy(player1);
            });
        });

        if (teamTwo != null) {

            arena.getL2().getChunk().load();
            teamTwo.forEach(u -> {
                Player player = this.plugin.getServer().getPlayer(u);
                PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);

                player.sendMessage(ranked ? Messages.RANKED_MATCH_FOUND.getMessage().replace("%player%", t1l.getName() + (party ? "'s Team" : "")).replace("%elo%", String.valueOf(t1lp.getElo(kit))).replace("%kit%", kit != null ? kit.getName() : "HCF") : Messages.UNRANKED_MATCH_FOUND.getMessage().replace("%player%", t1l.getName() + (party ? "'s Team" : "")).replace("%kit%", kit != null ? kit.getName() : "HCF"));
                hide(player);

                this.plugin.getManagerHandler().getPlayerManager().resetPlayer(player);
                this.plugin.getManagerHandler().getPlayerManager().hideAll(player);

                if (kit != null) {
                    boolean hasKits = false;
                    for (int i = 1; i <= 7; i++) {
                        if (practiceProfile.getCustomKit(kit, i) != null) {
                            hasKits = true;

                            CustomKit customKit = practiceProfile.getCustomKit(kit, i);

                            ItemStack clone = Items.CUSTOM_KIT.getItem().clone();
                            String name = customKit.getName().replace("%kit%", kit.getName()).replace("%number%", String.valueOf(i));
                            player.getInventory().setItem(i - 1, new ItemBuilder(clone.getType()).setDurability(clone.getDurability()).setName(name).toItemStack());
                        }
                    }
                    if (!hasKits) {
                        player.getInventory().setContents(kit.getInventory().getContents());
                        player.getInventory().setArmorContents(kit.getArmor());
                    } else {
                        player.getInventory().setItem(8, Items.DEFAULT_KIT.getItem());
                    }

                    CraftPlayer player2 = (CraftPlayer) player;
                    Knockback knockback;
                    if ((RevampSpigot.getInstance().getKnockbackManager().getKnockbackProfile(kit.getName()) != null)) {
                        knockback = RevampSpigot.getInstance().getKnockbackManager().getKnockbackProfile(kit.getName());
                    }
                    else {
                        knockback = RevampSpigot.getInstance().getKnockbackManager().getKnockbackProfile("default");
                    }
                    player2.setKnockback(knockback);

                } else {
                    player.getInventory().setContents(practiceProfile.getHcfKit().getInventory().getContents());
                    player.getInventory().setArmorContents(practiceProfile.getHcfKit().getArmor());

                    switch (practiceProfile.getHcfKit()) {
                        case BARD: {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
                            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 1));
                            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 1));

                            new BardTask(this.plugin, player).runTaskTimer(this.plugin, 0L, 0L);
                            break;
                        }
                        case ARCHER: {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
                            break;
                        }
                        case ROGUE: {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
                            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 2));
                            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 2));
                            break;
                        }
                    }
                }
                player.updateInventory();

                player.teleport(arena.getL2());

                if (kit != null) {
                    player.setMaximumNoDamageTicks(kit.getDamageTicks());
                    if (kit.getName().equalsIgnoreCase("Combo")) {
                        player.setMaximumNoDamageTicks(kit.getDamageTicks());
                    }
                }

                ScoreHelper scoreHelper = ScoreHelper.getByPlayer(player);

                teamTwo.forEach(u1 -> {
                    Player player1 = this.plugin.getServer().getPlayer(u1);

                    player.showPlayer(player1);
                    scoreHelper.addFriendly(player1);
                });

                teamOne.forEach(u1 -> {
                    Player player1 = this.plugin.getServer().getPlayer(u1);

                    player.showPlayer(player1);
                    scoreHelper.addEnemy(player1);
                });
            });
        }
        new BukkitRunnable() {
            int i = 5;

            public void run() {
                if (matchState != MatchState.STARTING) {
                    this.cancel();
                    return;
                }
                if (i > 0) {
                    broadcast(Messages.MATCH_STARTING.getMessage().replace("%time%", String.valueOf(i)));
                    playSound(Sound.NOTE_STICKS);
                }
                i--;
                if (i == -1) {
                    this.cancel();

                    broadcast(Messages.MATCH_STARTED.getMessage());
                    playSound(Sound.NOTE_PLING);

                    matchState = MatchState.STARTED;
                    startTime = System.currentTimeMillis();
                }
            }
        }.runTaskTimerAsynchronously(this.plugin, 20L, 20L);
    }

    public void addDeath(Player player, MatchDeathReason matchDeathReason, Player killer) {
        if (dead.contains(player.getUniqueId()) || matchState == MatchState.ENDING) return;
        dead.add(player.getUniqueId());

        player.setGameMode(GameMode.SPECTATOR);
        player.setAllowFlight(true);
        player.setFlying(true);

        List<UUID> allPlayers = new ArrayList<>(teamOne);
        if (teamTwo != null) allPlayers.addAll(teamTwo);
        allPlayers.addAll(spectators);

        if (this.plugin.getManagerHandler().getSettingsManager().isLightningEffect())
            BukkitReflection.sendLightning(player, player.getLocation());

        allPlayers.stream().filter(u -> !exempt.contains(u)).forEach(u -> {
            Player players = this.plugin.getServer().getPlayer(u);

            players.hidePlayer(player);

            if (this.plugin.getManagerHandler().getSettingsManager().isLightningEffect())
                BukkitReflection.sendLightning(players, player.getLocation());
        });

        new InventorySnapshot(player);

        this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
            this.plugin.getManagerHandler().getPlayerManager().resetPlayer(player);
        }, 5L);

        switch (matchDeathReason) {
            case DISCONNECTED: {
                broadcast(Messages.PLAYER_DEATH_DISCONNECTED.getMessage().replace("%player%", player.getName()));
                exempt.add(player.getUniqueId());
                break;
            }
            case KILLED: {
                broadcast(Messages.PLAYER_DEATH_KILLED.getMessage().replace("%player%", player.getName()).replace("%killer%", killer.getName()));
                break;
            }
            case DIED: {
                broadcast(Messages.PLAYER_DEATH_DIED.getMessage().replace("%player%", player.getName()));
                break;
            }
            case LEFT: {
                broadcast(Messages.PLAYER_DEATH_LEFT.getMessage().replace("%player%", player.getName()));
                exempt.add(player.getUniqueId());
                break;
            }
        }
        if (getAlive(true) == (teamTwo == null ? 1 : 0)) end(teamTwo == null);
        if (teamTwo != null && getAlive(false) == 0) end(true);
    }

    public void end(boolean t1) {

        if (matchState == MatchState.ENDING) return;


        matchState = MatchState.ENDING;

        endTime = System.currentTimeMillis();


        //CLEAR BUILD UHC
        this.getPlacedBlockLocations().forEach(location -> location.getBlock().setType(Material.AIR));
        this.getOriginalBlockChanges().forEach((blockState) -> blockState.getLocation().getBlock().setType(blockState.getType()));

        new MatchResetRunnable(this).runTaskTimer(this.plugin, 20L, 20L);


        String[] parts = Messages.MATCH_END.getMessage().split("\n");

        TextComponent winnerNames = new TextComponent("");
        (t1 ? teamOne : teamTwo).forEach(u -> {
            if (teamTwo != null || !dead.contains(u)) {
                OfflinePlayer offlinePlayer = this.plugin.getServer().getOfflinePlayer(u);
                TextComponent winnerName = new TextComponent(CC.GREEN + offlinePlayer.getName());
                winnerName.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Messages.INVENTORY_HOVER.getMessage().replace("%player%", offlinePlayer.getName())).create()));
                winnerName.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/inventory " + offlinePlayer.getName()));
                winnerNames.addExtra(winnerName);
                winnerNames.addExtra(" ");
            }
        });
        TextComponent loserNames = new TextComponent("");
        (t1 ? teamTwo != null ? teamTwo : dead : teamOne).forEach(u -> {
            OfflinePlayer offlinePlayer = this.plugin.getServer().getOfflinePlayer(u);
            TextComponent loserName = new TextComponent(CC.RED + offlinePlayer.getName());
            loserName.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Messages.INVENTORY_HOVER.getMessage().replace("%player%", offlinePlayer.getName())).create()));
            loserName.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/inventory " + offlinePlayer.getName()));
            loserNames.addExtra(loserName);
            loserNames.addExtra(" ");
        });

        /*
        TextComponent gg = new TextComponent("");
        gg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Messages.GG_HOVER.getMessage()).create()));
        loserNames.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/msg " + offlinePlayer.getName() + " gg"));
        */

        Map<Integer, TextComponent> excluded = new HashMap<>();
        for (String part : parts) {
            if (CC.strip(part).contains("%winnerNames%")) {
                String[] breakdown = part.split("%winnerNames%");

                TextComponent textComponent;
                if (!CC.strip(part).equalsIgnoreCase("%winnerNames%")) {
                    textComponent = new TextComponent(CC.translate(breakdown[0]));
                    textComponent.addExtra(winnerNames);

                    if (breakdown.length >= 1) {
                        for (int i = 1; i < breakdown.length; i++) {
                            if (i > 1) {
                                textComponent.addExtra(winnerNames);
                            }
                            textComponent.addExtra(breakdown[i]);
                        }
                    }
                } else {
                    textComponent = winnerNames;
                }
                excluded.put(Arrays.asList(parts).indexOf(part), textComponent);
            }
        }
        for (String part : parts) {
            if (CC.strip(part).contains("%loserNames%")) {
                String[] breakdown = part.split("%loserNames%");

                TextComponent textComponent;
                if (!CC.strip(part).equalsIgnoreCase("%loserNames%")) {
                    textComponent = new TextComponent(CC.translate(breakdown[0]));
                    textComponent.addExtra(loserNames);

                    if (breakdown.length >= 1) {
                        for (int i = 1; i < breakdown.length; i++) {
                            if (i > 1) {
                                textComponent.addExtra(loserNames);
                            }
                            textComponent.addExtra(breakdown[i]);
                        }
                    }
                } else {
                    textComponent = loserNames;
                }
                excluded.put(Arrays.asList(parts).indexOf(part), textComponent);
            }
        }
        for (int i = 0; i < parts.length; i++) {
            if (excluded.containsKey(i)) {
                broadcast(excluded.get(i));
            } else {
                broadcast(parts[i]);
            }
        }
        if (ranked) {
            int[] newElo = EloUtil.getNewRankings(t1lp.getElo(kit), t2lp.getElo(kit), t1);
            int eloDifference = Math.abs(newElo[0] - t1lp.getElo(kit));

            t1lp.setElo(kit, newElo[0] <= this.plugin.getManagerHandler().getSettingsManager().getMinElo() ? this.plugin.getManagerHandler().getSettingsManager().getMinElo() : (newElo[0] >= this.plugin.getManagerHandler().getSettingsManager().getMaxElo() ? this.plugin.getManagerHandler().getSettingsManager().getMaxElo() : newElo[0]));
            t2lp.setElo(kit, newElo[1] <= this.plugin.getManagerHandler().getSettingsManager().getMinElo() ? this.plugin.getManagerHandler().getSettingsManager().getMinElo() : (newElo[1] >= this.plugin.getManagerHandler().getSettingsManager().getMaxElo() ? this.plugin.getManagerHandler().getSettingsManager().getMaxElo() : newElo[1]));

            Player winner = t1 ? t1l : t2l;
            PracticeProfile wp = this.plugin.getManagerHandler().getProfileManager().getProfile(winner);

            Player loser = t1 ? t2l : t1l;
            PracticeProfile lp = this.plugin.getManagerHandler().getProfileManager().getProfile(loser);

            String eloChanges = Messages.ELO_CHANGES.getMessage().replace("%winner%", winner.getName()).replace("%loser%", loser.getName()).replace("%newWinnerElo%", String.valueOf(wp.getElo(kit))).replace("%newLoserElo%", String.valueOf(lp.getElo(kit))).replace("%eloDifference%", String.valueOf(eloDifference));

            broadcast(eloChanges);
        }

        List<UUID> allPlayers = new ArrayList<>();

        teamOne.stream().filter(u -> !exempt.contains(u)).forEach(allPlayers::add);
        if (teamTwo != null) teamTwo.stream().filter(u -> !exempt.contains(u)).forEach(allPlayers::add);
        spectators.stream().filter(u -> !exempt.contains(u)).forEach(allPlayers::add);

        for (UUID uuid : allPlayers) {
            Player player = this.plugin.getServer().getPlayer(uuid);

            new InventorySnapshot(player);
        }

        this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {

            items.forEach(Item::remove);

            allPlayers.forEach(u -> {
                Player player = this.plugin.getServer().getPlayer(u);

                if (kit != null) teamOne.forEach(u1 -> kit.getUnrankedMatch().remove(u1));
                if (kit != null) teamOne.forEach(u1 -> kit.getRankedMatch().remove(u1));

                if (teamTwo != null && kit != null) teamTwo.forEach(u1 -> kit.getUnrankedMatch().remove(u1));
                if (teamTwo != null && kit != null) teamTwo.forEach(u1 -> kit.getRankedMatch().remove(u1));

                if (player != null) {
                    PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);

                    practiceProfile.setHits(0);
                    practiceProfile.setCombo(0);
                    practiceProfile.setLongestCombo(0);
                    practiceProfile.setThrownPots(0);
                    practiceProfile.setFullyLandedPots(0);

                    practiceProfile.setPlayerState(practiceProfile.getParty() != null ? PlayerState.PARTY : PlayerState.LOBBY);
                    practiceProfile.setPlayerState(practiceProfile.getParty() != null ? PlayerState.PARTY : PlayerState.LOBBY);
                    this.plugin.getManagerHandler().getPlayerManager().giveItems(player, true);
                    this.plugin.getManagerHandler().getPlayerManager().teleportSpawn(player);

                    ScoreHelper scoreHelper = ScoreHelper.getByPlayer(player);
                    scoreHelper.resetEnemy();
                    scoreHelper.resetFriendly();

                    if (party) {
                        Party p1 = t1lp.getParty();

                        if (p1 != null) {
                            p1.setPartyState(PartyState.LOBBY);
                            p1.setMatch(null);
                        }

                        if (teamTwo != null) {
                            Party p2 = t2lp.getParty();

                            if (p2 != null) {
                                p2.setPartyState(PartyState.LOBBY);
                                p2.setMatch(null);
                            }
                        }
                    }
                }
            });

            if (tournament) {
                Tournament tournament = this.plugin.getManagerHandler().getTournamentManager().getTournament();
                tournament.getMatchList().remove(this);

                tournament.broadcast(Messages.PLAYER_ELIMINATED_TOURNAMENT.getMessage().replace("%player%", (t1 ? t2l : t1l).getName()));
                tournament.getParticipants().remove(t1 ? t2l.getUniqueId() : t1l.getUniqueId());

                (t1 ? t1lp : t2lp).setPlayerState(PlayerState.TOURNAMENT);
                this.plugin.getManagerHandler().getPlayerManager().giveItems(t1 ? t1l : t2l, true);

                if (tournament.getParticipants().size() == 1) {
                    Player winner = this.plugin.getServer().getPlayer(tournament.getParticipants().get(0));

                    tournament.end(winner.getName());
                } else if (tournament.getParticipants().size() == 0) {
                    tournament.end(null);
                } else if (tournament.getMatchList().size() == 0) {

                    tournament.setStartTime(0);
                    tournament.setRound(tournament.getRound() + 1);

                    tournament.setTournamentState(TournamentState.WAITING);
                    this.plugin.getServer().broadcastMessage(Messages.TOURNAMENT_NEXT_ROUND.getMessage().replace("%round%", String.valueOf(tournament.getRound())));
                    this.plugin.getManagerHandler().getTournamentManager().next();
                }
            }
        }, 100L);
    }

    public int getAlive(boolean t1) {
        int alive = 0;
        for (UUID uuid : t1 ? teamOne : teamTwo) {
            if (!dead.contains(uuid)) alive += 1;
        }
        return alive;
    }

    public void broadcast(String message) {
        List<UUID> allPlayers = new ArrayList<>();
        teamOne.stream().filter(u -> !exempt.contains(u)).forEach(allPlayers::add);
        if (teamTwo != null) teamTwo.stream().filter(u -> !exempt.contains(u)).forEach(allPlayers::add);
        spectators.stream().filter(u -> !exempt.contains(u)).forEach(allPlayers::add);

        allPlayers.stream().filter(u -> this.plugin.getServer().getPlayer(u) != null).forEach(u -> this.plugin.getServer().getPlayer(u).sendMessage(message));
    }

    public void broadcast(TextComponent message) {
        List<UUID> allPlayers = new ArrayList<>();
        teamOne.stream().filter(u -> !exempt.contains(u)).forEach(allPlayers::add);
        if (teamTwo != null) teamTwo.stream().filter(u -> !exempt.contains(u)).forEach(allPlayers::add);
        spectators.stream().filter(u -> !exempt.contains(u)).forEach(allPlayers::add);

        allPlayers.stream().filter(u -> this.plugin.getServer().getPlayer(u) != null).forEach(u -> this.plugin.getServer().getPlayer(u).spigot().sendMessage(message));
    }

    public void playSound(Sound sound) {
        List<UUID> allPlayers = new ArrayList<>();
        teamOne.stream().filter(u -> !exempt.contains(u)).forEach(allPlayers::add);
        if (teamTwo != null) teamTwo.stream().filter(u -> !exempt.contains(u)).forEach(allPlayers::add);
        spectators.stream().filter(u -> !exempt.contains(u)).forEach(allPlayers::add);

        allPlayers.stream().filter(u -> this.plugin.getServer().getPlayer(u) != null).forEach(u -> this.plugin.getServer().getPlayer(u).playSound(this.plugin.getServer().getPlayer(u).getLocation(), sound, 20f, 20f));
    }

    private void hide(Player player) {
        this.plugin.getServer().getOnlinePlayers().stream().filter(p -> !teamOne.contains(p.getUniqueId()) && !teamTwo.contains(p.getUniqueId()) && !spectators.contains(p.getUniqueId())).forEach(player::hidePlayer);
    }
}
