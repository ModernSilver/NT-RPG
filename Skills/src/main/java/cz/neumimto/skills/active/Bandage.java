package cz.neumimto.skills.active;

import cz.neumimto.Decorator;
import cz.neumimto.effects.negative.Bleeding;
import cz.neumimto.rpg.ResourceLoader;
import cz.neumimto.rpg.common.effects.EffectService;
import cz.neumimto.rpg.entities.EntityService;
import cz.neumimto.rpg.entities.IEntity;
import cz.neumimto.rpg.players.IActiveCharacter;
import cz.neumimto.rpg.skills.PlayerSkillContext;
import cz.neumimto.rpg.skills.SkillNodes;
import cz.neumimto.rpg.skills.SkillResult;
import cz.neumimto.rpg.skills.mods.SkillContext;
import cz.neumimto.rpg.skills.parents.Targeted;
import cz.neumimto.rpg.skills.tree.SkillType;
import org.spongepowered.api.item.ItemTypes;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by NeumimTo on 5.8.2017.
 */
@Singleton
@ResourceLoader.Skill("ntrpg:bandage")
public class Bandage extends Targeted {

	@Inject
	private EntityService entityService;

	@Inject
	private EffectService effectService;

	@Override
	public void init() {
		super.init();
		settings.addNode(SkillNodes.HEALED_AMOUNT, 15, 5);
		setIcon(ItemTypes.PAPER);
		addSkillType(SkillType.HEALING);
		addSkillType(SkillType.PHYSICAL);
	}

	@Override
	public void castOn(IEntity target, IActiveCharacter source, PlayerSkillContext info, SkillContext skillContext) {
		if (target.isFriendlyTo(source)) {
			float floatNodeValue = skillContext.getFloatNodeValue(SkillNodes.HEALED_AMOUNT);
			entityService.healEntity(target, floatNodeValue, this);
			Decorator.healEffect(target.getEntity().getLocation().add(0, 1, 0));
			if (target.hasEffect(Bleeding.name)) {
				effectService.removeEffectContainer(target.getEffect(Bleeding.name), target);
			}
			skillContext.next(source, info, SkillResult.OK);
			return;
		}
		skillContext.next(source, info, SkillResult.CANCELLED);
	}
}
