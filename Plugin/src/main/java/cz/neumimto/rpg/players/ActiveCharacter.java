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

package cz.neumimto.rpg.players;

import cz.neumimto.core.localization.Arg;
import cz.neumimto.core.localization.LocalizableParametrizedText;
import cz.neumimto.rpg.effects.EffectSourceType;
import cz.neumimto.rpg.effects.IEffect;
import cz.neumimto.rpg.effects.IEffectContainer;
import cz.neumimto.rpg.inventory.ConfigRPGItemType;
import cz.neumimto.rpg.inventory.RPGItemType;
import cz.neumimto.rpg.inventory.items.types.CustomItem;
import cz.neumimto.rpg.persistance.model.CharacterClass;
import cz.neumimto.rpg.persistance.model.EquipedSlot;
import cz.neumimto.rpg.players.groups.ClassDefinition;
import cz.neumimto.rpg.players.groups.String;
import cz.neumimto.rpg.players.groups.ConfigClass;
import cz.neumimto.rpg.players.parties.Party;
import cz.neumimto.rpg.players.properties.PropertyService;
import cz.neumimto.rpg.skills.ExtendedSkillInfo;
import cz.neumimto.rpg.skills.ISkill;
import cz.neumimto.rpg.skills.ItemAccessSkill;
import cz.neumimto.rpg.skills.SkillData;
import cz.neumimto.rpg.skills.parents.StartingPoint;
import cz.neumimto.rpg.skills.tree.SkillTreeSpecialization;
import org.jline.utils.Log;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatType;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.stream.Collectors;

import static cz.neumimto.rpg.NtRpgPlugin.pluginConfig;


/**
 * Created by NeumimTo on 26.12.2014.
 */

public class ActiveCharacter implements IActiveCharacter {

	private transient UUID pl;
	private CharacterBase base;

	private Map<String, PlayerClassData> classes = new HashMap<>();

	private transient float[] characterProperties;
	private transient float[] characterPropertiesLevel;

	private transient boolean invulnerable;
	private transient boolean silenced = false;
	private transient boolean isusingguimod;

	private IReservable mana = new Mana(this);
	private IReservable health = new Health(this);

	private transient Party party;

	private transient Map<String, IEffectContainer<Object, IEffect<Object>>> effects = new HashMap<>();
	private Map<String, ExtendedSkillInfo> skills = new HashMap<>();

	private transient Click click = new Click();
	private transient Set<RPGItemType> allowedArmorIds = new HashSet<>();
	private transient Map<ItemType, RPGItemWrapper> allowedWeapons = new HashMap<>();
	private transient Map<EntityType, Double> projectileDamage = new HashMap<>();
	private transient Set<RPGItemType> allowedOffHandWeapons = new HashSet<>();

	private transient WeakReference<Party> pendingPartyInvite = new WeakReference<Party>(null);
	private transient double weaponDamage;
	private transient double armorvalue;

	private transient DamageType preferedDamageType = null;
	private transient Map<String, Integer> transientAttributes = new HashMap<>();

	private transient List<Integer> slotsToReinitialize;
	private transient Map<EquipedSlot, CustomItem> equipedArmor;
	private Set<EquipedSlot> denySlotInteractionArr;

	private Set<SkillTreeSpecialization> specs = new HashSet<>();

	private transient Map<String, SkillTreeViewModel> skillTreeViewLocation;
	private CustomItem offHand;
	private int mainHandSlotId;
	private CustomItem mainHand;

	public ActiveCharacter(Player pl, CharacterBase base) {
		this.pl = pl.getUniqueId();
		characterProperties = new float[PropertyService.LAST_ID];
		characterPropertiesLevel = new float[PropertyService.LAST_ID];
		equipedArmor = new HashMap<>();
		this.base = base;
		slotsToReinitialize = new ArrayList<>();
		skillTreeViewLocation = new HashMap<>();
		denySlotInteractionArr = new HashSet<>();
	}

	public boolean isSilenced() {
		return silenced;
	}

	public void setSilenced(boolean silenced) {
		this.silenced = silenced;
	}

	@Override
	public String getName() {
		return getCharacterBase().getName();
	}

	@Override
	public boolean isStub() {
		return false;
	}

	@Override
	public float[] getCharacterProperties() {
		return characterProperties;
	}

	@Override
	public float getProperty(int index) {
		return characterProperties[index] + characterPropertiesLevel[index] * getPrimaryClass().getLevel();
	}

	@Override
	public void setProperty(int index, float value) {
		characterProperties[index] = value;
	}

	@Override
	public float[] getCharacterLevelProperties() {
		return characterPropertiesLevel;
	}

	@Override
	public void setCharacterLevelProperties(float[] arr) {
		this.characterPropertiesLevel = arr;
	}

	@Override
	public void updateLastKnownLocation(int x, int y, int z, String name) {
		getCharacterBase().setX(x);
		getCharacterBase().setY(y);
		getCharacterBase().setZ(z);
		getCharacterBase().setWorld(name);
	}

	@Override
	public boolean isInvulnerable() {
		return invulnerable;
	}

	@Override
	public void setInvulnerable(boolean b) {
		this.invulnerable = b;
	}


	@Override
	public void setCharacterLevelProperty(int index, float value) {
		characterPropertiesLevel[index] = value;
	}

	@Override
	public float getCharacterPropertyWithoutLevel(int index) {
		return characterProperties[index];
	}

	@Override
	public double getMaxMana() {
		return getMana().getMaxValue();
	}

	@Override
	public void setMaxMana(float mana) {
		getMana().setMaxValue(mana);
	}

	@Override
	public void setMaxHealth(float maxHealth) {
		getHealth().setMaxValue(maxHealth);
	}

	@Override
	public void setHealth(float maxHealth) {
		getHealth().setValue(maxHealth);
	}

	@Override
	public IReservable getMana() {
		return mana;
	}

	@Override
	public void setMana(IReservable mana) {
		this.mana = mana;
	}

	@Override
	public IReservable getHealth() {
		return health;
	}

	@Override
	public void setHealth(IReservable health) {
		this.health = health;
	}


	@Override
	public Player getEntity() {
		return getPlayer();
	}

	@Override
	public Map<EquipedSlot, CustomItem> getEquipedInventorySlots() {
		return equipedArmor;
	}


	@Override
	public Map<java.lang.String, IEffectContainer<Object, IEffect<Object>>> getEffectMap() {
		return effects;
	}

	@Override
	public void addExperiences(double exp, ExperienceSource source) {
		for (PlayerClassData nClass : classes.values()) {
			if (nClass.getClassDefinition().hasExperienceSource(source)) {
				CharacterClass c = getCharacterBase().getCharacterClass(nClass.getClassDefinition());
				Double nClass1 = c.getExperiences() == null ? 0 : c.getExperiences();
				if (nClass1 == null) {
					c.setExperiences(exp);
				}
			}
		}
	}


	@Override
	public Player getPlayer() {
		return Sponge.getServer().getPlayer(pl).orElse(null);
	}

	@Override
	public void setPlayer(Player pl) {
		this.pl = pl.getUniqueId();
	}

	@Override
	public void resetRightClicks() {
		click.setTimes(0);
		click.setLastTime(0);
	}

	@Override
	public int getAttributePoints() {
		return base.getAttributePoints();
	}

	@Override
	public void setAttributePoints(int attributePoints) {
		this.base.setAttributePoints(attributePoints);
	}

	@Override
	public Map<java.lang.String, Integer> getTransientAttributes() {
		return transientAttributes;
	}

	@Override
	public Integer getAttributeValue(java.lang.String name) {
		return base.getAttributes().get(name) + getTransientAttributes().get(name);
	}

	@Override
	public Map<java.lang.String, Long> getCooldowns() {
		return getCharacterBase().getCharacterCooldowns();
	}

	@Override
	public boolean hasCooldown(java.lang.String thing) {
		return getCharacterBase().getCharacterCooldowns().containsKey(thing);
	}

	private void mergeWeapons(ClassDefinition g) {
		mergeWeapons(g.getWeapons());
		HashMap<ItemType, Set<ConfigRPGItemType>> offHandWeapons = g.getOffHandWeapons();
		for (Map.Entry<ItemType, Set<ConfigRPGItemType>> e : offHandWeapons.entrySet()) {
			Set<ConfigRPGItemType> value = e.getValue();
			for (ConfigRPGItemType configRPGItemType : value) {
				allowedOffHandWeapons.add(configRPGItemType.getRpgItemType());
			}
		}
	}

	private void mergeWeapons(Map<ItemType, Set<ConfigRPGItemType>> map) {
		for (Map.Entry<ItemType, Set<ConfigRPGItemType>> toAdd : map.entrySet()) {
			RPGItemWrapper wrapper = allowedWeapons.get(toAdd.getKey());
			if (wrapper == null) {
				wrapper = RPGItemWrapper.createFromSet(toAdd.getValue());
				allowedWeapons.put(toAdd.getKey(), wrapper);
			} else {
				wrapper.addItems(toAdd.getValue());
			}
		}
	}

	@Override
	public double getBaseWeaponDamage(RPGItemType weaponItemType) {
		RPGItemWrapper wrapper = getAllowedWeapons().get(weaponItemType.getItemType());
		if (wrapper == null) {
			return 0D;
		}
		for (ConfigRPGItemType configRPGItemType : wrapper.getItems()) {
			if (weaponItemType.getDisplayName() == null) {
				if (configRPGItemType.getRpgItemType().getDisplayName() == null) {
					//todo check if the displayname is reserved
					return wrapper.getDamage(weaponItemType); //null is first, if both null => can use unnamed item
				}
			} else {
				if (weaponItemType.getDisplayName().equalsIgnoreCase(configRPGItemType.getRpgItemType().getDisplayName())) {
					return wrapper.getDamage(weaponItemType);
				}
			}
		}
		return 0D;
	}

	@Override
	public double getBaseProjectileDamage(EntityType id) {
		Double d = getProjectileDamages().get(id);
		if (d == null) {
			return 0;
		}
		return d;
	}

    @Override
    public IActiveCharacter updateItemRestrictions() {
        Log.debug("Updating item restrictions " + getName());

		allowedWeapons.clear();
		allowedOffHandWeapons.clear();
        allowedArmorIds.clear();
        getProjectileDamages().clear();

        Iterator<PlayerClassData> iterator = classes.values().iterator();
        //put in first
        if (iterator.hasNext()) {
            PlayerClassData next = iterator.next();
            Map<ItemType, Set<ConfigRPGItemType>> weapons = next.getClassDefinition().getWeapons();
            for (Map.Entry<ItemType, Set<ConfigRPGItemType>> entry : weapons.entrySet()) {
                allowedWeapons.put(entry.getKey(), RPGItemWrapper.createFromSet(entry.getValue()));
            }

            HashMap<ItemType, Set<ConfigRPGItemType>> offHandWeapons = next.getClassDefinition().getOffHandWeapons();
            for (Set<ConfigRPGItemType> configRPGItemTypes : offHandWeapons.values()) {
                for (ConfigRPGItemType configRPGItemType : configRPGItemTypes) {
                    allowedOffHandWeapons.add(configRPGItemType.getRpgItemType());
                }
            }
            allowedArmorIds.addAll(next.getClassDefinition().getAllowedArmor());
            getProjectileDamages().putAll(next.getClassDefinition().getProjectileDamage());
        }

        //calculate rest
        while (iterator.hasNext()) {
            PlayerClassData next = iterator.next();

            //merge weapon sets
            mergeWeapons(next.getClassDefinition());

            //might be expensive on massive skilltrees, eventually i could cache these types of skill in an extra collection
            for (ExtendedSkillInfo extendedSkillInfo : getSkills().values()) {
                if (extendedSkillInfo.getSkill().getType() == EffectSourceType.ITEM_ACCESS_SKILL) {
                    ItemAccessSkill.ItemAccessSkillData skillData = (ItemAccessSkill.ItemAccessSkillData) extendedSkillInfo.getSkillData();
                    Map<Integer, Map<ItemType, Set<ConfigRPGItemType>>> items = skillData.getItems();
                    for (Map.Entry<Integer, Map<ItemType, Set<ConfigRPGItemType>>> ent : items.entrySet()) {
                        if (ent.getKey() <= getLevel()) {
                            mergeWeapons(ent.getValue());
                        }
                    }
                }
            }
            allowedArmorIds.addAll(next.getClassDefinition().getAllowedArmor());
        }

		for (PlayerClassData playerClassData : getClasses().values()) {
			ClassDefinition configClass = playerClassData.getClassDefinition();
			Map<EntityType, Double> projectileDamage = configClass.getProjectileDamage();
			for (Map.Entry<EntityType, Double> entityType : projectileDamage.entrySet()) {
				Double aDouble = getProjectileDamages().get(entityType.getKey());
				if (aDouble == null) {
					getProjectileDamages().put(entityType.getKey(), entityType.getValue());
				} else if (pluginConfig.WEAPON_MERGE_STRATEGY == 1) {
					getProjectileDamages().put(entityType.getKey(), aDouble + entityType.getValue());
				} else if (pluginConfig.WEAPON_MERGE_STRATEGY == 2) {
					getProjectileDamages().put(entityType.getKey(), Math.max(aDouble, entityType.getValue()));
				}
			}
		}
		return this;
	}

	@Override
	public Set<RPGItemType> getAllowedArmor() {
		return allowedArmorIds;
	}

	@Override
	public boolean canWear(RPGItemType armor) {
		return getAllowedArmor().contains(armor);
	}

	@Override
	public boolean canUse(RPGItemType weaponItemType, HandType h) {
		if (h == HandTypes.MAIN_HAND) {
			RPGItemWrapper set = getAllowedWeapons().get(weaponItemType.getItemType());
			return set != null && set.containsItem(weaponItemType);
		} else {
			return allowedOffHandWeapons.contains(weaponItemType);
		}
	}


	@Override
	public Map<ItemType, RPGItemWrapper> getAllowedWeapons() {
		return allowedWeapons;
	}

	@Override
	public Map<EntityType, Double> getProjectileDamages() {
		return projectileDamage;
	}

	@Override
	public CharacterBase getCharacterBase() {
		return base;
	}

	@Override
	public void sendMessage(LocalizableParametrizedText message, Arg arg) {
		getPlayer().sendMessage(message.toText(arg));
	}

	@Override
	public void sendMessage(ChatType chatType, Text message) {
		getPlayer().sendMessage(chatType, Text.of(message));
	}

	@Override
	public Map<java.lang.String, ExtendedSkillInfo> getSkills() {
		return Collections.unmodifiableMap(skills); //lets use wrapper class instaed of guava's immutable
	}

	@Override
	public void sendMessage(LocalizableParametrizedText message) {
		sendMessage(message, Arg.EMPTY);
	}

	@Override
	public void addSkill(java.lang.String id, ExtendedSkillInfo info) {
		skills.put(id, info);
	}

	@Override
	public ExtendedSkillInfo getSkill(java.lang.String id) {
		return skills.get(id);
	}

	@Override
	public void getRemoveAllSkills() {
		getCharacterBase().getCharacterSkills().clear();
		skills.clear();
	}

	@Override
	public ExtendedSkillInfo getSkillInfo(ISkill skill) {
		return skills.get(skill.getId());
	}

	@Override
	public ExtendedSkillInfo getSkillInfo(java.lang.String s) {
		return skills.get(s.toLowerCase());
	}

	@Override
	public boolean hasSkill(java.lang.String name) {
		return skills.containsKey(name.toLowerCase());
	}

	@Override
	public int getLevel() {
		return getPrimaryClass().getLevel();
	}

    @Override
    public Map<String, PlayerClassData> getClasses() {
        return classes;
    }

    @Override
	public Party getParty() {
		return party;
	}

	@Override
	public void setParty(Party party) {
		if (this.party != null) {
			party.removePlayer(this);
		}
		this.party = party;
	}

	@Override
	public boolean hasParty() {
		return getParty() != null && getParty().getPlayers().size() > 1;
	}

	@Override
	public boolean isInPartyWith(IActiveCharacter character) {
		return (character.hasParty() && hasParty() && character.getParty() == character.getParty());
	}

	@Override
	public double getWeaponDamage() {
		return weaponDamage;
	}

	@Override
	public void setWeaponDamage(double damage) {
		weaponDamage = damage;
	}

	@Override
	public double getArmorValue() {
		return armorvalue;
	}

	@Override
	public void setArmorValue(double value) {
		armorvalue = value;
	}

	@Override
	public boolean hasPreferedDamageType() {
		return preferedDamageType != null;
	}

	@Override
	public DamageType getDamageType() {
		return preferedDamageType;
	}

	@Override
	public void setDamageType(DamageType damageType) {
		this.preferedDamageType = damageType;
	}

	@Override
	public boolean isUsingGuiMod() {
		return isusingguimod;
	}

	@Override
	public void setUsingGuiMod(boolean b) {
		isusingguimod = b;
	}

	@Override
	public boolean isPartyLeader() {
		return hasParty() && getParty().getLeader() == this;
	}

	@Override
	public Party getPendingPartyInvite() {
		return pendingPartyInvite.get();
	}

	@Override
	public void setPendingPartyInvite(Party party) {
		pendingPartyInvite = new WeakReference<Party>(party);
	}

	@Override
	public MessageType getPreferedMessageType() {
		return getCharacterBase().getMessageType();
	}

	@Override
	public void setPreferedMessageType(MessageType type) {
		this.getCharacterBase().setMessageType(type);
	}

	@Override
	public boolean hasClass(ClassDefinition configClass) {
		String type = configClass.getClassType();
		return getClassByType(type) != null;
	}

	@Override
	public List<Integer> getSlotsToReinitialize() {
		return slotsToReinitialize;
	}

	@Override
	public void setSlotsToReinitialize(List<Integer> slotsToReinitialize) {
		this.slotsToReinitialize = slotsToReinitialize;
	}

	@Override
	public boolean isDetached() {
		return getPlayer() == null;
	}


	@Override
	public boolean isFriendlyTo(IActiveCharacter character) {
		if (character == this) {
			return true;
		}
		return getParty().getPlayers().contains(character);
	}

	@Override
	public Map<java.lang.String, SkillTreeViewModel> getSkillTreeViewLocation() {
		return skillTreeViewLocation;
	}

	@Override
	public SkillTreeViewModel getLastTimeInvokedSkillTreeView() {
		for (SkillTreeViewModel skillTreeViewModel : skillTreeViewLocation.values()) {
			if (skillTreeViewModel.isCurrent()) {
				return skillTreeViewModel;
			}
		}
		return null;
	}

	@Override
	public void addSkillTreeSpecialization(SkillTreeSpecialization specialization) {
		this.specs.add(specialization);
	}

	@Override
	public void removeSkillTreeSpecialization(SkillTreeSpecialization specialization) {
		if (hasSkillTreeSpecialization(specialization)) {
			specs.remove(specialization);
		}
	}

	@Override
	public boolean hasSkillTreeSpecialization(SkillTreeSpecialization specialization) {
		return specs.contains(specialization);
	}

	@Override
	public Set<SkillTreeSpecialization> getSkillTreeSpecialization() {
		return Collections.unmodifiableSet(specs);
	}

	@Override
	public Set<EquipedSlot> getSlotsCannotBeEquiped() {
		return denySlotInteractionArr;
	}

	@Override
	public CustomItem getMainHand() {
		return mainHand;
	}

	@Override
	public int getMainHandSlotId() {
		return mainHandSlotId;
	}

	@Override
	public void setMainHand(CustomItem customItem, int slot) {
		this.mainHand = customItem;
		this.mainHandSlotId = slot;
	}

	@Override
	public CustomItem getOffHand() {
		return offHand;
	}

	@Override
	public void setOffHand(CustomItem customItem) {
		this.offHand = customItem;
	}

	@Override
	public int hashCode() {
		return getPlayer().getUniqueId().hashCode() * 37;
	}

	@Override
	public double getExperienceBonusFor(java.lang.String name, EntityType type) {
		double exp = 0;
		for (PlayerClassData playerClassData : getClasses().values()) {
			exp += playerClassData.getClassDefinition().getExperiencesBonus(name, type);
		}
		return exp;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ActiveCharacter that = (ActiveCharacter) o;
		return that.getCharacterBase().getId().equals(this.getCharacterBase().getId());
	}

	@Override
	public java.lang.String toString() {
		return "ActiveCharacter{" +
				"uuid=" + pl +
				" name=" + getName() +
				'}';
	}
}
