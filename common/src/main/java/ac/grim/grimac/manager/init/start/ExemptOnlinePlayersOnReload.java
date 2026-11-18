package ac.grim.grimac.manager.init.start;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.platform.api.player.PlatformPlayer;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.PlayerDataManager;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;

public class ExemptOnlinePlayersOnReload implements StartableInitable {

    // Long enough for PacketEvents to settle after /reload, short enough that a player
    // cannot remain unchecked for the rest of the session.
    private static final long GRACE_TICKS = 20L * 10L;

    @Override
    public void start() {
        PlayerDataManager data = GrimAPI.INSTANCE.getPlayerDataManager();
        for (PlatformPlayer player : GrimAPI.INSTANCE.getPlatformPlayerFactory().getOnlinePlayers()) {
            User user = PacketEvents.getAPI().getPlayerManager().getUser(player.getNative());
            if (user == null) continue;

            data.exemptTemporarily(user);
            GrimPlayer tracked = data.getPlayer(user);
            if (tracked != null) tracked.updatePermissions();

            GrimAPI.INSTANCE.getScheduler().getGlobalRegionScheduler().runDelayed(
                    GrimAPI.INSTANCE.getGrimPlugin(),
                    () -> {
                        // A real grim.exempt grant is handled by the permission refresh;
                        // this only ends the reload grace.
                        data.clearTemporaryExemption(user);
                        GrimPlayer playerData = data.getPlayer(user);
                        if (playerData != null) playerData.updatePermissions();
                    },
                    GRACE_TICKS);
        }
    }
}
