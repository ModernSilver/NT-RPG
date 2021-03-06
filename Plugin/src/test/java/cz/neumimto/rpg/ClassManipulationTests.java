package cz.neumimto.rpg;


import cz.neumimto.rpg.api.ActionResult;
import cz.neumimto.rpg.configuration.ClassTypeDefinition;
import cz.neumimto.rpg.junit.CharactersExtension;
import cz.neumimto.rpg.junit.CharactersExtension.Stage;
import cz.neumimto.rpg.junit.NtRpgExtension;
import cz.neumimto.rpg.persistance.model.CharacterClass;
import cz.neumimto.rpg.players.CharacterService;
import cz.neumimto.rpg.players.IActiveCharacter;
import cz.neumimto.rpg.players.PlayerClassData;
import cz.neumimto.rpg.players.groups.ClassDefinition;
import name.falgout.jeffrey.testing.junit.guice.GuiceExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.LinkedHashMap;

import static cz.neumimto.rpg.junit.CharactersExtension.Stage.Stages.READY;

@ExtendWith({CharactersExtension.class, GuiceExtension.class, NtRpgExtension.class})
public class ClassManipulationTests {

    ClassDefinition pc1;
    ClassDefinition pc2;
    ClassDefinition pc3;


    ClassDefinition ps1;
    ClassDefinition ps2;
    ClassDefinition ps3;

    @Inject
    CharacterService characterService;

    private IActiveCharacter character;

    @BeforeEach
    public void beforeEach(@Stage(READY) IActiveCharacter character) {
        this.character = character;


        NtRpgPlugin.pluginConfig.CLASS_TYPES = new LinkedHashMap<String, ClassTypeDefinition>() {{
            put("Primary", new ClassTypeDefinition(null, null, null, false, 1));
            put("Secondary", new ClassTypeDefinition(null, null, null, false, 2));
        }};

        pc1 = new ClassDefinition("class1", "Primary");
        pc2 = new ClassDefinition("class2", "Primary");
        pc3 = new ClassDefinition("class3", "Primary");

        pc1.getClassDependencyGraph().getConflicts().addAll(Arrays.asList(pc2, pc3));
        pc2.getClassDependencyGraph().getConflicts().addAll(Arrays.asList(pc1, pc3));
        pc3.getClassDependencyGraph().getConflicts().addAll(Arrays.asList(pc1, pc2));

        ps1 = new ClassDefinition("secondary1", "Secondary");
        ps2 = new ClassDefinition("secondary2", "Secondary");
        ps3 = new ClassDefinition("secondary3", "Secondary");

        ps1.getClassDependencyGraph().getConflicts().addAll(Arrays.asList(ps2, ps3));
        ps2.getClassDependencyGraph().getConflicts().addAll(Arrays.asList(ps1, ps3));
        ps3.getClassDependencyGraph().getConflicts().addAll(Arrays.asList(ps1, ps2));

    }


    @Test
    public void may_assign_primary_primary_as_first() {
        ActionResult result = characterService.canGainClass(character, pc1);
        Assertions.assertTrue(result.isOk());
    }

    @Test
    public void may_not_select_same_type() {
        CharacterClass characterClass = new CharacterClass();
        PlayerClassData playerClassData = new PlayerClassData(pc1, characterClass);
        character.addClass(playerClassData);
        ActionResult result = characterService.canGainClass(character, pc2);
        Assertions.assertTrue(!result.isOk());
    }

    @Test
    public void respects_class_selection_order() {
        NtRpgPlugin.pluginConfig.RESPECT_CLASS_SELECTION_ORDER = true;
        ActionResult result = characterService.canGainClass(character, ps2);
        Assertions.assertTrue(!result.isOk());
    }

    @Test
    public void select_secondary_class() {
        NtRpgPlugin.pluginConfig.RESPECT_CLASS_SELECTION_ORDER = true;
        CharacterClass characterClass = new CharacterClass();
        PlayerClassData playerClassData = new PlayerClassData(pc1, characterClass);
        character.addClass(playerClassData);

        ActionResult result = characterService.canGainClass(character, ps1);
        Assertions.assertTrue(result.isOk());
    }

    @Test
    public void select_secondary_before_primary() {
        NtRpgPlugin.pluginConfig.RESPECT_CLASS_SELECTION_ORDER = false;
        ActionResult result = characterService.canGainClass(character, ps2);
        Assertions.assertTrue(result.isOk());
    }

}
