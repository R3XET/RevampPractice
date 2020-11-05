package eu.revamp.practice.util.enums;

import eu.revamp.practice.RevampPractice;
import eu.revamp.spigot.utils.chat.color.CC;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Messages {

    ACTIVATED_ARCHER_BUFF("&6(&eArcher&6) &8» &eYou have activated the buff &a%buff%&e."),
    ACTIVATED_BARD_BUFF("&6(&eBard&6) &8» &eYou have activated the buff &a%buff%&e."),
    ACTIVATED_ROGUE_BUFF("&6(&eRogue&6) &8» &eYou have activated the buff &a%buff%&e."),
    ARCHER_KIT_SET("&eYou have set the kit for HCF &aArcher&e."),
    ARCHER_TAGGED("&6(&eRogue&6) &8» &eYou have been archer tagged for &615 &eseconds!"),
    ARENA_ALREADY_EXISTS("&cThat arena already exists."),
    ARENA_CREATED("&eYou have created the arena &a%arena%&e."),
    ARENA_DELETED("&eYou have deleted the arena &a%arena%&e."),
    ARENA_DOESNT_EXIST("&cThat arena doesn't exist."),
    ARENA_FIRST_SET("&eYou set the first location for arena &a%arena%&e."),
    ARENA_SECOND_SET("&eYou set the second location for arena &a%arena%&e."),
    ARENA_TYPE_UPDATED("&eYou have updated the type of arena &a%arena%&e to &a%type%&e."),
    BACKSTABBED("&6(&eRogue&6) &8» &eYou were backstabbed by &6%player%&e."),
    BACKSTABBED_PLAYER("&6(&eRogue&6) &8» &eYou backstabbed &6%player%&e."),
    BARD_KIT_SET("&eYou have set the kit for HCF &aBard&e."),
    BUILDER_DISABLED("&6(&eBuilder&6) &8» &cDisabled"),
    BUILDER_ENABLED("&6(&eBuilder&6) &8» &aEnabled"),
    CANT_DROP_FIRST_ITEM("&cYou can't drop the item in your first slot."),
    CANT_DUEL_YOURSELF("&cYou can't duel yourself!"),
    CANT_PROMOTE_YOURSELF("&aYou can't promote yourself!"),
    COULD_NOT_FIND_PLAYER("&cCould not find player with the name %player%."),
    CREATED_PARTY("&eYou have created a party!"),
    CUSTOM_NAME_SET("&eCustom name for %kit% &a#%number% to &r%name%&e."),
    DELETED_CUSTOM_KIT("&eDeleted custom kit &a%kit% #%number%&e."),
    DIAMOND_KIT_SET("&eYou have set the kit for HCF &aDiamond&e."),
    DUEL_HOVER("Click here to accept %player%'s duel request."),
    DUEL_REQUEST("&a%player% &ehas sent you a duel request with the kit &a%kit% &a[Accept]"),
    DUEL_REQUEST_DISABLED("&6(&eDuel Requests&6) &8» &cDisabled"),
    DUEL_REQUEST_ENABLED("&6(&eDuel Requests&6) &8» &aEnabled"),
    DUEL_REQUEST_EXISTS("&cYou already have an existing duel request to that player."),
    DUEL_REQUEST_NOT_FOUND("&cThat duel request was not found."),
    EDITOR_SET("&aYou have successfully set the editor."),
    ELO_CHANGES("&eElo changes: &a%winner% +%eloDifference% (%newWinnerElo%) &c%loser% -%eloDifference% (%newLoserElo%)"),
    EQUIPPED_CUSTOM_KIT("&eEquipped custom kit &r%name%&e."),
    EQUIPPED_DEFAULT_KIT("&eEquipped default kit for %kit%."),
    EVENT_ALREADY_STARTED("&cThat event has already started."),
    EVENT_CANCELLED("&6(&eEvent&6) &8» &eThe &a%event% &ehas been cancelled."),
    EVENT_DELAY("You must wait for the event cooldown to end before starting another one."),
    EVENT_DOESNT_EXIST("&cThat event doesn't exist."),
    EVENT_FIRST_SET("&eThe first spawn for event &a%event% &ehas been set."),
    EVENT_FULL("&cThat event is full."),
    EVENT_HELP("&8&m--------------------------------------\n" +
            "&d&lEvent Help\n" +
            "&8&m--------------------------------------\n" +
            "&d/event start <name> - Start an event\n" +
            "/event join (name) - Join an event\n" +
            "/event leave - Leave an event\n" +
            "/event spectate (name) - Spectate an event\n" +
            "&8&m--------------------------------------"),
    EVENT_HOVER("Click to join the %event% event."),
    EVENT_LOBBY_SET("&eThe lobby for event &a%event% &ehas been set."),
    EVENT_PLAYER_ELIMINATED("&c%player% &7has been eliminated."),
    EVENT_SECOND_SET("&eThe second spawn for event &a%event% &ehas been set."),
    EVENT_STARTING("&a%player% &eis hosting a &a%event% &eevent! &a[Join]"),
    EVENT_WINNER("&6(&eEvent&6) &8» &a%player% &ehas won the &a%event% &eevent!"),
    EXISTING_PARTY_INVITE("&cYou already have an existing party invite out to that player."),
    FEATURE_DISABLED("&cThis feature is currently disabled."),
    GG_HOVER("&5Click here to send a GG"),
    HAND_CANT_BE_AIR("&cYour hand may not be air."),
    INVALID_AMOUNT("&cInvalid amount."),
    INVALID_ELO("&cThe elo amount you have entered is either too little or too big."),
    INVALID_PAGE_NUMBER("&cInvalid page number."),
    INVALID_STATE("&cYou cannot do this in your current state."),
    INVENTORY_HOVER("Click here to view %player%'s inventory."),
    INVENTORY_NOT_FOUND("&cThere was no inventory found for that player."),
    JOINED_RANKED_QUEUE("&aYou are now queued for Ranked %kit%."),
    JOINED_UNRANKED_QUEUE("&aYou are now queued for Unranked %kit%."),
    KIT_ALREADY_EXISTS("&cThat kit already exists."),
    KIT_CREATED("&eYou have created the kit &a%kit%&e."),
    KIT_DELETED("&eYou have deleted the kit &a%kit%&e."),
    KIT_DISPLAY_SET("&eYou have updated the display of kit &a%kit%&e."),
    KIT_DOESNT_EXIST("&cThat kit doesn't exist."),
    KIT_EDITABLE_UPDATED("&eYou have updated editable of kit &a%kit% &eto &a%value%&e."),
    KIT_EDITCHEST_UPDATED("&eYou have updated the edit chest of kit &a%kit% &eto &a%value%&e."),
    KIT_EDITINV_UPDATED("&eYou have set the edit inventory for the kit &a%kit%&e."),
    KIT_INVENTORY_SET("&eYou have set the inventory for the kit &a%kit%&e."),
    KIT_ISNT_RANKED("&cThat kit isn't ranked."),
    KIT_NOT_SET("&cYou don't have an inventory set for that kit."),
    KIT_RANKED_UPDATED("&eYou have updated ranked of kit &a%kit% &eto &a%value%&e."),
    KIT_TICKS_UPDATED("&eYou have updated the type of kit &a%kit%&e to &a%ticks%&e."),
    KIT_TYPE_DOESNT_EXIST("&cThat kit type doesn't exist."),
    KIT_TYPE_UPDATED("&eYou have updated the type of kit &a%kit%&e to &a%type%&e."),
    KIT_UNRANKED_UPDATED("&eYou have updated unranked of kit &a%kit% &eto &a%value%&e."),
    LEADERBOARDS_HEADER("&a%kit% &eleaderboards page &a%page%"),
    LEADERBOARDS_PAGE_DOESNT_EXIST("&cThat page doesn't exist."),
    LEADERBOARDS_PLAYER("&a#%place%&e. &a%player% &e(&a%elo%&e)"),
    LEADER_CANT_LEAVE("&cYou are the party leader. To leave the party, you must promote someone else or disband the party."),
    LEFT_EVENT("&cYou have left the event."),
    LEFT_PARTY("&cYou have left the party."),
    LEFT_QUEUE("&cYou have left the queue for %queueType% %kit%."),
    LEFT_TOURNAMENT("&cYou have left the tournament."),
    LOADED_CUSTOM_KIT("&eLoaded custom kit for %kit% &a#%number%."),
    LOADED_KIT("&eYou have loaded the kit &a%kit%&e."),
    MATCH_END("&bPost-match inventories &7(Click to view):\n" +
            "\n" +
            "&6Winner: &a%winnerNames%\n" +
            "&6Loser: &c%loserNames%") //MATCH_END("&bPost-match inventories &7(Click to view):\n\n&6Winner: &a%winnerNames%\n&6Loser: &c%loserNames%\n&7(&5&lGG&7) - Soon..."),
    ,
    MATCH_STARTED("&aMatch started."),
    MATCH_STARTING("&e%time%..."),
    MAY_BACKSTAB_AGAIN("&6(&eRogue&6) &8» &eYou may use backstab again!"),
    MAY_PEARL_AGAIN("&aYou may pearl again!"),
    MUST_BE_IN_LOBBY("&cYou must be in the lobby to do this."),
    MUST_BE_PARTY_LEADER("&cYou must be the party leader to do this."),
    MUST_CREATE_PARTY_TO_DUEL("&cYou must create a party to duel %player%'s party."),
    MUST_NEED_TWO_MEMBERS("&cYou need at least 2 players in your party to do this."),
    MUST_WAIT_TO_ARCHER_BUFF("&cYou can only buff every 60 seconds. You must wait %time%s."),
    MUST_WAIT_TO_BACKSTAB("&cYou must wait to %time%s to backstab again."),
    MUST_WAIT_TO_BARD_BUFF("&cYou can only buff every 6 seconds. You must wait %time%s."),
    MUST_WAIT_TO_PEARL("&cYou must wait %time%s to do that again!"),
    MUST_WAIT_TO_ROGUE_BUFF("&cYou can only buff every 60 seconds. You must wait %time%s."),
    NEED_MORE_ENERGY("&cYou need %energy% energy to do this."),
    NEVER_PLAYED_BEFORE("&c%player% has never played before."),
    NOT_HCF_ARENA("&cThat is not an HCF arena."),
    NOT_IN_EVENT("&cYou are not in an event."),
    NOT_IN_PARTY("&cYou are not in a party."),
    NOT_IN_TOURNAMENT("&cYou are not in a tournament."),
    NO_EVENT_STARTED("&cThere is no event by that name started."),
    NO_LONGER_ARCHER_MARKED("&6(&eArcher&6) &8» &eYou are no longer archer tagged!"),
    NO_LONGER_SPECTATING("&cYou are no longer spectating."),
    NO_TOURNAMENT_STARTED("&cThere is no tournament started."),
    NPC_HELP("&8&m--------------------------------------" +
            "\n&2&lNPC Help" +
            "\n&8&m--------------------------------------" +
            "\n&2/npc spawnEditor - Spawn the NPC editor" +
            "\n&2/npc despawnEditor - Despawn the NPC editor" +
            "\n&2/npc setLocation - Set NPC location" +
            "\n&2/npc setName (name) - Set NPC editor name" +
            "\n&2/npc setSkin (skin) - Set NPC editor skin" +
            "\n&8&m--------------------------------------"),
    PARTY_CLOSED("&eThe party has been &cclosed&e."),
    PARTY_DISBANDED("&cYour party has been disbanded."),
    PARTY_DOESNT_EXIST("&cThat party no longer exists."),
    PARTY_DUEL_HOVER("Click here to accept %player%'s party duel request."),
    PARTY_DUEL_REQUEST("&a%player%'s Party &e(&a%amount%&e) has sent you a duel request with the kit &a%kit% &a[Accept]"),
    PARTY_FFA_STARTING("&eParty ffa starting..."),
    PARTY_FULL("&cThat party is full."),
    PARTY_HELP("&8&m--------------------------------------\n" +
            "&d&lParty Help\n" +
            "&8&m--------------------------------------\n" +
            "&d/party create - Create a party\n" +
            "/party invite (player) - Invite a player to your party\n" +
            "/party join (player) - Join a player's party\n" +
            "/party leave - Leave your party\n" +
            "/party kick (player) - Kick a player from your party\n" +
            "/party open - Open your party\n" +
            "/party promote (player) - Transfer leadership of party\n" +
            "/party disband - Disband party\n" +
            "/party info - View party details\n" +
            " \n" +
            "Use '&6@&d' &dto type in party chat\n" +
            "&8&m--------------------------------------"),
    PARTY_INFO("&8&m--------------------------------------\n" +
            "&6Leader: &e%leader%\n" +
            "&6Members (&e%amount%&6): &e%members%\n" +
            "&6Status: &e%status%\n" +
            "&8&m--------------------------------------"),
    PARTY_INVITE("&a%player% &ehas invited you to their party (&a%amount%&e) &a[Accept]"),
    PARTY_INVITE_HOVER("Click here to join %player%'s party."),
    PARTY_INVITE_NOT_FOUND("&cYou do not have an invite from that party."),
    PARTY_INVITE_SENT("&a%player% &ehas been invited to the party."),
    PARTY_NOT_IN_LOBBY("&cThat party is not in the lobby."),
    PARTY_OPENED("&eThe party has been &aopened&e."),
    PLAYERS_PARTY_NOT_IN_LOBBY("&c%player%'s party is not in the lobby."),
    PLAYER_ARCHER_TAGGED("&6(&eArcher&6) &8» &eYou have archer tagged &6%player% &efor &615 &eseconds!"),
    PLAYER_DEATH_DIED("&c%player% &7died."),
    PLAYER_DEATH_DISCONNECTED("&c%player% &7disconnected."),
    PLAYER_DEATH_KILLED("&c%player% &7was killed by &a%killer%&7."),
    PLAYER_DEATH_LEFT("&c%player% &7left."),
    PLAYER_DISABLED_DUELS("&c%player% has duel requests disabled."),
    PLAYER_ELIMINATED_TOURNAMENT("&a%player% &ehas been eliminated!"),
    PLAYER_HEALTH("&a%player%'s &ehealth is &c%health% \u2764"),
    PLAYER_JOINED_EVENT("&a%player% &ehas joined the event!"),
    PLAYER_JOINED_PARTY("&a%player% &ehas joined the party."),
    PLAYER_JOINED_TOURNAMENT("&a%player% &ehas joined the tournament &a(%players%/%max%)"),
    PLAYER_LEFT_EVENT("&a%player% &ehas left the event!"),
    PLAYER_LEFT_PARTY("&a%player% &ehas left the party."),
    PLAYER_LEFT_TOURNAMENT("&a%player% &ehas left the tournament &a(%players%/%max%)"),
    PLAYER_NOT_IN_LOBBY("&c%player% is not in the lobby."),
    PLAYER_NOT_IN_MATCH("&c%player% is not in a match."),
    PLAYER_NOT_IN_PARTY("&c%player% is not in a party."),
    PLAYER_NOT_IN_YOUR_PARTY("&c%player% is not in your party."),
    PLAYER_NOW_SPECTATING("&7* &a%player% &eis now spectating."),
    PLAYER_NO_LONGER_SPECTATING("&7* &a%player% &eis no longer spectating."),
    PLAYER_PROMOTED("&a%player% &ehas been promoted to party leader."),
    PROVIDE_A_MESSAGE("&cPlease provide a message."),
    RANKED_MATCH_FOUND("&e&lMatch found! &eOpponent: &b%player% (%elo% Elo)"),
    ROGUE_KIT_SET("&eYou have set the kit for HCF &aRogue&e."),
    SAVED_CUSTOM_KIT("&eSaved custom kit &a%kit% #%number%&e."),
    SCOREBOARD_DISABLED("&6(&eScoreboard&6) &8» &cDisabled"),
    SCOREBOARD_ENABLED("&6(&eScoreboard&6) &8» &aEnabled"),
    SENT_DUEL("&eSent a duel request to &a%player% &ewith the kit &a%kit%&e."),
    SENT_PARTY_DUEL("&eSent a duel request to &a%player%'s Party &ewith the kit &a%kit%&e."),
    SET_CUSTOM_NAME("&eType your kit name in chat (color codes are applicable)"),
    SET_ELO("&eYou have set &a%player%'s &eelo for kit &a%kit% &eto &a%elo%&e."),
    SILENT_DISABLED("&6(&eSilent&6) &8» &cDisabled"),
    SILENT_ENABLED("&6(&eSilent&6) &8» &aEnabled"),
    SPAWN_SET("&aYou have successfully set the spawn."),
    SUMO_EVENT_MATCHUP("&6(&eSumo&6) &8» &eNow fighting: &a%player1% &evs. &a%player2%"),
    SUMO_EVENT_MATCH_STARTED("&aMatch started!"),
    SUMO_EVENT_MATCH_STARTING("&e%time%..."),
    SUMO_ROUND_STARTING("&6(&eSumo&6) &8» &eNext round starting in &a%time%..."),
    TELEPORTING_TO_SPAWN("&eTeleporting you to spawn..."),
    TOURNAMENT_ALREADY_STARTED("&cThere is already a tournament started."),
    TOURNAMENT_CANCELLED("&6(&eTournament&6) &8» &eThe &a%kit% &etournament has been cancelled."),
    TOURNAMENT_FULL("&cThe tournament is full."),
    TOURNAMENT_HELP("&8&m--------------------------------------\n" +
            "&d&lTournament Help\n" +
            "&8&m--------------------------------------\n" +
            "&d/tournament join - Join a tournament\n" +
            "/tournament leave - Leave a tournament\n" +
            "/tournament status - View all tournament matches\n" +
            "&8&m--------------------------------------"),
    TOURNAMENT_HOVER("Click to join the %kit% tournament."),
    TOURNAMENT_NEXT_ROUND("&6(&eTournament&6) &8» &eRound &a%round% &ewill start in &a30 &eseconds."),
    TOURNAMENT_NO_FIGHTS("&cThere are no fights happening."),
    TOURNAMENT_SITOUT("&cWe could not find you a match. You will automatically be advanced to the next round."),
    TOURNAMENT_STARTING("&eThere is a &a%kit% &etournament starting! &a[Join]"),
    TOURNAMENT_STATUS_FOOTER("&8&m-----------------------------------"),
    TOURNAMENT_STATUS_HEADER("&8&m-----------------------------------"),
    TOURNAMENT_STATUS_MATCH("&d%player1% vs. %player2%"),
    TOURNAMENT_WINNER("&6(&eTournament&6) &8» &a%player% &ehas won the &a%kit% &etournament!"),
    UNRANKED_MATCH_FOUND("&e&lMatch found! &eOpponent: &b%player%");

    private String defaultMessage;

    public String getMessage() {
        return (RevampPractice.getInstance().getManagerHandler().getConfigurationManager().getMessagesFile().get(getPath()) != null ? CC.translate(RevampPractice.getInstance().getManagerHandler().getConfigurationManager().getMessagesFile().getString(getPath())) : CC.translate(defaultMessage)).replace("%splitter%", "┃");
    }

    public String getPath() {
        return this.toString().replace("_", "-");
    }

}
