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

package cz.neumimto.rpg.persistance;

import com.typesafe.config.*;
import cz.neumimto.core.localization.TextHelper;
import cz.neumimto.rpg.NtRpgPlugin;
import cz.neumimto.rpg.Pair;
import cz.neumimto.rpg.ResourceLoader;
import cz.neumimto.rpg.Rpg;
import cz.neumimto.rpg.gui.SkillTreeInterfaceModel;
import cz.neumimto.rpg.players.attributes.Attribute;
import cz.neumimto.rpg.skills.*;
import cz.neumimto.rpg.skills.configs.SkillConfigLoader;
import cz.neumimto.rpg.skills.mods.ActiveSkillPreProcessorWrapper;
import cz.neumimto.rpg.skills.mods.SkillPreProcessorFactory;
import cz.neumimto.rpg.skills.parents.StartingPoint;
import cz.neumimto.rpg.skills.tree.SkillTree;
import cz.neumimto.rpg.skills.utils.SkillLoadingErrors;
import cz.neumimto.rpg.utils.Utils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.ItemType;

import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.*;
import java.util.stream.Collectors;

import static cz.neumimto.rpg.api.logging.Log.*;

/**
 * Created by NeumimTo on 24.7.2015.
 */
@Singleton
public class SkillTreeDao {

	public Map<String, SkillTree> getAll() {
		Path dir = ResourceLoader.skilltreeDir.toPath();
		Map<String, SkillTree> map = new HashMap<>();
		try (DirectoryStream<Path> paths = Files.newDirectoryStream(dir, "*.conf")) {
			paths.forEach(path -> {
				info("Loading skilltree from a file " + path.getFileName());
				Config config = ConfigFactory.parseFile(path.toFile());
				SkillTree skillTree = new SkillTree();
				try {
					skillTree.setDescription(config.getString("Description"));
				} catch (ConfigException e) {
					skillTree.setDescription("");
					warn("Missing \"Description\" node");
				}
				try {
					skillTree.setId(config.getString("Name"));
				} catch (ConfigException e) {
					warn("Missing \"Name\" skipping to another file");
					return;
				}
				skillTree.getSkills().put(StartingPoint.name.toPlain(), StartingPoint.SKILL_DATA);
				try {
					List<? extends ConfigObject> skills = config.getObjectList("Skills");
					createConfigSkills(skills, skillTree);
					loadSkills(skills, skillTree);
				} catch (ConfigException e) {
					warn("Missing \"Skills\" section. No skills defined");

				}

				try {
					List<String> asciiMap = config.getStringList("AsciiMap");
					Optional<String> max = asciiMap.stream().max(Comparator.comparingInt(String::length));
					if (max.isPresent()) {
						int length = max.get().length();
						int rows = asciiMap.size();

						short[][] array = new short[rows][length];

						int i = 0;
						int j = 0;
						StringBuilder num = new StringBuilder();
						for (String s : asciiMap) {
							for (char c1 : s.toCharArray()) {
								if (Character.isDigit(c1)) {
									num.append(c1);
									continue;
								} else if (c1 == 'X') {
									skillTree.setCenter(new Pair<>(i, j));
									j++;
									continue;
								}
								if (!num.toString().equals("")) {
									array[i][j] = Short.parseShort(num.toString());
									j++;
								}
								SkillTreeInterfaceModel guiModelByCharacter = NtRpgPlugin.GlobalScope.skillService.getGuiModelByCharacter(c1);
								if (guiModelByCharacter != null) {
									array[i][j] = guiModelByCharacter.getId();
								}
								num = new StringBuilder();
								j++;
							}
							j = 0;
							i++;
						}
						skillTree.setSkillTreeMap(array);
					}
				} catch (ConfigException | ArrayIndexOutOfBoundsException ignored) {
					error("Could not read ascii map in the skilltree " + skillTree.getId(), ignored);
					skillTree.setSkillTreeMap(new short[][]{});
				}
				map.put(skillTree.getId(), skillTree);
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	}

	private void createConfigSkills(List<? extends ConfigObject> sub, SkillTree skillTree) {
		for (ConfigObject co : sub) {
			Config c = co.toConfig();
			String id = c.getString("SkillId");
			Optional<ISkill> byId = NtRpgPlugin.GlobalScope.skillService.getById(id);
			if (!byId.isPresent()) {

				ISkill skill = null;
				try {
					String type = c.getString("Type");
					SkillConfigLoader type1 = Sponge.getRegistry().getType(SkillConfigLoader.class, type)
							.orElseThrow(() -> new IllegalArgumentException("Unknown skill type " + type + " in a skiltree " + skillTree.getId()));

					skill = type1.build(id.toLowerCase());

				} catch (ConfigException.Missing ignored) {
				}

				try {
					List<String> description = c.getStringList("Description");
					skill.setDescription(description.stream().map(TextHelper::parse).collect(Collectors.toList()));
				} catch (ConfigException.Missing ignored) {}

				try {
					List<String> lore = c.getStringList("Lore");
					skill.setDescription(lore.stream().map(TextHelper::parse).collect(Collectors.toList()));
				} catch (ConfigException.Missing ignored) {}

			} else {
			}
		}
	}

	private void loadSkills(List<? extends ConfigObject> sub, SkillTree skillTree) {
		for (ConfigObject co : sub) {

			Config c = co.toConfig();
			SkillData info = getSkillInfo(c.getString("SkillId"), skillTree);

			try {
				info.setMaxSkillLevel(c.getInt("MaxSkillLevel"));
			} catch (ConfigException e) {
				info.setMaxSkillLevel(1);
				warn("Missing \"MaxSkillLevel\" node for a skill \"" + info.getSkillId() + "\", setting to 1");
			}
			try {
				String combination = c.getString("Combination");
				combination = combination.trim();
				if (!"".equals(combination)) {
					info.setCombination(combination);
				}
			} catch (ConfigException e) {
			}

			try {
				info.setMinPlayerLevel(c.getInt("MinPlayerLevel"));
			} catch (ConfigException e) {
				info.setMinPlayerLevel(1);
				warn("Missing \"MinPlayerLevel\" node for a skill \"" + info.getSkillId() + "\", setting to 1");
			}

			try {
				info.setLevelGap(c.getInt("LevelGap"));
			} catch (ConfigException e) {
				info.setLevelGap(0);
				warn("Missing \"LevelGap\" node for a skill \"" + info.getSkillId() + "\", setting to 1");
			}


			try {
				Config reagent = c.getConfig("InvokeCost");
				SkillCost itemCost = new SkillCost();
				info.setInvokeCost(itemCost);
				List<? extends ConfigObject> list = reagent.getObjectList("Items");

				for (ConfigObject configObject : list) {
					try {
						SkillItemCost q = new SkillItemCost();
						q.setAmount(Integer.parseInt(configObject.get("Amount").unwrapped().toString()));
						String type = configObject.get("ItemType").unwrapped().toString();
						boolean consume = Boolean.valueOf(configObject.get("Consume").unwrapped().toString());
						q.setConsumeItems(consume);
						Optional<ItemType> type1 = Sponge.getRegistry().getType(ItemType.class, type);
						if (type1.isPresent()) {
							q.setItemType(type1.get());
							itemCost.getItemCost().add(q);
						} else {
							warn(" - Unknown ItemType " + type + " Defined in Invoke-Cost section for a skill " + info.getSkillId());
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				list = reagent.getObjectList("Insufficient");
				for (ConfigObject configObject : list) {
					String preprocessorFactoryId = configObject.get("Id").unwrapped().toString();
					Optional<SkillPreProcessorFactory> id = Rpg.get().getSkillPreProcessorFactory(preprocessorFactoryId);
					if (id.isPresent()) {
						SkillPreProcessorFactory skillPreProcessorFactory = id.get();
						ActiveSkillPreProcessorWrapper parse = skillPreProcessorFactory.parse(configObject);
						itemCost.getInsufficientProcessors().add(parse);
					} else {
						warn("- Unknown processor type " + configObject.get("Id").render() + ", use one of: " + Sponge.getRegistry()
								.getAllOf(SkillPreProcessorFactory.class)
								.stream().map(SkillPreProcessorFactory::getId).collect(Collectors.joining(", ")));
					}
				}
			} catch (Exception e) {

			}

			try {
				for (String conflicts : c.getStringList("Conflicts")) {
					info.getConflicts().add(getSkillInfo(conflicts, skillTree));
				}
			} catch (ConfigException ignored) {
			}

			try {
				Config softDepends = c.getConfig("SoftDepends");
				for (Map.Entry<String, ConfigValue> entry : softDepends.entrySet()) {
					String skillId = entry.getKey().replaceAll("\"", "");
					String render = entry.getValue().render();
					int i = Integer.parseInt(render);
					SkillData skill = getSkillInfo(skillId, skillTree);
					info.getSoftDepends().add(new SkillDependency(skill, i));
					skill.getDepending().add(info);
				}
			} catch (ConfigException ignored) {
			}
			try {
				Config softDepends = c.getConfig("HardDepends");
				for (Map.Entry<String, ConfigValue> entry : softDepends.entrySet()) {
					String skillId = entry.getKey();
					String render = entry.getValue().render();
					int i = Integer.parseInt(render);
					SkillData skill = getSkillInfo(skillId, skillTree);
					info.getHardDepends().add(new SkillDependency(skill, i));
					skill.getDepending().add(info);
				}
			} catch (ConfigException ignored) {
			}

			try {
				info.setSkillTreeId(c.getInt("SkillTreeId"));
			} catch (ConfigException ignored) {
				info(" - Skill " + info.getSkillId() + " missing SkillTreeId, it wont be possible to reference this skill in the ascii map");
			}

			try {
				info.setSkillName(TextHelper.parse(c.getString("Name")));
				info(" - Alternate name defined for skill " + info.getSkill().getId() + " > " + info.getSkillName().toPlain());
				NtRpgPlugin.GlobalScope.skillService.registerSkillAlternateName(info.getSkillName().toPlain(), info.getSkill());
			} catch (ConfigException missing) {
				info.setSkillName(info.getSkill().getLocalizableName());
			}

			SkillSettings skillSettings = new SkillSettings();
			try {
				Config settings = c.getConfig("SkillSettings");
				Collection<Attribute> attributes = Rpg.get().getAttributes();
				outer: for (Map.Entry<String, ConfigValue> e : settings.entrySet()) {
					if (e.getKey().endsWith(SkillSettings.bonus)) {
						continue;
					}
					String val = e.getValue().render();
					if (Utils.isNumeric(val)) {
						float norm = Float.parseFloat(val);
						for (Attribute attribute : attributes) {
							String s = "_per_" + attribute.getId();
							if (e.getKey().endsWith(s)) {
								String stripped = s.substring(0, val.length() - s.length());
								skillSettings.addAttributeNode(stripped, attribute, norm);
								continue outer;
							}
						}


						String name = e.getKey();
						skillSettings.addNode(name, norm);
						name = name + SkillSettings.bonus;
						float bonus = 0f;
						try {
							bonus = Float.parseFloat(settings.getString(name));
						} catch (ConfigException ignored) {
						}
						skillSettings.addNode(name, bonus);
					} else {
						skillSettings.addObjectNode(e.getKey(), val);
					}
				}
				addRequiredIfMissing(skillSettings);
			} catch (ConfigException ignored) {
				warn(" - missing SkillSettings section " + info.getSkillId());
			}
			info.setSkillSettings(skillSettings);

			SkillSettings defaultSkillSettings = info.getSkill().getDefaultSkillSettings();
			if (defaultSkillSettings != null && defaultSkillSettings.getNodes() != null) {
				Iterator<Map.Entry<String, Float>> iterator = defaultSkillSettings.getNodes().entrySet().iterator();
				while (iterator.hasNext()) {
					Map.Entry<String, Float> next = iterator.next();
					Float value = next.getValue();
					String key = next.getKey();
					if (key.endsWith(SkillSettings.bonus)) {
						continue;
					}
					if (!skillSettings.getNodes().containsKey(key)) {
						Float val2 = defaultSkillSettings.getNodes().get(key + SkillSettings.bonus);
						skillSettings.addNode(key, value, val2);
						warn(" - Missing settings node " + key + " for a skill " + info.getSkillId() + " - inherited from default: " + value + " / " + val2);
					}
				}
			}


			SkillLoadingErrors errors = new SkillLoadingErrors(skillTree.getId());
			try {
				info.getSkill().loadSkillData(info, skillTree, errors, c);
			} catch (ConfigException e) {

			}
			for (String s : errors.getErrors()) {
				info(s);
			}


			skillTree.getSkills().put(info.getSkillId().toLowerCase(), info);


		}
	}

	private void addRequiredIfMissing(SkillSettings skillSettings) {
		Map.Entry<String, Float> q = skillSettings.getFloatNodeEntry(SkillNodes.HPCOST.name());
		if (q == null) {
			skillSettings.addNode(SkillNodes.HPCOST, 0, 0);
		}
		q = skillSettings.getFloatNodeEntry(SkillNodes.MANACOST.name());
		if (q == null) {
			skillSettings.addNode(SkillNodes.MANACOST, 0, 0);
		}
		q = skillSettings.getFloatNodeEntry(SkillNodes.COOLDOWN.name());
		if (q == null) {
			skillSettings.addNode(SkillNodes.COOLDOWN, 0, 0);
		}
	}

	private SkillData getSkillInfo(String id, SkillTree tree) {
		final String lowercased = id.toLowerCase();
		SkillData info = tree.getSkills().get(lowercased);
		if (info == null) {
			ISkill skill = NtRpgPlugin.GlobalScope.skillService.getById(lowercased)
					.orElseThrow(
							() -> new IllegalStateException("Could not find a skill " + lowercased + " referenced in the skilltree " + tree.getId
									()));

			info = skill.constructSkillData();
			info.setSkill(skill);
			tree.getSkills().put(lowercased, info);
		}
		return info;
	}
}
