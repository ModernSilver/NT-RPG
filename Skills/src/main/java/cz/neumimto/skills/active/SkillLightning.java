package cz.neumimto.skills.active;

import cz.neumimto.Decorator;
import cz.neumimto.rpg.ResourceLoader;
import cz.neumimto.rpg.damage.SkillDamageSource;
import cz.neumimto.rpg.damage.SkillDamageSourceBuilder;
import cz.neumimto.rpg.entities.IEntity;
import cz.neumimto.rpg.players.IActiveCharacter;
import cz.neumimto.rpg.skills.NDamageType;
import cz.neumimto.rpg.skills.PlayerSkillContext;
import cz.neumimto.rpg.skills.SkillNodes;
import cz.neumimto.rpg.skills.SkillResult;
import cz.neumimto.rpg.skills.mods.SkillContext;
import cz.neumimto.rpg.skills.parents.Targeted;
import cz.neumimto.rpg.skills.tree.SkillType;

import javax.inject.Singleton;

/**
 * Created by NeumimTo on 29.12.2015.
 */
@Singleton
@ResourceLoader.Skill("ntrpg:lightning")
public class SkillLightning extends Targeted {

	@Override
	public void init() {
		super.init();
		setDamageType(NDamageType.LIGHTNING);
		settings.addNode(SkillNodes.DAMAGE, 10, 20);
		settings.addNode(SkillNodes.RANGE, 10, 10);
		addSkillType(SkillType.ELEMENTAL);
		addSkillType(SkillType.LIGHTNING);
	}

	@Override
	public void castOn(IEntity target, IActiveCharacter source, PlayerSkillContext info, SkillContext skillContext) {
		float damage = skillContext.getFloatNodeValue(SkillNodes.DAMAGE);
		SkillDamageSource s = new SkillDamageSourceBuilder()
				.fromSkill(this)
				.setSource(source)
				.build();
		target.getEntity().damage(damage, s);
		Decorator.strikeLightning(target.getEntity());
		skillContext.next(source, info, skillContext.result(SkillResult.OK));
	}
}
