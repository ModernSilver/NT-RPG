/*
 *     Copyright (c) 2015, NeumimTo https://github.com/NeumimTo
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package cz.neumimto.rpg.effects.common.mechanics;

import cz.neumimto.rpg.NtRpgPlugin;
import cz.neumimto.rpg.api.effects.EffectBase;
import cz.neumimto.rpg.api.effects.Generate;
import cz.neumimto.rpg.api.effects.IEffect;
import cz.neumimto.rpg.effects.CoreEffectTypes;
import cz.neumimto.rpg.effects.EffectStatusType;
import cz.neumimto.rpg.effects.IEffectConsumer;
import cz.neumimto.rpg.events.character.CharacterManaRegainEvent;
import cz.neumimto.rpg.gui.Gui;
import cz.neumimto.rpg.players.IActiveCharacter;
import cz.neumimto.rpg.properties.DefaultProperties;
import org.spongepowered.api.Sponge;

/**
 * Created by NeumimTo on 9.8.2015.
 */
@Generate(id = "name", description = "A component which enables mana regeneration")
public class DefaultManaRegeneration extends EffectBase {

	public static final String name = "DefaultManaRegen";
	private static final String apply = "You've gained mana reneneration.";
	private static final String remove = "You've lost mana regenartion.";
	private IActiveCharacter character;

	public DefaultManaRegeneration(IEffectConsumer character, long duration, Void value) {
		super(name, character);
		this.character = (IActiveCharacter) character;
		setPeriod(NtRpgPlugin.pluginConfig.MANA_REGENERATION_RATE);
		setApplyMessage(apply);
		setExpireMessage(remove);
		setDuration(-1);
		effectTypes.add(CoreEffectTypes.MANA_REGEN);
	}

	@Override
	public void onApply(IEffect self) {
		Gui.sendEffectStatus(character, EffectStatusType.APPLIED, this);
	}

	@Override
	public void onRemove(IEffect self) {
		Gui.sendEffectStatus(character, EffectStatusType.EXPIRED, this);
	}

	@Override
	public void onTick(IEffect self) {
		double current = character.getMana().getValue();
		double max = character.getMana().getMaxValue();
		if (current >= max) {
			return;
		}
		double regen = character.getMana().getRegen()
				* NtRpgPlugin.GlobalScope.entityService.getEntityProperty(character, DefaultProperties.mana_regen_mult);

		CharacterManaRegainEvent event = new CharacterManaRegainEvent(character, regen, this);
		if (Sponge.getEventManager().post(event)) return;

		current += event.getAmount();
		if (current > max) current = max;

		event.getTarget().getMana().setValue(current);
		Gui.displayMana(character);
	}

	@Override
	public boolean isStackable() {
		return false;
	}

	@Override
	public boolean requiresRegister() {
		return true;
	}

}
