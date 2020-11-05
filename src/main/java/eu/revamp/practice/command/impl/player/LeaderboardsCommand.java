package eu.revamp.practice.command.impl.player;

import eu.revamp.practice.RevampPractice;
import eu.revamp.practice.command.Command;
import eu.revamp.practice.command.CommandArgs;
import eu.revamp.practice.kit.Kit;
import eu.revamp.practice.player.LeaderboardPlayer;
import eu.revamp.practice.player.PracticeProfile;
import eu.revamp.practice.util.enums.Menus;
import eu.revamp.practice.util.enums.Messages;
import eu.revamp.spigot.utils.chat.color.CC;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.math.NumberUtils;
import org.bson.Document;
import org.bukkit.entity.Player;

import java.util.*;

@SuppressWarnings("Duplicates")
@AllArgsConstructor
public class LeaderboardsCommand {

    private final RevampPractice plugin;

    @Command(name = "leaderboards", aliases = {"lbs"}, inGameOnly = true)
    public void leaderboards(CommandArgs args) {
        if (this.plugin.getManagerHandler().getSettingsManager().isLeaderboardsGUI()) {
            Player player = args.getPlayer();

            player.openInventory(Menus.LEADERBOARDS.getInventory());
            return;
        }
        if (args.length() != 2) {
            args.getSender().sendMessage(CC.RED + "Usage: /" + args.getLabel() + " <kit> <page>");
            return;
        }
        Kit kit = this.plugin.getManagerHandler().getKitManager().getKit(args.getArgs(0));
        if (kit == null) {
            args.getSender().sendMessage(Messages.KIT_DOESNT_EXIST.getMessage());
            return;
        }
        if (!kit.isRanked()) {
            args.getSender().sendMessage(Messages.KIT_ISNT_RANKED.getMessage());
            return;
        }
        if (!NumberUtils.isDigits(args.getArgs(1))) {
            args.getSender().sendMessage(Messages.INVALID_PAGE_NUMBER.getMessage());
            return;
        }
        int page = Integer.parseInt(args.getArgs(1));

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
        int start = (page * 10) - 10;
        int end = (page * 10) - 1;

        if (leaderboardPlayers.size() < start) {
            args.getSender().sendMessage(Messages.LEADERBOARDS_PAGE_DOESNT_EXIST.getMessage());
            return;
        }

        leaderboardPlayers.sort(Comparator.comparing(LeaderboardPlayer::getElo));
        Collections.reverse(leaderboardPlayers);

        args.getSender().sendMessage(Messages.LEADERBOARDS_HEADER.getMessage().replace("%kit%", kit.getName()).replace("%page%", String.valueOf(page)));
        for (int i = start; i <= end; i++) {
            if (i < leaderboardPlayers.size()) {
                LeaderboardPlayer leaderboardPlayer = leaderboardPlayers.get(i);

                args.getSender().sendMessage(Messages.LEADERBOARDS_PLAYER.getMessage().replace("%place%", String.valueOf(i + 1)).replace("%player%", leaderboardPlayer.getName()).replace("%elo%", String.valueOf(leaderboardPlayer.getElo())));
            }
        }
    }
}
