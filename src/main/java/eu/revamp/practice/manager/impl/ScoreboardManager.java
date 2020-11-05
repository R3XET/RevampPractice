package eu.revamp.practice.manager.impl;

import eu.revamp.practice.RevampPractice;
import eu.revamp.practice.hostedevent.HostedEvent;
import eu.revamp.practice.hostedevent.impl.SumoEvent;
import eu.revamp.practice.kit.Kit;
import eu.revamp.practice.kit.hcf.HCFKit;
import eu.revamp.practice.manager.Manager;
import eu.revamp.practice.manager.ManagerHandler;
import eu.revamp.practice.match.Match;
import eu.revamp.practice.party.Party;
import eu.revamp.practice.party.PartyState;
import eu.revamp.practice.player.PracticeProfile;
import eu.revamp.practice.queue.Queue;
import eu.revamp.practice.tournament.Tournament;
import eu.revamp.practice.util.DurationUtil;
import eu.revamp.practice.util.misc.ScoreHelper;
import eu.revamp.practice.util.reflection.BukkitReflection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("Duplicates")
public class ScoreboardManager extends Manager {

    public ScoreboardManager(ManagerHandler managerHandler) {
        super(managerHandler);
    }

    public void update(Player player) {
        try {
            if (!managerHandler.getProfileManager().hasProfile(player) || !ScoreHelper.hasScore(player) || !this.managerHandler.getPlugin().getConfig().getBoolean("scoreboard.enabled")) {
                return;
            }
            PracticeProfile practiceProfile = this.managerHandler.getProfileManager().getProfile(player);
            ScoreHelper scoreHelper = ScoreHelper.getByPlayer(player);

            String title = "Title";
            List<String> toReturn = new ArrayList<>();

            switch (practiceProfile.getPlayerState()) {
                case LOBBY: {
                    title = managerHandler.getPlugin().getConfig().getString("scoreboard.LOBBY.title");
                    for (String line : managerHandler.getPlugin().getConfig().getStringList("scoreboard.LOBBY.slots")) {
                        line = line.replace("%online%", String.valueOf(this.managerHandler.getPlugin().getServer().getOnlinePlayers().size()));
                        line = line.replace("%queueing%", String.valueOf(getQueueing()));
                        line = line.replace("%fighting%", String.valueOf(getFighting()));
                        toReturn.add(line);
                    }
                    break;
                }
                case QUEUE: {
                    Queue queue = practiceProfile.getQueue();
                    title = managerHandler.getPlugin().getConfig().getString("scoreboard." + (queue.isRanked() ? "RANKED" : "UNRANKED") + "-QUEUE.title");
                    for (String line : managerHandler.getPlugin().getConfig().getStringList("scoreboard." + (queue.isRanked() ? "RANKED" : "UNRANKED") + "-QUEUE.slots")) {
                        line = line.replace("%online%", String.valueOf(this.managerHandler.getPlugin().getServer().getOnlinePlayers().size()));
                        line = line.replace("%queueing%", String.valueOf(getQueueing()));
                        line = line.replace("%fighting%", String.valueOf(getFighting()));

                        line = line.replace("%kit%", queue.getKit().getName());
                        line = line.replace("%time%", DurationUtil.getDuration(queue.getStartTime()));

                        line = line.replace("%minElo%", String.valueOf(queue.getMinElo()));
                        line = line.replace("%maxElo%", String.valueOf(queue.getMaxElo()));
                        toReturn.add(line);
                    }
                    break;
                }
                case MATCH: {
                    Match match = practiceProfile.getMatch();
                    Player opponent = match.getTeamOne().contains(player.getUniqueId()) ? match.getT2l() : match.getT1l();
                    PracticeProfile playerProfile = this.managerHandler.getProfileManager().getProfile(player);
                    PracticeProfile opponentProfile = this.managerHandler.getProfileManager().getProfile(player);
                    title = managerHandler.getPlugin().getConfig().getString("scoreboard." + (match.isRanked() ? "RANKED" : "UNRANKED") + "-MATCH.title");
                    for (String line : managerHandler.getPlugin().getConfig().getStringList("scoreboard." + (match.isRanked() ? "RANKED" : "UNRANKED") + "-MATCH.slots")) {
                        line = line.replace("%online%", String.valueOf(this.managerHandler.getPlugin().getServer().getOnlinePlayers().size()));
                        line = line.replace("%queueing%", String.valueOf(getQueueing()));
                        line = line.replace("%fighting%", String.valueOf(getFighting()));

                        line = line.replace("%elo%", String.valueOf(playerProfile.getElo(match.getKit())));
                        line = line.replace("%opponentElo%", String.valueOf(opponentProfile.getElo(match.getKit())));


                        line = line.replace("%kit%", match.getKit().getName());
                        line = line.replace("%opponent%", opponent.getName());
                        line = line.replace("%ping%", String.valueOf(BukkitReflection.getPing(player)));
                        line = line.replace("%opponentPing%", String.valueOf(BukkitReflection.getPing(opponent)));
                        line = line.replace("%time%", (match.getEndTime() != 0 ? DurationUtil.getDuration(match.getStartTime(), match.getEndTime()) : DurationUtil.getDuration(match.getStartTime())));
                        toReturn.add(line);
                    }
                    break;
                }
                case EDITING: {
                    title = managerHandler.getPlugin().getConfig().getString("scoreboard.EDITING.title");
                    for (String line : managerHandler.getPlugin().getConfig().getStringList("scoreboard.EDITING.slots")) {
                        line = line.replace("%online%", String.valueOf(this.managerHandler.getPlugin().getServer().getOnlinePlayers().size()));
                        line = line.replace("%queueing%", String.valueOf(getQueueing()));
                        line = line.replace("%fighting%", String.valueOf(getFighting()));

                        line = line.replace("%kit%", practiceProfile.getEditing().getName());
                        toReturn.add(line);
                    }
                    break;
                }
                case PARTY: {
                    Party party = practiceProfile.getParty();
                    Player leader = this.managerHandler.getPlugin().getServer().getPlayer(party.getLeader());

                    title = managerHandler.getPlugin().getConfig().getString("scoreboard.PARTY-" + party.getPartyState().toString() + (party.getPartyState() == PartyState.MATCH && party.getMatch().getTeamTwo() == null ? "-FFA" : "") + (party.getPartyState() == PartyState.MATCH && party.getMatch().getKit() == null && practiceProfile.getHcfKit() == HCFKit.BARD ? "-BARD" : "") + ".title");
                    for (String line : managerHandler.getPlugin().getConfig().getStringList("scoreboard.PARTY-" + party.getPartyState().toString() + (party.getPartyState() == PartyState.MATCH && party.getMatch().getTeamTwo() == null ? "-FFA" : "") + (party.getPartyState() == PartyState.MATCH && party.getMatch().getKit() == null && practiceProfile.getHcfKit() == HCFKit.BARD ? "-BARD" : "") + ".slots")) {
                        line = line.replace("%online%", String.valueOf(this.managerHandler.getPlugin().getServer().getOnlinePlayers().size()));
                        line = line.replace("%queueing%", String.valueOf(getQueueing()));
                        line = line.replace("%fighting%", String.valueOf(getFighting()));

                        line = line.replace("%leader%", leader.getName());
                        line = line.replace("%members%", String.valueOf(party.getMembers().size()));

                        if (party.getPartyState() == PartyState.MATCH) {
                            Match match = party.getMatch();

                            if (match.getTeamTwo() != null) {
                                boolean t1 = match.getTeamOne().contains(player.getUniqueId());

                                line = line.replace("%alive%", String.valueOf(match.getAlive(t1)));
                                line = line.replace("%all%", String.valueOf(t1 ? match.getTeamOne().size() : match.getTeamTwo().size()));
                                line = line.replace("%ping%", String.valueOf(BukkitReflection.getPing(player)));

                                line = line.replace("%opponentAlive%", String.valueOf(match.getAlive(!t1)));
                                line = line.replace("%opponentAll%", String.valueOf(t1 ? match.getTeamTwo().size() : match.getTeamOne().size()));
                                line = line.replace("%time%", (match.getEndTime() != 0 ? DurationUtil.getDuration(match.getStartTime(), match.getEndTime()) : DurationUtil.getDuration(match.getStartTime())));

                                if (match.getKit() == null && practiceProfile.getHcfKit() == HCFKit.BARD) {
                                    line = line.replace("%energy%", String.valueOf(practiceProfile.getBardEnergy()));
                                }
                            } else {
                                line = line.replace("%alive%", String.valueOf(match.getAlive(true)));
                                line = line.replace("%all%", String.valueOf(match.getTeamOne().size()));
                                line = line.replace("%ping%", String.valueOf(BukkitReflection.getPing(player)));

                                line = line.replace("%time%", (match.getEndTime() != 0 ? DurationUtil.getDuration(match.getStartTime(), match.getEndTime()) : DurationUtil.getDuration(match.getStartTime())));
                            }
                        }
                        toReturn.add(line);
                    }
                    break;
                }
                case EVENT:
                case SPECTATING_EVENT: {
                    HostedEvent hostedEvent = practiceProfile.getHostedEvent();
                    title = managerHandler.getPlugin().getConfig().getString("scoreboard." + hostedEvent.getName().toUpperCase() + "-EVENT-" + hostedEvent.getEventState().toString() + ".title");
                    for (String line : managerHandler.getPlugin().getConfig().getStringList("scoreboard." + hostedEvent.getName().toUpperCase() + "-EVENT-" + hostedEvent.getEventState().toString() + ".slots")) {
                        line = line.replace("%online%", String.valueOf(this.managerHandler.getPlugin().getServer().getOnlinePlayers().size()));
                        line = line.replace("%queueing%", String.valueOf(getQueueing()));
                        line = line.replace("%fighting%", String.valueOf(getFighting()));

                        line = line.replace("%players%", String.valueOf(hostedEvent.getPlayers().size()));
                        line = line.replace("%max%", String.valueOf(this.managerHandler.getSettingsManager().getPlayersPerEvent()));
                        line = line.replace("%time%", hostedEvent.getStartTime() > 0 ? DurationUtil.getDuration(hostedEvent.getStartTime()) : DurationUtil.getDurationDown(hostedEvent.getDown(), 29));
                        toReturn.add(line);
                    }
                    break;
                }
                case TOURNAMENT: {
                    Tournament tournament = practiceProfile.getTournament();
                    title = managerHandler.getPlugin().getConfig().getString("scoreboard.TOURNAMENT-" + tournament.getTournamentState().toString() + ".title");
                    for (String line : managerHandler.getPlugin().getConfig().getStringList("scoreboard.TOURNAMENT-" + tournament.getTournamentState().toString() + ".slots")) {
                        line = line.replace("%online%", String.valueOf(this.managerHandler.getPlugin().getServer().getOnlinePlayers().size()));
                        line = line.replace("%queueing%", String.valueOf(getQueueing()));
                        line = line.replace("%fighting%", String.valueOf(getFighting()));

                        line = line.replace("%players%", String.valueOf(tournament.getParticipants().size()));
                        line = line.replace("%max%", String.valueOf(this.managerHandler.getSettingsManager().getPlayersPerTournament()));
                        line = line.replace("%time%", tournament.getStartTime() > 0 ? DurationUtil.getDuration(tournament.getStartTime()) : DurationUtil.getDurationDown(tournament.getDown(), 59));
                        line = line.replace("%round%", String.valueOf(tournament.getRound()));
                        line = line.replace("%kit%", tournament.getKit().getName());
                        toReturn.add(line);
                    }
                    break;
                }
                case SPECTATING: {
                    Match match = practiceProfile.getSpectating();
                    title = managerHandler.getPlugin().getConfig().getString("scoreboard.SPECTATING.title");
                    for (String line : managerHandler.getPlugin().getConfig().getStringList("scoreboard.SPECTATING.slots")) {
                        line = line.replace("%online%", String.valueOf(this.managerHandler.getPlugin().getServer().getOnlinePlayers().size()));
                        line = line.replace("%queueing%", String.valueOf(getQueueing()));
                        line = line.replace("%fighting%", String.valueOf(getFighting()));

                        line = line.replace("%kit%", match.getKit().getName());
                        line = line.replace("%player1%", match.getT1l().getName());
                        line = line.replace("%player2%", match.getT2l().getName());
                        toReturn.add(line);
                    }
                }

                case EVENT_MATCH: {
                    HostedEvent hostedEvent = practiceProfile.getHostedEvent();
                    title = managerHandler.getPlugin().getConfig().getString("scoreboard." + hostedEvent.getName().toUpperCase() + "-EVENT-" + "STARTED" + ".title");

                    SumoEvent sumoEvent = new SumoEvent(RevampPractice.getInstance());

                    Player player1 = sumoEvent.getP1();
                    Player player2 = sumoEvent.getP2();

                    for (String line : managerHandler.getPlugin().getConfig().getStringList("scoreboard." + hostedEvent.getName().toUpperCase() + "-EVENT-" + "STARTED" + ".slots")) {
                        line = line.replace("%online%", String.valueOf(this.managerHandler.getPlugin().getServer().getOnlinePlayers().size()));
                        line = line.replace("%queueing%", String.valueOf(getQueueing()));
                        line = line.replace("%fighting%", String.valueOf(getFighting()));

                        line = line.replace("%players%", String.valueOf(hostedEvent.getPlayers().size()));
                        line = line.replace("%max%", String.valueOf(this.managerHandler.getSettingsManager().getPlayersPerEvent()));
                        line = line.replace("%time%", hostedEvent.getStartTime() > 0 ? DurationUtil.getDuration(hostedEvent.getStartTime()) : DurationUtil.getDurationDown(hostedEvent.getDown(), 29));
                        line = line.replace("%opponent%", player1.equals(player) ? player2.getName() : player1.getName());
                        line = line.replace("%ping%", String.valueOf(BukkitReflection.getPing(player)));
                        line = line.replace("%opponentPing%", player1.equals(player) ? String.valueOf(BukkitReflection.getPing(player2)) : String.valueOf(BukkitReflection.getPing(player1)));
                        toReturn.add(line);
                    }
                }
            }
            if (!practiceProfile.isScoreboard()) {
                toReturn.clear();
            }
            scoreHelper.setTitle(title);
            scoreHelper.setSlotsFromList(toReturn);
        } catch (Exception ex) {
            //remove the errors :3
        }
    }

    private int getQueueing() {
        int queueing = 0;
        for (Kit kit : this.managerHandler.getKitManager().getKitsList()) {
            queueing += kit.getUnrankedQueue().size() + kit.getRankedQueue().size();
        }
        return queueing;
    }

    private int getFighting() {
        int fighting = 0;
        for (Kit kit : this.managerHandler.getKitManager().getKitsList()) {
            fighting += kit.getUnrankedMatch().size() + kit.getRankedMatch().size();
        }
        return fighting;
    }
}
