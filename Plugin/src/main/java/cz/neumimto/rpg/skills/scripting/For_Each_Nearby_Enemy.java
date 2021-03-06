package cz.neumimto.rpg.skills.scripting;

import cz.neumimto.rpg.NtRpgPlugin;
import cz.neumimto.rpg.entities.IEntity;
import cz.neumimto.rpg.players.IActiveCharacter;
import cz.neumimto.rpg.scripting.JsBinding;
import cz.neumimto.rpg.skills.pipeline.SkillComponent;
import cz.neumimto.rpg.utils.TriConsumer;
import cz.neumimto.rpg.utils.Utils;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Living;

import java.util.Collection;
import java.util.function.Consumer;

//This is a workaround for some bugs i necountered while working in nashorn Nashorn
@JsBinding(JsBinding.Type.OBJECT)
@SkillComponent(
		value = "Do action for every nearby enemy",
		usage = "for_each_nearby_enemy(entity, radius, new Consumer() { apply: function(entity} { .. })",
		params = {
				@SkillComponent.Param("entity - An entity which we search for its enemies"),
				@SkillComponent.Param("range - Maximal search range"),
				@SkillComponent.Param("consumer - callback"),
		}
)
public class For_Each_Nearby_Enemy implements TriConsumer<IEntity, Number, Consumer<IEntity>> {

	@Override
	public void accept(IEntity entity, Number radius, Consumer<IEntity> consumer) {
		Collection<Entity> nearbyEntities = entity.getEntity().getNearbyEntities(radius.doubleValue());
		IActiveCharacter character = (IActiveCharacter) entity;
		for (Entity nearbyEntity : nearbyEntities) {
			if (nearbyEntity instanceof Living) {
				Living living = (Living) nearbyEntity;
				IEntity iEntity = NtRpgPlugin.GlobalScope.entityService.get(nearbyEntity);
				if (!iEntity.isFriendlyTo(character) && Utils.canDamage(character, living)) {
					consumer.accept(iEntity);
				}
			}
		}
	}
}