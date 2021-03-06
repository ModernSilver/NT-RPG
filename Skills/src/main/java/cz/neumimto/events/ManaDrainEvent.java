package cz.neumimto.events;

import cz.neumimto.rpg.entities.IEntity;
import cz.neumimto.rpg.events.AbstractCancellableNEvent;
import cz.neumimto.rpg.players.IActiveCharacter;
import cz.neumimto.rpg.scripting.JsBinding;

/**
 * Created by NeumimTo on 7.7.2017.
 */
@JsBinding(JsBinding.Type.CLASS)
public class ManaDrainEvent extends AbstractCancellableNEvent {

	private final IEntity source;
	private final IActiveCharacter target;
	private double amountDrained;

	public ManaDrainEvent(IEntity source, IActiveCharacter target, double k) {
		this.source = source;
		this.target = target;
		this.amountDrained = k;
	}

	@Override
	public IEntity getSource() {
		return source;
	}

	public IActiveCharacter getTarget() {
		return target;
	}

	public double getAmountDrained() {
		return amountDrained;
	}

	public void setAmountDrained(double amountDrained) {
		this.amountDrained = amountDrained;
	}
}
