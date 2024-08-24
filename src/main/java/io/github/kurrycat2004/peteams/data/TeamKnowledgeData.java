package io.github.kurrycat2004.peteams.data;

import io.github.kurrycat2004.peteams.PETeams;
import io.github.kurrycat2004.peteams.Tags;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class TeamKnowledgeData extends WorldSavedData {
    private final Map<String, Team> teams = new HashMap<>();

    private static final String ID = Tags.MODID + "_sync_knowledge";

    public TeamKnowledgeData(String name) {
        super(name);
    }

    public static TeamKnowledgeData getInstance() {
        MapStorage mapStorage = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().getMapStorage();
        if (mapStorage == null) {
            PETeams.LOGGER.error("Failed to get overworld map storage!");
            throw new NullPointerException("Map storage is null");
        }
        WorldSavedData data = mapStorage.getOrLoadData(TeamKnowledgeData.class, ID);
        TeamKnowledgeData instance;
        if (data == null) {
            instance = new TeamKnowledgeData(ID);
            mapStorage.setData(ID, instance);
        } else {
            instance = (TeamKnowledgeData) data;
        }
        return instance;
    }

    public Team getTeam(String uuid) {
        return teams.computeIfAbsent(uuid, Team::new);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        for (NBTBase tag : nbt.getTagList("teams", Constants.NBT.TAG_COMPOUND)) {
            NBTTagCompound teamTag = (NBTTagCompound) tag;
            String teamUuid = teamTag.getString("uuid");

            if (!teams.containsKey(teamUuid)) teams.put(teamUuid, new Team());
            teams.get(teamUuid).readFromNBT(teamTag.getCompoundTag("team"));
        }
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList teamTagList = new NBTTagList();
        teams.forEach((uuid, team) -> {
            NBTTagCompound teamTag = new NBTTagCompound();
            teamTag.setString("uuid", uuid);
            teamTag.setTag("team", team.writeToNBT(new NBTTagCompound()));
            teamTagList.appendTag(teamTag);
        });
        compound.setTag("teams", teamTagList);
        return compound;
    }
}
