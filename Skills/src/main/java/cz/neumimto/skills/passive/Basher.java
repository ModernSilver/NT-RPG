package cz.neumimto.skills.passive;

import cz.neumimto.effects.positive.Bash;
import cz.neumimto.model.BashModel;
import cz.neumimto.rpg.ResourceLoader;
import cz.neumimto.rpg.common.effects.EffectService;
import cz.neumimto.rpg.entities.EntityService;
import cz.neumimto.rpg.players.IActiveCharacter;
import cz.neumimto.rpg.skills.PlayerSkillContext;
import cz.neumimto.rpg.skills.SkillNodes;
import cz.neumimto.rpg.skills.parents.PassiveSkill;
import cz.neumimto.rpg.skills.tree.SkillType;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by NeumimTo on 4.7.2017.
 */
@Singleton
@ResourceLoader.Skill("ntrpg:basher")
public class Basher extends PassiveSkill {

	@Inject
	private EffectService effectService;

	@Inject
	private EntityService entityService;

	public Basher() {
		super(Bash.name);
		settings.addNode(SkillNodes.DAMAGE, 10, 10);
		settings.addNode(SkillNodes.CHANCE, 0.1f, 0.005f);
		settings.addNode(SkillNodes.PERIOD, 2500, -100);
		settings.addNode(SkillNodes.DURATION, 1000, 50f);
		setDamageType(DamageTypes.ATTACK);
		addSkillType(SkillType.PHYSICAL);
	}

	@Override
	public void applyEffect(PlayerSkillContext info, IActiveCharacter character) {
		BashModel model = getBashModel(info, character);
		effectService.addEffect(new Bash(character, -1, model), this);
	}

	@Override
	public void skillUpgrade(IActiveCharacter IActiveCharacter, int level) {
		super.skillUpgrade(IActiveCharacter, level);
		PlayerSkillContext info = IActiveCharacter.getSkill(getId());
		BashModel model = getBashModel(info, IActiveCharacter);
		effectService.removeEffect(Bash.name, IActiveCharacter, this);
		effectService.addEffect(new Bash(IActiveCharacter, -1, model), this);
	}

	private BashModel getBashModel(PlayerSkillContext info, IActiveCharacter character) {
		BashModel model = new BashModel();
		int level = info.getTotalLevel();
		model.chance = (int) info.getSkillData().getSkillSettings().getLevelNodeValue(SkillNodes.CHANCE, level);
		model.cooldown = (long) info.getSkillData().getSkillSettings().getLevelNodeValue(SkillNodes.COOLDOWN, level);
		model.damage = info.getSkillData().getSkillSettings().getLevelNodeValue(SkillNodes.DAMAGE, level);
		model.stunDuration = (long) info.getSkillData().getSkillSettings().getLevelNodeValue(SkillNodes.DURATION, level);
		return model;
	}
}
