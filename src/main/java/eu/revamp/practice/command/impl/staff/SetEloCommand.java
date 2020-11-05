package eu.revamp.practice.command.impl.staff;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import eu.revamp.practice.RevampPractice;
import eu.revamp.practice.kit.Kit;
import eu.revamp.practice.util.enums.Messages;
import eu.revamp.spigot.utils.chat.color.CC;
import lombok.AllArgsConstructor;
import eu.revamp.practice.command.Command;
import eu.revamp.practice.command.CommandArgs;
import eu.revamp.practice.player.PracticeProfile;
import org.apache.commons.lang.math.NumberUtils;
import org.bson.Document;
import org.bukkit.OfflinePlayer;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class SetEloCommand {

    private final RevampPractice plugin;

    @Command(name = "setelo", permission = "practice.command.setelo")
    public void setElo(CommandArgs args) {
        if (args.length() != 3) {
            args.getSender().sendMessage(CC.RED + "Usage: /" + args.getLabel() + " <player> <kit> <elo>");
            return;
        }
        OfflinePlayer offlinePlayer = this.plugin.getServer().getOfflinePlayer(args.getArgs(0));
        if (offlinePlayer == null || !offlinePlayer.hasPlayedBefore()) {
            args.getSender().sendMessage(Messages.NEVER_PLAYED_BEFORE.getMessage().replace("%player%", args.getArgs(0)));
            return;
        }
        String kitName = args.getArgs(1);
        Kit kit = this.plugin.getManagerHandler().getKitManager().getKit(kitName);
        if (kit == null) {
            args.getSender().sendMessage(Messages.KIT_DOESNT_EXIST.getMessage());
            return;
        }
        if (!kit.isRanked()) {
            args.getSender().sendMessage(Messages.KIT_ISNT_RANKED.getMessage());
            return;
        }
        if (!NumberUtils.isDigits(args.getArgs(2))) {
            args.getSender().sendMessage(Messages.INVALID_AMOUNT.getMessage());
            return;
        }
        int newElo = Integer.parseInt(args.getArgs(2));
        if (newElo < this.plugin.getManagerHandler().getSettingsManager().getMinElo() || newElo > this.plugin.getManagerHandler().getSettingsManager().getMaxElo()) {
            args.getSender().sendMessage(Messages.INVALID_ELO.getMessage());
            return;
        }
        if (offlinePlayer.isOnline()) {
            PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(offlinePlayer.getPlayer());
            practiceProfile.setElo(kit, newElo);
        } else {
            MongoCollection<Document> mongoCollection = this.plugin.getManagerHandler().getMongoManager().getMongoDatabase().getCollection(kit.getName());
            Map<String, Object> documentMap = new HashMap<>();

            Document document = mongoCollection.find(Filters.eq("uuid", offlinePlayer.getUniqueId().toString())).first();

            document.keySet().forEach(v -> documentMap.put(v, document.get(v)));

            documentMap.put("elo", newElo);

            if (document != null) mongoCollection.deleteOne(document);

            mongoCollection.insertOne(new Document(documentMap));
        }
        args.getSender().sendMessage(Messages.SET_ELO.getMessage().replace("%player%", offlinePlayer.getName()).replace("%kit%", kit.getName()).replace("%elo%", String.valueOf(newElo)));
    }
}
