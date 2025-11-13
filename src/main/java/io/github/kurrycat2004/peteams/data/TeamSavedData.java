package io.github.kurrycat2004.peteams.data;

import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.Universe;
import io.github.kurrycat2004.peteams.PETeams;
import io.github.kurrycat2004.peteams.Tags;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeamSavedData extends WorldSavedData {
    private static final String ID = Tags.MODID + "_sync_knowledge";

    private final Map<Short, Team> teams = new HashMap<>();

    public TeamSavedData(String name) {
        super(name);
    }

    public static TeamSavedData getInstance() {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            throw new IllegalStateException("Tried to get TeamSavedData on client side!");
        }
        MapStorage mapStorage = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().getMapStorage();
        if (mapStorage == null) {
            PETeams.LOGGER.error("Failed to get overworld map storage!");
            throw new NullPointerException("Map storage is null");
        }
        WorldSavedData data = mapStorage.getOrLoadData(TeamSavedData.class, ID);
        TeamSavedData instance;
        if (data == null) {
            instance = new TeamSavedData(ID);
            mapStorage.setData(ID, instance);
        } else {
            instance = (TeamSavedData) data;
        }
        return instance;
    }

    public static Collection<Team> getTeams() {
        return TeamSavedData.getInstance().teams.values();
    }

    @Nullable
    public static Team getCommonTeam(@NotNull UUID playerUUID, @NotNull UUID otherPlayerUUID) {
        if (!Universe.loaded()) return null;
        ForgePlayer forgePlayer = Universe.get().getPlayer(playerUUID);
        if (forgePlayer == null || !forgePlayer.hasTeam()) return null;
        ForgePlayer otherForgePlayer = Universe.get().getPlayer(otherPlayerUUID);
        if (otherForgePlayer == null || !otherForgePlayer.hasTeam()) return null;

        if (forgePlayer.team.equalsTeam(otherForgePlayer.team)) {
            return getTeam(forgePlayer.team.getUID());
        }
        return null;
    }

    /**
     * This <strong>always</strong> returns a Team instance. If the team does not exist, it will be created.
     *
     * @param uuid Team UUID
     * @return Team instance
     */
    public static @NotNull Team getTeam(short uuid) {
        return TeamSavedData.getInstance().teams.computeIfAbsent(uuid, Team::new);
    }

    public static @Nullable ForgePlayer getPlayer(@NotNull EntityPlayer player) {
        return getPlayer(player.getUniqueID());
    }

    public static @Nullable ForgePlayer getPlayer(UUID playerUUID) {
        if (!Universe.loaded()) return null;
        ForgePlayer forgePlayer = Universe.get().getPlayer(playerUUID);
        if (forgePlayer == null) {
            PETeams.LOGGER.warn("Failed to get ForgePlayer for player {}", playerUUID);
            PETeams.logStackTrace();
        }
        return forgePlayer;
    }

    public static @Nullable Team getTeamFromPlayerUUID(UUID playerUUID) {
        ForgePlayer forgePlayer = getPlayer(playerUUID);
        if (forgePlayer == null) return null;
        if (forgePlayer.team == null || !forgePlayer.hasTeam()) return null;

        return TeamSavedData.getTeam(forgePlayer.team.getUID());
    }

    public static @Nullable Team getTeamFromPlayer(EntityPlayer player) {
        return getTeamFromPlayerUUID(player.getUniqueID());
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        for (NBTBase tag : nbt.getTagList("teams", Constants.NBT.TAG_COMPOUND)) {
            NBTTagCompound teamTag = (NBTTagCompound) tag;
            String uuidString = teamTag.getString(Team.TAG_UUID);
            short uuid;
            if (uuidString.isEmpty()) {
                uuid = teamTag.getShort(Team.TAG_UUID);
            } else {
                // backwards compat, was String before
                uuid = Integer.valueOf(uuidString, 16).shortValue();
            }

            if (!teams.containsKey(uuid)) teams.put(uuid, new Team());
            teams.get(uuid).readFromNBT(teamTag.getCompoundTag("team"));
        }
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList teamTagList = new NBTTagList();
        teams.forEach((uuid, team) -> {
            NBTTagCompound teamTag = new NBTTagCompound();
            teamTag.setShort(Team.TAG_UUID, uuid);
            teamTag.setTag("team", team.writeToNBT(new NBTTagCompound()));
            teamTagList.appendTag(teamTag);
        });
        compound.setTag("teams", teamTagList);
        return compound;
    }
}
