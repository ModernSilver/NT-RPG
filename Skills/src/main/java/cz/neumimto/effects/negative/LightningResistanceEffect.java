package cz.neumimto.effects.negative;

import cz.neumimto.effects.SingleResistanceValueEffect;
import cz.neumimto.rpg.api.effects.Generate;
import cz.neumimto.rpg.effects.IEffectConsumer;
import cz.neumimto.rpg.properties.DefaultProperties;
import cz.neumimto.rpg.scripting.JsBinding;

/**
 * Created by NeumimTo on 1.8.2017.
 */
@JsBinding(JsBinding.Type.CLASS)
@Generate(id = "name", description = "Increases resistance against lightning damage")
public class LightningResistanceEffect extends SingleResistanceValueEffect {

	public static final String name = "Lightning resistance";

	public LightningResistanceEffect(IEffectConsumer consumer, long duration, float value) {
		super(name, consumer, DefaultProperties.lightning_damage_protection_mult, value);
		setDuration(duration);
	}
}
