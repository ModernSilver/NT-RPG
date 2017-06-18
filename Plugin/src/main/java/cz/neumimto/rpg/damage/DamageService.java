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

package cz.neumimto.rpg.damage;

import cz.neumimto.core.ioc.Inject;
import cz.neumimto.core.ioc.PostProcess;
import cz.neumimto.core.ioc.Singleton;
import cz.neumimto.rpg.IEntity;
import cz.neumimto.rpg.IEntityType;
import cz.neumimto.rpg.entities.EntityService;
import cz.neumimto.rpg.players.CharacterService;
import cz.neumimto.rpg.players.IActiveCharacter;
import cz.neumimto.rpg.players.properties.DefaultProperties;
import cz.neumimto.rpg.players.properties.PropertyService;
import cz.neumimto.rpg.skills.NDamageType;
import cz.neumimto.rpg.utils.ItemStackUtils;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Created by NeumimTo on 4.8.15.
 */
@Singleton
public class DamageService {

    @Inject
    public EntityService entityService;

    @Inject
    private CharacterService characterService;


    public BiFunction<Double, Double, Double> DamageArmorReductionFactor = (damage, armor) -> armor / (armor + 10 * damage);

    private Map<ItemType, Integer> map = new HashMap<>();

    public double getCharacterItemDamage(IActiveCharacter character, ItemType type) {
        if (character.isStub() || type == null)
            return 1;
        double base = character.getBaseWeaponDamage(type) + characterService.getCharacterProperty(character, DefaultProperties.weapon_damage_bonus);
        if (map.containsKey(type)) {
            base += characterService.getCharacterProperty(character, map.get(type));
        } else return 1;
        if (ItemStackUtils.isSword(type)) {
            base *= characterService.getCharacterProperty(character, DefaultProperties.swords_damage_mult);
        } else if (ItemStackUtils.isAxe(type)) {
            base *= characterService.getCharacterProperty(character, DefaultProperties.axes_damage_mult);
        } else if (ItemStackUtils.isPickaxe(type)) {
            base *= characterService.getCharacterProperty(character, DefaultProperties.pickaxes_damage_mult);
        } else if (ItemStackUtils.isHoe(type)) {
            base *= characterService.getCharacterProperty(character, DefaultProperties.hoes_damage_mult);
        } else if (ItemStackUtils.isBow(type)) {
            base *= characterService.getCharacterProperty(character, DefaultProperties.bows_meele_damage_mult);
        } else if (ItemStackUtils.isStaff(type)) {
            base *= characterService.getCharacterProperty(character, DefaultProperties.staffs_damage_mult);
        }
        return base;
    }

    public double getCharacterProjectileDamage(IActiveCharacter character, EntityType type) {
        if (character.isStub() || type == null)
            return 1;
        double base = character.getBaseProjectileDamage(type) + characterService.getCharacterProperty(character, DefaultProperties.projectile_damage_bonus);
        if (type == EntityTypes.SPECTRAL_ARROW || type == EntityTypes.TIPPED_ARROW) {
            base *= characterService.getCharacterProperty(character, DefaultProperties.arrow_damage_mult);
        } else {
            base *= characterService.getCharacterProperty(character, DefaultProperties.other_projectile_damage_mult);
        }
        return base;
    }

    public void recalculateCharacterWeaponDamage(IActiveCharacter character) {
        if (character.isStub()) {
			return;
        }
        ItemStack i = character.getPlayer().getItemInHand(HandTypes.MAIN_HAND).orElse(null);
        double damage = getCharacterItemDamage(character, i == null ? null : i.getItem());
           // damage += character.getMainHand().getDamage() + character.getOffHand().getDamage();
        character.setWeaponDamage(damage);

    }

    public double getEntityResistance(IEntity entity, DamageType source) {
        if (source == DamageTypes.ATTACK)
            return entityService.getEntityProperty(entity, DefaultProperties.physical_damage_protection_mult);
        if (source == DamageTypes.FIRE)
            return entityService.getEntityProperty(entity, DefaultProperties.fire_damage_protection_mult);
        if (source == DamageTypes.MAGIC)
            return entityService.getEntityProperty(entity, DefaultProperties.magic_damage_protection_mult);
        if (source == NDamageType.LIGHTNING)
            return entityService.getEntityProperty(entity, DefaultProperties.lightning_damage_protection_mult);
        if (source == NDamageType.ICE)
            return entityService.getEntityProperty(entity, DefaultProperties.ice_damage_protection_mult);
        return 1;
    }

    public double getEntityBonusDamage(IEntity entity, DamageType source) {
        if (source == DamageTypes.ATTACK)
            return entityService.getEntityProperty(entity, DefaultProperties.physical_damage_bonus_mult);
        if (source == DamageTypes.FIRE)
            return entityService.getEntityProperty(entity, DefaultProperties.fire_damage_bonus_mult);
        if (source == DamageTypes.MAGIC)
            return entityService.getEntityProperty(entity, DefaultProperties.magic_damage_bonus_mult);
        if (source == NDamageType.LIGHTNING)
            return entityService.getEntityProperty(entity, DefaultProperties.lightning_damage_bonus_mult);
        if (source == NDamageType.ICE)
            return entityService.getEntityProperty(entity, DefaultProperties.ice_damage_bonus_mult);
        return 0;
    }

    @PostProcess(priority = 6)
    public void buildPropertiesMap() {
        map.put(ItemTypes.DIAMOND_SWORD, DefaultProperties.diamond_sword_bonus_damage);
        map.put(ItemTypes.GOLDEN_SWORD, DefaultProperties.golden_sword_bonus_damage);
        map.put(ItemTypes.IRON_SWORD, DefaultProperties.iron_sword_bonus_damage);
        map.put(ItemTypes.WOODEN_SWORD, DefaultProperties.wooden_sword_bonus_damage);

        map.put(ItemTypes.DIAMOND_AXE, DefaultProperties.diamond_axe_bonus_damage);
        map.put(ItemTypes.GOLDEN_AXE, DefaultProperties.golden_axe_bonus_damage);
        map.put(ItemTypes.IRON_AXE, DefaultProperties.iron_axe_bonus_damage);
        map.put(ItemTypes.WOODEN_AXE, DefaultProperties.wooden_axe_bonus_damage);

        map.put(ItemTypes.DIAMOND_PICKAXE, DefaultProperties.diamond_pickaxe_bonus_damage);
        map.put(ItemTypes.GOLDEN_PICKAXE, DefaultProperties.golden_pickaxe_bonus_damage);
        map.put(ItemTypes.IRON_PICKAXE, DefaultProperties.iron_pickaxe_bonus_damage);
        map.put(ItemTypes.WOODEN_PICKAXE, DefaultProperties.wooden_pickaxe_bonus_damage);


        map.put(ItemTypes.DIAMOND_HOE, DefaultProperties.diamond_hoe_bonus_damage);
        map.put(ItemTypes.GOLDEN_HOE, DefaultProperties.golden_hoe_bonus_damage);
        map.put(ItemTypes.IRON_HOE, DefaultProperties.iron_hoe_bonus_damage);
        map.put(ItemTypes.WOODEN_HOE, DefaultProperties.wooden_hoe_bonus_damage);

        map.put(ItemTypes.BOW, DefaultProperties.bow_meele_bonus_damage);


    }

}
