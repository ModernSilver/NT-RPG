package cz.neumimto.effects.negative;

import com.flowpowered.math.vector.Vector3d;
import cz.neumimto.rpg.api.effects.Generate;
import cz.neumimto.rpg.api.effects.IEffect;
import cz.neumimto.rpg.effects.CommonEffectTypes;
import cz.neumimto.rpg.effects.IEffectConsumer;
import cz.neumimto.rpg.effects.ShapedEffectDecorator;
import cz.neumimto.rpg.gui.ParticleDecorator;
import cz.neumimto.rpg.scripting.JsBinding;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.Extent;

/**
 * Created by NeumimTo on 5.6.17.
 */
@JsBinding(JsBinding.Type.CLASS)
@Generate(id = "name", description = "Stuns the target, Stunned entities may not move, nor use skill")
public class StunEffect extends ShapedEffectDecorator<Location<World>> {

	public static final String name = "Stun";
	private static final Vector3d vec3d = new Vector3d(0, 2, 0);
	private static final long tickRate = 50L;

	private static ParticleEffect particleEffect = ParticleEffect.builder()
			.quantity(8)
			.type(ParticleTypes.CRITICAL_HIT)
			.build();

	public StunEffect(IEffectConsumer consumer, long duration) {
		super(name, consumer);
		setValue(consumer.getEntity().getLocation());
		setDuration(duration);
		setPeriod(10);
		setPrinterCount(1);
		addEffectType(CommonEffectTypes.SILENCE);
		addEffectType(CommonEffectTypes.STUN);
		setStackable(false, null);
	}


	@Override
	public void onTick(IEffect self) {
		if (getLastTickTime() <= System.currentTimeMillis() - tickRate) {
			getConsumer().getEntity().setLocation(getValue());
		}
	}

	@Override
	public void draw(Vector3d vec) {
		Location<Extent> add = getConsumer().getLocation().add(vec).add(vec3d);
		World extent = getConsumer().getEntity().getLocation().getExtent();
		extent.spawnParticles(particleEffect, add.getPosition());

	}

	@Override
	public Vector3d[] getVertices() {
		return ParticleDecorator.tinyCircle;
	}

}
