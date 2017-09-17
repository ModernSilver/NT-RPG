package cz.neumimto.effects.negative;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import cz.neumimto.rpg.effects.EffectBase;
import cz.neumimto.rpg.effects.IEffectConsumer;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * Created by NeumimTo on 20.8.2017.
 */
public class WebEffect extends EffectBase<Long> {

    public static String name = "Web";
    private Vector3i[] vector3is = new Vector3i[8];
    public WebEffect(IEffectConsumer effectConsumer, long duration) {
        super(name, effectConsumer);
        setDuration(duration);
        setStackable(false, null);
    }

    @Override
    public void onApply() {
        super.onApply();
        Location<World> location = getConsumer().getEntity().getLocation();
        Vector3d position = location.getPosition();
        int floorY = position.getFloorY();
        BlockState build = BlockState.builder().blockType(BlockTypes.WEB).build();
        int b = 0;
        for (int i = -1; i <= 1; i++) {
            for (int x = -1; x <= 1; x++) {
                Vector3i vector3i = new Vector3i(
                        position.getFloorX() + i,
                        floorY,
                        position.getFloorZ() + x);
                vector3is[b] = vector3i;
                b++;
                location.getExtent().setBlock(
                        vector3i,
                        build,
                        BlockChangeFlag.NONE
                );
            }
        }
    }

    @Override
    public void onRemove() {
        super.onRemove();
        Location<World> location = getConsumer().getEntity().getLocation();
        BlockState build = BlockState.builder().blockType(BlockTypes.AIR).build();

        for (Vector3i vector3i : vector3is) {
            BlockState block = location.getExtent().getBlock(vector3i);
            if (block.getType() == BlockTypes.WEB) {
                location.getExtent().setBlock(
                        vector3i,
                        build,
                        BlockChangeFlag.NONE
                );
            }
        }
    }
}