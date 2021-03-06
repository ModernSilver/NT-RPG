package cz.neumimto.rpg.commands.elements;

import cz.neumimto.rpg.players.attributes.Attribute;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.PatternMatchingCommandElement;
import org.spongepowered.api.text.Text;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

/**
 * Created by NeumimTo on 16.11.2017.
 */
public class CharacterAttributeCommandElement extends PatternMatchingCommandElement {

	public CharacterAttributeCommandElement(@Nullable Text key) {
		super(key);
	}

	@Override
	protected Iterable<String> getChoices(CommandSource source) {
		Collection<Attribute> allOf = Sponge.getRegistry().getAllOf(Attribute.class);
		return allOf.stream().map(Attribute::getId).collect(Collectors.toSet());
	}

	@Override
	protected Object getValue(String choice) {
		return Sponge.getRegistry().getType(Attribute.class, choice).orElse(null);
	}

}
