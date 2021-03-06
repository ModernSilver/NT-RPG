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

package cz.neumimto.rpg;

import com.google.inject.Injector;
import cz.neumimto.rpg.commands.CommandService;
import cz.neumimto.rpg.common.effects.EffectService;
import cz.neumimto.rpg.damage.SpongeDamageService;
import cz.neumimto.rpg.entities.EntityService;
import cz.neumimto.rpg.gui.Gui;
import cz.neumimto.rpg.gui.ParticleDecorator;
import cz.neumimto.rpg.gui.VanillaMessaging;
import cz.neumimto.rpg.inventory.SpongeInventoryService;
import cz.neumimto.rpg.inventory.SpongeItemService;
import cz.neumimto.rpg.inventory.runewords.RWService;
import cz.neumimto.rpg.players.CharacterService;
import cz.neumimto.rpg.players.parties.PartyService;
import cz.neumimto.rpg.properties.SpongePropertyService;
import cz.neumimto.rpg.scripting.JSLoader;
import cz.neumimto.rpg.skills.SkillService;
import org.spongepowered.api.Game;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by NeumimTo on 6.8.2015.
 */
@Singleton
public class GlobalScope {

	@Inject
	public CharacterService characterService;

	@Inject
	public EffectService effectService;

	@Inject
	public ClassService classService;

	@Inject
	public CommandService commandService;

	@Inject
	public SkillService skillService;

	@Inject
	public NtRpgPlugin plugin;

	@Inject
	public Game game;

	@Inject
	public SpongeDamageService damageService;

	@Inject
	public SpongeInventoryService inventorySerivce;

	@Inject
	public RWService runewordService;

	@Inject
	public EntityService entityService;

	@Inject
	public PartyService partyService;

	@Inject
	public SpongePropertyService spongePropertyService;

	@Inject
	public SpongeItemService itemService;

	@Inject
	public Injector injector;

	@Inject
	public JSLoader jsLoader;

	@Inject
	public ResourceLoader resourceLoader;

	@Inject
	public EffectService experienceService;

	@Inject
	public SpongeInventoryService spongeInventoryService;

	@Inject
	public VanillaMessaging vanillaMessaging;

	@Inject
	public ParticleDecorator particleDecorator;

	@Inject
	public RWService rwService;

	@Inject
	public Gui gui;
}
