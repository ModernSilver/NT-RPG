package cz.neumimto.events;

import cz.neumimto.rpg.entities.IEntity;
import cz.neumimto.rpg.events.AbstractCancellableNEvent;
import cz.neumimto.rpg.scripting.JsBinding;

/**
 * Created by NeumimTo on 6.7.2017.
 */
@JsBinding(JsBinding.Type.CLASS)
public class CriticalStrikeEvent extends AbstractCancellableNEvent {

	private final IEntity source;
	private final IEntity target;
	private final double damage;

	public CriticalStrikeEvent(IEntity source, IEntity target, double effect) {
		this.source = source;
		this.target = target;
		this.damage = effect;
	}

	@Override
	public IEntity getSource() {
		return source;
	}

	public IEntity getTarget() {
		return target;
	}

	public double getDamage() {
		return damage;
	}
}
