package cz.neumimto.rpg.gui;

import cz.neumimto.core.ioc.IoC;
import cz.neumimto.rpg.NtRpgPlugin;
import cz.neumimto.rpg.commands.InfoCommand;
import cz.neumimto.rpg.configuration.Localization;
import cz.neumimto.rpg.effects.EffectSourceType;
import cz.neumimto.rpg.inventory.data.InventoryCommandItemMenuData;
import cz.neumimto.rpg.inventory.data.MenuInventoryData;
import cz.neumimto.rpg.inventory.data.NKeys;
import cz.neumimto.rpg.inventory.data.SkillTreeInventoryViewControllsData;
import cz.neumimto.rpg.players.IActiveCharacter;
import cz.neumimto.rpg.players.SkillTreeViewModel;
import cz.neumimto.rpg.players.groups.PlayerGroup;
import cz.neumimto.rpg.skills.*;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.util.*;

import static cz.neumimto.rpg.gui.CatalogTypeItemStackBuilder.Block;
import static cz.neumimto.rpg.gui.CatalogTypeItemStackBuilder.Item;

/**
 * Created by ja on 29.12.2016.
 */
public class GuiHelper {


	private static NtRpgPlugin plugin;

	public static GameProfile HEAD_ARROW_DOWN;
	public static GameProfile HEAD_ARROW_UP;
	public static GameProfile HEAD_ARROW_LEFT;
	public static GameProfile HEAD_ARROW_RIGHT;

	public static Map<DamageType, CatalogTypeItemStackBuilder> damageTypeToItemStack = new HashMap<>();

	static {
		plugin = IoC.get().build(NtRpgPlugin.class);
		HEAD_ARROW_DOWN = GameProfile.of(UUID.fromString("f14aa295-a1b0-4edd-974c-e1e00d9a1e39"));
		HEAD_ARROW_UP = GameProfile.of(UUID.fromString("96f198b9-1e67-4b68-bbd1-c5213797e58a"));
		HEAD_ARROW_LEFT = GameProfile.of(UUID.fromString("4d35f021-81b6-44ee-a711-8d8462174124"));
		HEAD_ARROW_RIGHT = GameProfile.of(UUID.fromString("1f961930-4e97-47b7-a5a1-2cc5150f3764"));

		damageTypeToItemStack.put(DamageTypes.ATTACK, Item.of(ItemTypes.STONE_SWORD));
		damageTypeToItemStack.put(DamageTypes.CONTACT, Item.of(ItemTypes.CACTUS));

		damageTypeToItemStack.put(DamageTypes.CUSTOM, Item.of(ItemTypes.BARRIER));

		damageTypeToItemStack.put(DamageTypes.DROWN, Block.of(BlockTypes.WATER));
		damageTypeToItemStack.put(DamageTypes.EXPLOSIVE, Item.of(ItemTypes.TNT));
		damageTypeToItemStack.put(DamageTypes.FALL, Item.of(ItemTypes.IRON_BOOTS));
		damageTypeToItemStack.put(DamageTypes.FIRE, Block.of(BlockTypes.FIRE));

		damageTypeToItemStack.put(DamageTypes.HUNGER, Item.of(ItemTypes.ROTTEN_FLESH));
		damageTypeToItemStack.put(DamageTypes.MAGMA, Block.of(BlockTypes.LAVA));
		damageTypeToItemStack.put(DamageTypes.PROJECTILE, Item.of(ItemTypes.TIPPED_ARROW));
		damageTypeToItemStack.put(DamageTypes.VOID, Block.of(BlockTypes.PORTAL));
		damageTypeToItemStack.put(DamageTypes.MAGIC, Item.of(ItemTypes.ENCHANTED_BOOK));

		damageTypeToItemStack.put(NDamageType.ICE, Block.of(BlockTypes.FROSTED_ICE));
		damageTypeToItemStack.put(NDamageType.MAGICAL, Item.of(ItemTypes.ENCHANTED_BOOK));
		damageTypeToItemStack.put(NDamageType.MEELE_CRITICAL, Item.of(ItemTypes.DIAMOND_SWORD));
		damageTypeToItemStack.put(NDamageType.PHYSICAL, Item.of(ItemTypes.IRON_NUGGET));
		damageTypeToItemStack.put(NDamageType.LIGHTNING, Item.of(ItemTypes.NETHER_STAR));
	}

	public static ItemStack damageTypeToItemStack(DamageType type) {
		if (type == null)
			return ItemStack.empty();
		CatalogTypeItemStackBuilder a = damageTypeToItemStack.get(type);
		ItemStack is = null;
		if (a == null) {
			is = ItemStack.of(ItemTypes.STONE, 1);
		} else {
			is = a.toItemStack();
		}
		is.offer(new MenuInventoryData(true));
		is.offer(Keys.DISPLAY_NAME, Text.of(type.getName()));
		return is;
	}

	public static Inventory createPlayerGroupView(PlayerGroup group) {
		Inventory.Builder builder = Inventory.builder();
		Inventory i = builder.of(InventoryArchetypes.DOUBLE_CHEST).build(plugin);
		i.query(new SlotPos(2, 2)).offer(createWeaponCommand(group));
		i.query(new SlotPos(3, 2)).offer(createArmorCommand(group));
		i.query(new SlotPos(2, 3)).offer(createAttributesCommand(group));
		i.query(new SlotPos(0, 0)).offer(createDescriptionItem(group.getDescription()));
		return i;
	}

	public static ItemStack createAttributesCommand(PlayerGroup group) {
		ItemStack i = ItemStack.of(ItemTypes.BOOK, 1);
		i.offer(NKeys.MENU_INVENTORY, true);
		i.offer(Keys.DISPLAY_NAME, Text.of(Localization.ATTRIBUTES, TextColors.DARK_RED));
		String cc = IoC.get().build(InfoCommand.class).getAliases().iterator().next();
		i.offer(new InventoryCommandItemMenuData(cc + " attributes-initial " + group.getName()));
		return i;
	}

	public static ItemStack createDescriptionItem(String description) {
		ItemStack i = ItemStack.of(ItemTypes.PAPER, 1);
		i.offer(Keys.DISPLAY_NAME, Text.of(""));
		i.offer(Keys.HIDE_MISCELLANEOUS, true);
		i.offer(Keys.HIDE_ATTRIBUTES, true);
		i.offer(new MenuInventoryData(true));
		i.offer(Keys.ITEM_LORE, Collections.singletonList(Text.of(description, TextColors.GRAY)));
		return i;
	}

	public static ItemStack createArmorCommand(PlayerGroup group) {
		ItemStack i = ItemStack.of(ItemTypes.DIAMOND_CHESTPLATE, 1);
		i.offer(NKeys.MENU_INVENTORY, true);
		i.offer(Keys.DISPLAY_NAME, Text.of(Localization.WEAPONS, TextColors.DARK_RED));
		i.offer(Keys.ITEM_LORE, Collections.singletonList(Text.of(Localization.WEAPONS_MENU_HELP, TextColors.GRAY)));
		i.offer(Keys.HIDE_MISCELLANEOUS, true);
		i.offer(Keys.HIDE_ATTRIBUTES, true);
		i.offer(new InventoryCommandItemMenuData("show armor " + group.getName()));
		return i;
	}

	public static ItemStack createWeaponCommand(PlayerGroup group) {
		ItemStack i = ItemStack.of(ItemTypes.DIAMOND_SWORD, 1);
		i.offer(NKeys.MENU_INVENTORY, true);
		i.offer(Keys.DISPLAY_NAME, Text.of(Localization.ARMOR, TextColors.DARK_RED));
		i.offer(Keys.ITEM_LORE, Collections.singletonList(Text.of(Localization.ARMOR_MENU_HELP, TextColors.GRAY)));
		i.offer(Keys.HIDE_MISCELLANEOUS, true);
		i.offer(Keys.HIDE_ATTRIBUTES, true);
		i.offer(new InventoryCommandItemMenuData("show weapons " + group.getName()));
		return i;
	}

	public static List<Text> getItemLore(String s) {
		String[] a = s.split("\\n");
		List<Text> t = new ArrayList<>();
		for (String s1 : a) {
			t.add(Text.builder(s1).color(TextColors.GOLD).style(TextStyles.ITALIC).build());
		}
		return t;
	}

	public static ItemStack back(String command, String displayName) {
		ItemStack of = ItemStack.of(ItemTypes.PAPER, 1);
		of.offer(Keys.DISPLAY_NAME, Text.of(displayName, TextColors.WHITE));
		of.offer(new InventoryCommandItemMenuData(command));
		return of;
	}

	public static ItemStack unclickableInterface() {
		ItemStack of = ItemStack.of(ItemTypes.STAINED_GLASS_PANE, 1);
		of.offer(new MenuInventoryData(true));
		of.offer(Keys.DYE_COLOR, DyeColors.YELLOW);
		of.offer(Keys.DISPLAY_NAME, Text.EMPTY);
		return of;
	}

	public static ItemStack back(PlayerGroup g) {
		ItemStack of = ItemStack.of(ItemTypes.PAPER, 1);
		String l = "class";
		if (g.getType() == EffectSourceType.RACE) {
			l = "race";
		}
		of.offer(Keys.DISPLAY_NAME, Text.of(Localization.BACK, TextColors.WHITE));
		String c = IoC.get().build(InfoCommand.class).getAliases().get(0);
		of.offer(new InventoryCommandItemMenuData(c + " " + l + " " + g.getName()));
		return of;
	}

	public static ItemStack skillToItemStack(IActiveCharacter character, SkillData skillData) {
		ISkill skill = skillData.getSkill();
		ItemType itemType = skill.getIcon().itemType;
		if (itemType == null) {
			itemType = ItemTypes.STONE;
		}

		ItemStack.Builder builder = ItemStack.builder();
		builder.itemType(itemType);
		List<Text> lore = new ArrayList<>();

		String desc = skill.getDescription();
		String skillTargetType = Localization.SKILL_TYPE_TARGETTED;
		if (skill instanceof ActiveSkill) {
			skillTargetType = Localization.SKILL_TYPE_ACTIVE;
		} else if (skill instanceof PassiveSkill) {
			skillTargetType = Localization.SKILL_TYPE_PASSIVE;
		}
		if (desc != null)
			for (String s : desc.split(":n")) {
				lore.add(Text.of(s, TextColors.GOLD));
			}

		lore.add(Text.of(skillTargetType, TextColors.DARK_PURPLE, TextStyles.ITALIC));
		lore.add(Text.EMPTY);

		int minPlayerLevel = skillData.getMinPlayerLevel();
		int maxSkillLevel = skillData.getMaxSkillLevel();

		String s = Localization.MIN_PLAYER_LEVEL;
		if (minPlayerLevel > 0) {
			lore.add(Text.builder(s).color(TextColors.YELLOW)
					.append(Text.builder("" + minPlayerLevel)
							.color(character.getLevel() < minPlayerLevel ? TextColors.RED : TextColors.GREEN)
							.build())
					.build());
		}
		s = Localization.MAX_SKILL_LEVEL + " " + maxSkillLevel;
		lore.add(Text.builder(s)
				.color(TextColors.YELLOW)
				.build());
		lore.add(Text.EMPTY);


		if (skill.getLore() != null) {
			String[] split = skill.getLore().split(":n");
			for (String ss : split) {
				lore.add(Text.builder(ss).style(TextStyles.ITALIC).color(TextColors.GOLD).build());
			}
		}

		ItemStack is = ItemStack.builder().itemType(itemType)
				.quantity(1)
				.add(Keys.ITEM_LORE, lore)
				.build();
		is.offer(new MenuInventoryData(true));
		is.offer(Keys.DISPLAY_NAME, Text.builder(skill.getName()).style(TextStyles.BOLD).build());
		return is;
	}


	public static Inventory createSkillTreeInventoryViewTemplate(IActiveCharacter character, SkillTree skillTree) {
		Inventory i = Inventory.builder()
				.of(InventoryArchetypes.DOUBLE_CHEST)
				.build(plugin);

		i.query(new SlotPos(7, 0)).offer(unclickableInterface());
		i.query(new SlotPos(7, 1)).offer(unclickableInterface());
		i.query(new SlotPos(7, 2)).offer(unclickableInterface());
		i.query(new SlotPos(7, 3)).offer(unclickableInterface());
		i.query(new SlotPos(7, 4)).offer(unclickableInterface());
		i.query(new SlotPos(7, 5)).offer(unclickableInterface());

		ItemStack md = ItemStack.of(ItemTypes.PAPER, 1);

		md.offer(new SkillTreeInventoryViewControllsData("mode"));

		List<Text> lore = new ArrayList<>();
		SkillTreeViewModel model = character.getSkillTreeViewLocation().get(skillTree.getId());
		if (model == null) {
			model = new SkillTreeViewModel();
			character.getSkillTreeViewLocation().put(skillTree.getId(), model);
		}
		lore.add(Text.builder().build());
		lore.add(Text.builder(model.getInteractiveMode().getTransltion()).build());
		md.offer(Keys.ITEM_LORE, lore);

		i.query(new SlotPos(8, 1)).offer(md);

		i.query(new SlotPos(8, 2)).offer(createHead(/*HEAD_ARROW_UP*/ "Up"));
		i.query(new SlotPos(8, 3)).offer(createHead(/*HEAD_ARROW_DOWN*/ "Down"));
		i.query(new SlotPos(8, 4)).offer(createHead(/*HEAD_ARROW_RIGHT*/ "Right"));
		i.query(new SlotPos(8, 5)).offer(createHead(/*HEAD_ARROW_LEFT*/ "Left"));

		return i;
	}
	public static ItemStack createHead(/* GameProfile gameProfile*/ String name) {
		ItemStack of = ItemStack.of(ItemTypes.STONE, 1);
		of.offer(Keys.HIDE_MISCELLANEOUS, true);
		of.offer(Keys.HIDE_ATTRIBUTES, true);
		of.offer(Keys.DISPLAY_NAME, Text.of(name));
		of.offer(new SkillTreeInventoryViewControllsData(name));
		//of.offer(Keys.SKULL_TYPE, SkullTypes.PLAYER);
		//of.offer(Keys.REPRESENTED_PLAYER, gameProfile);

		return of;
	}

	public static ItemStack createSkillTreeInventoryMenuBoundary() {
		ItemStack of = ItemStack.of(ItemTypes.STAINED_GLASS_PANE, 1);
		of.offer(Keys.HIDE_MISCELLANEOUS, true);
		of.offer(Keys.HIDE_ATTRIBUTES, true);
		of.offer(Keys.DISPLAY_NAME, Text.EMPTY);
		of.offer(new MenuInventoryData(true));
		of.offer(Keys.DYE_COLOR, DyeColors.RED);
		return of;
	}

	public static ItemStack createSkillTreeConfirmButtom() {
		ItemStack itemStack = ItemStack.of(ItemTypes.KNOWLEDGE_BOOK, 1);
		itemStack.offer(Keys.HIDE_MISCELLANEOUS, true);
		itemStack.offer(Keys.HIDE_ATTRIBUTES, true);
		itemStack.offer(Keys.DISPLAY_NAME, Text.of(Localization.CONFIRM_SKILL_SELECTION_BUTTON));
		itemStack.offer(new SkillTreeInventoryViewControllsData("confirm"));
		return itemStack;
	}

	public static Inventory createSkillDetailInventoryView(String skillTree, SkillData skillData) {
		Inventory build = Inventory.builder()
				.of(InventoryArchetypes.DOUBLE_CHEST)
				.build(plugin);

		ItemStack back = back("skilltree", Localization.SKILLTREE);
		build.query(new SlotPos(0,0)).offer(back);

		build.query(new SlotPos(0,1)).offer(damageTypeToItemStack(skill.getDamageType()));

		return build;
	}
}
