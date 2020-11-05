package eu.revamp.practice.player;

import eu.revamp.practice.RevampPractice;
import eu.revamp.practice.hostedevent.HostedEvent;
import eu.revamp.practice.kit.CustomKit;
import eu.revamp.practice.kit.Kit;
import eu.revamp.practice.kit.hcf.HCFKit;
import eu.revamp.practice.match.Match;
import eu.revamp.practice.match.MatchRequest;
import eu.revamp.practice.party.Party;
import eu.revamp.practice.party.PartyInvite;
import eu.revamp.practice.queue.Queue;
import lombok.Getter;
import lombok.Setter;
import eu.revamp.practice.tournament.Tournament;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class PracticeProfile {

    private boolean openInventory;
    private Kit editInv;
    private boolean scoreboard = true;
    private PlayerState playerState = PlayerState.LOBBY;
    private Queue queue;
    private Party party;
    private Match match;
    private long lastEnderpearl;
    private Map<Kit, Integer> eloMap = new HashMap<>();
    private Kit editing;
    private Map<Kit, ArrayList<CustomKit>> customKitMap = new HashMap<>();
    private Inventory editLayoutInventory;
    private Inventory hcfKitSelectorInventory;
    private CustomKit settingName;
    private List<PartyInvite> partyInviteList = new ArrayList<>();
    private Match spectating;
    private boolean buildMode;
    private HCFKit hcfKit = HCFKit.DIAMOND;
    private List<MatchRequest> matchRequestList = new ArrayList<>();
    private Player dueling;
    private long lastBardBuff;
    private double bardEnergy;
    private List<String> activeBardItems = new ArrayList<>();
    private List<PotionEffect> potionEffectsList = new ArrayList<>();

    private Map<Player, Player> lastPlayerMatch = new HashMap<>();

    private long lastArcherSpeedBuff;
    private long lastArcherJumpBuff;
    private long lastArcherMark;
    private long lastRogueSpeedBuff;
    private long lastRogueJumpBuff;
    private long lastRogueBackstab;
    private HostedEvent hostedEvent;
    private Tournament tournament;
    private boolean viewingPlayerInv;
    private boolean silentMode;
    private boolean duelRequests = true;
    private int hits;
    private int combo;
    private int longestCombo;
    private int thrownPots;
    private int fullyLandedPots;

    public int getElo(Kit kit) {
        return eloMap.getOrDefault(kit, RevampPractice.getInstance().getManagerHandler().getSettingsManager().getDefaultElo());
    }

    public void setElo(Kit kit, int elo) {
        eloMap.put(kit, elo);
    }

    public CustomKit getCustomKit(Kit kit, int number) {
        CustomKit customKit = null;
        if (customKitMap.containsKey(kit)) {
            for (CustomKit customKits : customKitMap.get(kit)) {
                if (customKits.getNumber() == number) customKit = customKits;
            }
        }
        return customKit;
    }

    public void setCustomKit(Kit kit, CustomKit customKit) {
        ArrayList<CustomKit> customKits = customKitMap.getOrDefault(kit, new ArrayList<>());
        customKits.add(customKit);

        customKitMap.put(kit, customKits);
    }

    public void removeCustomKit(Kit kit, int number) {
        CustomKit customKit = getCustomKit(kit, number);

        ArrayList<CustomKit> customKits = customKitMap.getOrDefault(kit, new ArrayList<>());
        customKits.remove(customKit);

        customKitMap.put(kit, customKits);
    }

    public PotionEffect getPotionEffect(PotionEffectType potionEffectType) {
        PotionEffect potionEffect = null;
        for (PotionEffect potionEffects : potionEffectsList) {
            if (potionEffects.getType().getName().equalsIgnoreCase(potionEffectType.getName()))
                potionEffect = potionEffects;
        }
        return potionEffect;
    }
}
