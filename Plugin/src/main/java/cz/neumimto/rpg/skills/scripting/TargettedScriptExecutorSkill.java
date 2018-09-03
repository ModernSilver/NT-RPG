package cz.neumimto.rpg.skills.scripting;

import cz.neumimto.rpg.IEntity;
import cz.neumimto.rpg.players.IActiveCharacter;
import cz.neumimto.rpg.scripting.JsBinding;
import cz.neumimto.rpg.skills.ExtendedSkillInfo;
import cz.neumimto.rpg.skills.SkillResult;
import cz.neumimto.rpg.skills.utils.SkillModifier;

/**
 * Created by NeumimTo on 3.9.2018.
 */
@FunctionalInterface
@JsBinding(JsBinding.Type.CLASS)
public interface TargettedScriptExecutorSkill {

    SkillResult cast(IActiveCharacter character,IEntity target, ExtendedSkillInfo info, SkillModifier modifier, SkillScriptContext context);
}
