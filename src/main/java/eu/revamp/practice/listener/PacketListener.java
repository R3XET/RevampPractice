package eu.revamp.practice.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import eu.revamp.practice.RevampPractice;
import eu.revamp.practice.player.PracticeProfile;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

@SuppressWarnings("Duplicates")
public class PacketListener implements Listener {

    private final RevampPractice instance;

    public PacketListener(RevampPractice instance) {
        this.instance = instance;

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(instance, PacketType.Play.Client.CLIENT_COMMAND) {
            @Override
            public void onPacketReceiving(PacketEvent event) {

                Player player = event.getPlayer();
                PracticeProfile practiceProfile = instance.getManagerHandler().getProfileManager().getProfile(player);

                practiceProfile.setOpenInventory(true);
            }
        });

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(instance, PacketType.Play.Server.NAMED_SOUND_EFFECT) {
            @Override
            public void onPacketSending(PacketEvent event) { // took this from joel

                Player player = event.getPlayer();
                PacketContainer packet = event.getPacket();

                String name = packet.getStrings().read(0);
                if (name.contains("random") || name.contains("hurt") || name.startsWith("note") || name.contains("random.successful_hit") || name.contains("weather")) {
                    event.setCancelled(false);
                    return;
                }

                Location soundLocation = new Location(player.getWorld(), packet.getIntegers().read(0) / 8.0, packet.getIntegers().read(1) / 8.0, packet.getIntegers().read(2) / 8.0);

                Player closest = null;
                double bestDistance = Double.MAX_VALUE;

                for (Player p : player.getWorld().getPlayers()) {

                    if (p.getLocation().distance(soundLocation) < bestDistance) {
                        bestDistance = p.getLocation().distance(soundLocation);
                        closest = p;
                    }
                }

                if (closest != null) {
                    event.setCancelled(player.canSee(closest));
                }
            }
        });

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(instance, PacketType.Play.Server.PLAYER_INFO) {
            @Override
            public void onPacketSending(PacketEvent event) {
       //         if (!instance.getManagerHandler().getSettingsManager().isHideInvisiblesInTab()) {
                    event.setCancelled(false);
         //       }
            }
        });
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent potionSplashEvent) {
        if (potionSplashEvent.getEntity().getShooter() instanceof Player) {
            Player shooter = (Player) potionSplashEvent.getEntity().getShooter();

            com.comphenix.protocol.events.PacketListener particleListener = new PacketAdapter(instance, PacketType.Play.Server.WORLD_EVENT) {
                @Override
                public void onPacketSending(PacketEvent event) {
                    Player player = event.getPlayer();
                    event.setCancelled(!player.canSee(shooter));
                }
            };
            ProtocolLibrary.getProtocolManager().addPacketListener(particleListener);
            instance.getServer().getScheduler().scheduleSyncDelayedTask(instance, () -> {
                ProtocolLibrary.getProtocolManager().removePacketListener(particleListener);
            }, 20L);
        }
    }

    @EventHandler
    public void onLaunch(ProjectileLaunchEvent projectileLaunchEvent) {
        if (projectileLaunchEvent.getEntity().getShooter() instanceof Player) {
            Player shooter = (Player) projectileLaunchEvent.getEntity().getShooter();

            instance.getServer().getOnlinePlayers().stream().filter(p -> p != shooter && !p.canSee(shooter)).forEach(p -> {
                instance.getEntityHider().hideEntity(p, projectileLaunchEvent.getEntity());
            });
        }
    }
}
