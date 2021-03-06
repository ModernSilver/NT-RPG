package cz.neumimto.skills.active;

import cz.neumimto.effects.negative.StunEffect;
import cz.neumimto.rpg.ResourceLoader;
import cz.neumimto.rpg.common.effects.EffectService;
import cz.neumimto.rpg.damage.SkillDamageSource;
import cz.neumimto.rpg.damage.SkillDamageSourceBuilder;
import cz.neumimto.rpg.entities.IEntity;
import cz.neumimto.rpg.players.IActiveCharacter;
import cz.neumimto.rpg.skills.PlayerSkillContext;
import cz.neumimto.rpg.skills.SkillNodes;
import cz.neumimto.rpg.skills.SkillResult;
import cz.neumimto.rpg.skills.mods.SkillContext;
import cz.neumimto.rpg.skills.parents.Targeted;
import cz.neumimto.rpg.skills.tree.SkillType;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by ja on 20.8.2017.
 */
@Singleton
@ResourceLoader.Skill("ntrpg:stun")
public class Stun extends Targeted {

	@Inject
	private EffectService effectService;

	@Override
	public void init() {
		super.init();
		settings.addNode(SkillNodes.DAMAGE, 10, 1);
		settings.addNode(SkillNodes.DURATION, 4500, 100);
		addSkillType(SkillType.PHYSICAL);
		addSkillType(SkillType.MOVEMENT);
		setDamageType(DamageTypes.ATTACK);
	}

	@Override
	public void castOn(IEntity target, IActiveCharacter source, PlayerSkillContext info, SkillContext skillContext) {
		long duration = skillContext.getLongNodeValue(SkillNodes.DURATION);
		double damage = skillContext.getDoubleNodeValue(SkillNodes.DAMAGE);
		StunEffect stunEffect = new StunEffect(target, duration);
		effectService.addEffect(stunEffect, this, source);
		if (damage > 0) {
			SkillDamageSource s = new SkillDamageSourceBuilder()
					.fromSkill(this)
					.setSource(source)
					.build();
			target.getEntity().damage(damage, s);
		}
		skillContext.next(source, info, skillContext.result(SkillResult.OK));
	}

}
