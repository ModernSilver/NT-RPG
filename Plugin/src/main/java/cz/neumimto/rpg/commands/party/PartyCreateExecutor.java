package cz.neumimto.rpg.commands.party;

import cz.neumimto.core.localization.Arg;
import cz.neumimto.rpg.NtRpgPlugin;
import cz.neumimto.rpg.configuration.Localizations;
import cz.neumimto.rpg.gui.Gui;
import cz.neumimto.rpg.players.IActiveCharacter;
import cz.neumimto.rpg.players.parties.Party;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;

public class PartyCreateExecutor implements CommandExecutor {
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		IActiveCharacter character = NtRpgPlugin.GlobalScope.characterService.getCharacter((Player) src);
		if (character.isStub()) {
			Gui.sendMessage(character, Localizations.CHARACTER_IS_REQUIRED, Arg.EMPTY);
			return CommandResult.success();
		}
		if (character.hasParty()) {
			Gui.sendMessage(character, Localizations.ALREADY_IN_PARTY, Arg.EMPTY);
			return CommandResult.success();
		}
		Party party = new Party(character);
		character.setParty(party);
		Gui.sendMessage(character, Localizations.PARTY_CREATED, Arg.EMPTY);
		return CommandResult.success();
	}
}
