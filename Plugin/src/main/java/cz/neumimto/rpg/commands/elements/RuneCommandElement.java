package cz.neumimto.rpg.commands.elements;

import cz.neumimto.rpg.NtRpgPlugin;
import cz.neumimto.rpg.inventory.runewords.ItemUpgrade;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Created by NeumimTo on 5.11.2017.
 */
public class RuneCommandElement extends CommandElement {

	public RuneCommandElement(@Nullable Text key) {
		super(key);
	}

	@Override
	protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
		String skilllc = args.next();
		ItemUpgrade rune = NtRpgPlugin.GlobalScope.runewordService.getRune(skilllc);
		if (rune == null) {
			throw args.createError(TextSerializers.FORMATTING_CODE.deserialize("&CUnknown rune &C\"" + skilllc + "\""));
		}
		return rune;
	}

	@Override
	public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
		return new ArrayList<>(NtRpgPlugin.GlobalScope.runewordService.getRunes().keySet());
	}

	@Override
	public Text getUsage(CommandSource src) {
		return Text.of("<rune>");
	}

}