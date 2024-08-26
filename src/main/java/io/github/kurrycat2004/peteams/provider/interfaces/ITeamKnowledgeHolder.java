package io.github.kurrycat2004.peteams.provider.interfaces;

import io.github.kurrycat2004.peteams.data.Team;
import org.jetbrains.annotations.Nullable;

public interface ITeamKnowledgeHolder extends IKnowledgeHolder {
    boolean isShareEmc();

    boolean isShareKnowledge();

    @Nullable Team getTeam();
}
