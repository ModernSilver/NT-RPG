package cz.neumimto.rpg.skills;

import cz.neumimto.rpg.players.groups.ClassDefinition;

import java.util.Map;

public interface IPlayerSkillHandler {

    default PlayerSkillContext get(ISkill iSkill) {
        return get(iSkill.getId());
    }

    void add(String iSkill, PlayerSkillContext source);

    PlayerSkillContext get(String id);

    default void add(ISkill skill, PlayerSkillContext origin) {
        add(skill.getId(), origin);
    }

    PlayerSkillContext get(String id, ClassDefinition classDefinition);

    void remove(ISkill skill, ClassDefinition origin);

    Map<String, PlayerSkillContext> getSkills();

    void clear();

    boolean contains(String name);
}
