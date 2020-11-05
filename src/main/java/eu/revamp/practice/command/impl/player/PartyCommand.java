package eu.revamp.practice.command.impl.player;

import eu.revamp.practice.RevampPractice;
import eu.revamp.practice.command.Command;
import eu.revamp.practice.command.CommandArgs;
import eu.revamp.practice.kit.hcf.HCFKit;
import eu.revamp.practice.match.Match;
import eu.revamp.practice.match.MatchDeathReason;
import eu.revamp.practice.party.Party;
import eu.revamp.practice.party.PartyInvite;
import eu.revamp.practice.party.PartyState;
import eu.revamp.practice.player.PlayerState;
import eu.revamp.practice.player.PracticeProfile;
import eu.revamp.practice.util.enums.Messages;
import eu.revamp.spigot.utils.chat.color.CC;
import lombok.AllArgsConstructor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.craftbukkit.libs.joptsimple.internal.Strings;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("Duplicates")
@AllArgsConstructor
public class PartyCommand {

    private final RevampPractice plugin;

    @Command(name = "party", aliases = {"p"}, inGameOnly = true)
    public void party(CommandArgs args) {
        args.getSender().sendMessage(Messages.PARTY_HELP.getMessage());
    }

    @Command(name = "party.create", aliases = {"p.create"}, inGameOnly = true)
    public void partyCreate(CommandArgs args) {
        Player player = args.getPlayer();
        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);

        if (practiceProfile.getPlayerState() != PlayerState.LOBBY) {
            args.getSender().sendMessage(Messages.MUST_BE_IN_LOBBY.getMessage());
            return;
        }

        Party party = new Party();
        party.setLeader(player.getUniqueId());
        party.getMembers().add(player.getUniqueId());

        practiceProfile.setParty(party);
        practiceProfile.setPlayerState(PlayerState.PARTY);

        this.plugin.getManagerHandler().getPlayerManager().giveItems(player, true);
        args.getSender().sendMessage(Messages.CREATED_PARTY.getMessage());
    }

    @Command(name = "party.invite", aliases = {"p.invite"}, inGameOnly = true)
    public void partyInvite(CommandArgs args) {
        if (args.length() != 1) {
            args.getSender().sendMessage(CC.RED + "Usage: /" + args.getLabel() + " <player>");
            return;
        }
        Player player = args.getPlayer();
        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);

        if (practiceProfile.getPlayerState() != PlayerState.PARTY) {
            args.getSender().sendMessage(Messages.NOT_IN_PARTY.getMessage());
            return;
        }
        Party party = practiceProfile.getParty();

        if (party.getLeader() != player.getUniqueId()) {
            args.getSender().sendMessage(Messages.MUST_BE_PARTY_LEADER.getMessage());
            return;
        }

        if (party.getPartyState() != PartyState.LOBBY) {
            args.getSender().sendMessage(Messages.MUST_BE_IN_LOBBY.getMessage());
            return;
        }

        Player target = this.plugin.getServer().getPlayer(args.getArgs(0));
        if (target == null) {
            args.getSender().sendMessage(Messages.COULD_NOT_FIND_PLAYER.getMessage().replace("%player%", args.getArgs(0)));
            return;
        }

        PracticeProfile targetProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(target);

        if (targetProfile.getPlayerState() != PlayerState.LOBBY) {
            args.getSender().sendMessage(Messages.PLAYER_NOT_IN_LOBBY.getMessage().replace("%player%", target.getName()));
            return;
        }
        PartyInvite partyInvite = targetProfile.getPartyInviteList().stream().filter(p -> p.getParty() == party && System.currentTimeMillis() - p.getTimestamp() <= 60000).findFirst().orElse(null);

        if (partyInvite != null) {
            args.getSender().sendMessage(Messages.EXISTING_PARTY_INVITE.getMessage());
            return;
        }
        if (party.getMembers().size() >= this.plugin.getManagerHandler().getSettingsManager().getMaxPartyMembers()) {
            args.getSender().sendMessage(Messages.PARTY_FULL.getMessage());
            return;
        }
        targetProfile.getPartyInviteList().add(new PartyInvite(party));

        party.broadcast(Messages.PARTY_INVITE_SENT.getMessage().replace("%player%", target.getName()));

        TextComponent clickable = new TextComponent(Messages.PARTY_INVITE.getMessage().replace("%player%", player.getName()).replace("%amount%", String.valueOf(party.getMembers().size())));
        clickable.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Messages.PARTY_INVITE_HOVER.getMessage().replace("%player%", player.getName())).create()));
        clickable.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party join " + player.getName()));

        target.spigot().sendMessage(clickable);
    }

    @Command(name = "party.join", aliases = {"p.join"}, inGameOnly = true)
    public void partyJoin(CommandArgs args) {
        if (args.length() != 1) {
            args.getSender().sendMessage(CC.RED + "Usage: /" + args.getLabel() + " <player>");
            return;
        }
        Player player = args.getPlayer();
        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);

        if (practiceProfile.getPlayerState() != PlayerState.LOBBY) {
            args.getSender().sendMessage(Messages.MUST_BE_IN_LOBBY.getMessage());
            return;
        }

        Player target = this.plugin.getServer().getPlayer(args.getArgs(0));
        if (target == null) {
            args.getSender().sendMessage(Messages.COULD_NOT_FIND_PLAYER.getMessage().replace("%player%", args.getArgs(0)));
            return;
        }
        PracticeProfile targetProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(target);
        if (targetProfile.getPlayerState() != PlayerState.PARTY) {
            args.getSender().sendMessage(Messages.PLAYER_NOT_IN_PARTY.getMessage().replace("%player%", target.getName()));
            return;
        }
        Party party = targetProfile.getParty();
        if (party.getPartyState() != PartyState.LOBBY) {
            args.getSender().sendMessage(Messages.PLAYERS_PARTY_NOT_IN_LOBBY.getMessage().replace("%player%", target.getName()));
            return;
        }
        PartyInvite partyInvite = practiceProfile.getPartyInviteList().stream().filter(p -> p.getParty() == party && System.currentTimeMillis() - p.getTimestamp() <= 60000).findFirst().orElse(null);
        if (partyInvite == null && !party.isOpen()) {
            args.getSender().sendMessage(Messages.PARTY_INVITE_NOT_FOUND.getMessage());
            return;
        }
        if (party.getMembers().size() >= this.plugin.getManagerHandler().getSettingsManager().getMaxPartyMembers()) {
            args.getSender().sendMessage(Messages.PARTY_FULL.getMessage());
            return;
        }
        practiceProfile.getPartyInviteList().remove(partyInvite);
        practiceProfile.setParty(party);
        party.getMembers().add(player.getUniqueId());
        practiceProfile.setPlayerState(PlayerState.PARTY);

        this.plugin.getManagerHandler().getPlayerManager().giveItems(player, true);

        party.broadcast(Messages.PLAYER_JOINED_PARTY.getMessage().replace("%player%", player.getName()));
    }

    @Command(name = "party.leave", aliases = {"p.leave"}, inGameOnly = true)
    public void partyLeave(CommandArgs args) {
        Player player = args.getPlayer();
        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);

        if (practiceProfile.getPlayerState() != PlayerState.PARTY) {
            args.getSender().sendMessage(Messages.NOT_IN_PARTY.getMessage());
            return;
        }
        Party party = practiceProfile.getParty();
        if (party.getLeader() == player.getUniqueId()) {
            args.getSender().sendMessage(Messages.LEADER_CANT_LEAVE.getMessage());
            return;
        }
        party.getMembers().remove(player.getUniqueId());
        if (party.getPartyState() == PartyState.MATCH) {
            Match match = party.getMatch();

            match.getExempt().add(player.getUniqueId());

            this.plugin.getManagerHandler().getPlayerManager().teleportSpawn(player);
        }
        practiceProfile.setPlayerState(PlayerState.LOBBY);
        practiceProfile.setParty(null);
        practiceProfile.setHcfKit(HCFKit.DIAMOND);

        this.plugin.getManagerHandler().getPlayerManager().giveItems(player, true);
        party.broadcast(Messages.PLAYER_LEFT_PARTY.getMessage().replace("%player%", player.getName()));
        args.getSender().sendMessage(Messages.LEFT_PARTY.getMessage());
    }

    @Command(name = "party.kick", aliases = {"p.kick"}, inGameOnly = true)
    public void partyKick(CommandArgs args) {
        if (args.length() != 1) {
            args.getSender().sendMessage(CC.RED + "Usage: /" + args.getLabel() + " <player>");
            return;
        }
        Player player = args.getPlayer();
        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);

        if (practiceProfile.getPlayerState() != PlayerState.PARTY) {
            args.getSender().sendMessage(Messages.NOT_IN_PARTY.getMessage());
            return;
        }
        Party party = practiceProfile.getParty();
        if (party.getLeader() != player.getUniqueId()) {
            args.getSender().sendMessage(Messages.MUST_BE_PARTY_LEADER.getMessage());
            return;
        }
        Player target = this.plugin.getServer().getPlayer(args.getArgs(0));
        if (target == null) {
            args.getSender().sendMessage(Messages.COULD_NOT_FIND_PLAYER.getMessage().replace("%player%", args.getArgs(0)));
            return;
        }
        if (!party.getMembers().contains(target.getUniqueId())) {
            args.getSender().sendMessage(Messages.PLAYER_NOT_IN_YOUR_PARTY.getMessage().replace("%player%", target.getName()));
            return;
        }
        this.plugin.getServer().dispatchCommand(target, "party leave");
    }

    @Command(name = "party.open", aliases = {"p.open"}, inGameOnly = true)
    public void partyOpen(CommandArgs args) {
        Player player = args.getPlayer();
        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);

        if (practiceProfile.getPlayerState() != PlayerState.PARTY) {
            args.getSender().sendMessage(Messages.NOT_IN_PARTY.getMessage());
            return;
        }
        Party party = practiceProfile.getParty();
        if (party.getLeader() != player.getUniqueId()) {
            args.getSender().sendMessage(Messages.MUST_BE_PARTY_LEADER.getMessage());
            return;
        }
        party.setOpen(!party.isOpen());
        party.broadcast(party.isOpen() ? Messages.PARTY_OPENED.getMessage() : Messages.PARTY_CLOSED.getMessage());
    }

    @Command(name = "party.promote", aliases = {"p.promote"}, inGameOnly = true)
    public void partyPromote(CommandArgs args) {
        if (args.length() != 1) {
            args.getSender().sendMessage(CC.RED + "Usage: /" + args.getLabel() + " <player>");
            return;
        }
        Player player = args.getPlayer();
        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);

        if (practiceProfile.getPlayerState() != PlayerState.PARTY) {
            args.getSender().sendMessage(Messages.NOT_IN_PARTY.getMessage());
            return;
        }
        Party party = practiceProfile.getParty();
        if (party.getLeader() != player.getUniqueId()) {
            args.getSender().sendMessage(Messages.MUST_BE_PARTY_LEADER.getMessage());
            return;
        }
        Player target = this.plugin.getServer().getPlayer(args.getArgs(0));
        if (target == null) {
            args.getSender().sendMessage(Messages.COULD_NOT_FIND_PLAYER.getMessage().replace("%player%", args.getArgs(0)));
            return;
        }
        if (!party.getMembers().contains(target.getUniqueId())) {
            args.getSender().sendMessage(Messages.PLAYER_NOT_IN_YOUR_PARTY.getMessage().replace("%player%", target.getName()));
            return;
        }
        if (party.getLeader() == target.getUniqueId()) {
            args.getSender().sendMessage(Messages.CANT_PROMOTE_YOURSELF.getMessage());
            return;
        }
        party.setLeader(target.getUniqueId());
        if (party.getPartyState() == PartyState.LOBBY) {

            this.plugin.getManagerHandler().getPlayerManager().giveItems(player, true);
            this.plugin.getManagerHandler().getPlayerManager().giveItems(target, true);
        }
        party.broadcast(Messages.PLAYER_PROMOTED.getMessage().replace("%player%", target.getName()));
    }

    @Command(name = "party.disband", aliases = {"p.disband"}, inGameOnly = true)
    public void partyDisband(CommandArgs args) {
        Player player = args.getPlayer();
        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);

        if (practiceProfile.getPlayerState() != PlayerState.PARTY) {
            args.getSender().sendMessage(Messages.NOT_IN_PARTY.getMessage());
            return;
        }

        Party party = practiceProfile.getParty();
        if (party.getLeader() != player.getUniqueId()) {
            args.getSender().sendMessage(Messages.MUST_BE_PARTY_LEADER.getMessage());
            return;
        }

        try {
            party.getMembers().forEach(u -> {
                Player member = this.plugin.getServer().getPlayer(u);
                PracticeProfile memberProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(member);

                if (party.getPartyState() == PartyState.MATCH) {
                    Match match = party.getMatch();

                    match.addDeath(member, MatchDeathReason.LEFT, null);

                    this.plugin.getManagerHandler().getPlayerManager().teleportSpawn(player);
                }

                memberProfile.setPlayerState(PlayerState.LOBBY);
                memberProfile.setParty(null);
                memberProfile.setHcfKit(HCFKit.DIAMOND);

                this.plugin.getManagerHandler().getPlayerManager().giveItems(member, true);
                member.sendMessage(Messages.PARTY_DISBANDED.getMessage());
            });
            party.setPartyState(PartyState.LOBBY);
            party.setMatch(null);
            party.getMembers().clear();
        } catch (Exception e) {
            //remove errors :3
        }
    }

    @Command(name = "party.info", aliases = {"p.info"}, inGameOnly = true)
    public void partyInfo(CommandArgs args) {
        Player player = args.getPlayer();
        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);

        if (practiceProfile.getPlayerState() != PlayerState.PARTY) {
            args.getSender().sendMessage(Messages.NOT_IN_PARTY.getMessage());
            return;
        }
        Party party = practiceProfile.getParty();
        Player leader = this.plugin.getServer().getPlayer(party.getLeader());
        List<String> playerNames = new ArrayList<>();
        party.getMembers().stream().filter(u -> u != party.getLeader()).forEach(u -> playerNames.add(this.plugin.getServer().getPlayer(u).getName()));
        args.getSender().sendMessage(Messages.PARTY_INFO.getMessage().replace("%leader%", leader.getName()).replace("%amount%", String.valueOf(party.getMembers().size() - 1)).replace("%members%", Strings.join(playerNames, ", ")).replace("%status%", party.isOpen() ? "Open" : "Closed"));
    }
}
