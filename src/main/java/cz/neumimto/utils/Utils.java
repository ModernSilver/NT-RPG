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

package cz.neumimto.utils;

import com.flowpowered.math.vector.Vector3d;
import cz.neumimto.GlobalScope;
import cz.neumimto.IEntity;
import cz.neumimto.NtRpgPlugin;
import cz.neumimto.entities.IMob;
import cz.neumimto.players.IActiveCharacter;
import cz.neumimto.players.properties.PlayerPropertyService;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.*;
import java.util.function.Predicate;

/**
 * Created by NeumimTo on 25.7.2015.
 */
public class Utils {

    private static GlobalScope globalScope = NtRpgPlugin.GlobalScope;
    public static String LineSeparator = System.getProperty("line.separator");
    public static String Tab = "\t";

    public static double getPercentage(double n, double total) {
        return (n / total) * 100;
    }

    public static boolean isMoreThanPercentage(double a, double b, double percentage) {
        return ((a / b) * 100 - 100) >= percentage;
    }

    public static double round(double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

    public static double round(float value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return Math.round(value * scale) / scale;
    }

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    public static Set<Entity> getNearbyEntities(Location l, int radius) {
        double s = Math.pow(radius, 2);
        HashSet<Entity> ee = new HashSet<>();
        for (Entity e : l.getExtent().getEntities()) {
            if (e.getLocation().getPosition().distanceSquared(l.getX(), l.getY(), l.getZ()) <= s) {
                ee.add(e);
            }
        }
        return ee;
    }


    public static Optional<Entity> spawnProjectile(IEntity caster, EntityType type) {
        World world = caster.getEntity().getWorld();
        return world.createEntity(type, getFacingVector(caster.getEntity(),1));
    }

    public static Vector3d getFacingVector(Entity entity, int mult) {
        double yaw = (entity.getRotation().getX() + 90) % 360;
        double pitch = (entity.getRotation().getY()) * -1;
        double a = Math.cos(Math.toRadians(pitch));
        double b = Math.cos(Math.toRadians(yaw));
        double c = Math.sin(Math.toRadians(pitch));
        double d = Math.sin(Math.toRadians(yaw));
        return new Vector3d((mult * a) * b, mult * c, (mult * a) * d);
    }

    public static Vector3d getFacingVector(Entity entity) {
        double yaw = (entity.getRotation().getX() + 90) % 360;
        double pitch = (entity.getRotation().getY()) * -1;
        double a = Math.cos(Math.toRadians(pitch));
        double b = Math.cos(Math.toRadians(yaw));
        double c = Math.sin(Math.toRadians(pitch));
        double d = Math.sin(Math.toRadians(yaw));
        return new Vector3d(a * b, c, a * d);
    }

    public static Set<BlockType> transparentBlocks = new HashSet<>();

    public static boolean isTransparent(BlockType e) {
        return true;
    }

    public static Living getTargettedEntity(IActiveCharacter character, int range) {
        Player player = character.getPlayer();
        Set<Entity> nearbyEntities = getNearbyEntities(player.getLocation(), range);
        Iterator<BlockRayHit<World>> iterator = BlockRay.from(character.getPlayer()).blockLimit(range).iterator();
        while (iterator.hasNext()) {
            BlockRayHit<World> next = iterator.next();
            if (!isTransparent(next.getLocation().getBlockType())) {
                return null;
            }
            int blockX = next.getBlockX();
            int blockY = next.getBlockY();
            int blockZ = next.getBlockZ();
            for (Entity n : nearbyEntities) {
                if (isLivingEntity(n)) {
                    if (n.getLocation().getBlockX() == blockX
                            && n.getLocation().getBlockZ() == blockZ
                            && n.getLocation().getBlockY() == blockY) {
                        return (Living) n;
                    }
                }
            }
        }
        return null;
    }

    public static void hideProjectile(Projectile projectile) {
        projectile.offer(Keys.INVISIBLE, true);
    }

    public static String newLine(String s) {
        return Tab + s + LineSeparator;
    }

    /**
     * Resets stats of vanilla player object back to default state, Resets max hp, speed
     *
     * @param player
     */
    public static void resetPlayerToDefault(Player player) {
        player.offer(Keys.MAX_HEALTH, 20d);
        player.offer(Keys.HEALTH, 20d);
        player.offer(Keys.WALKING_SPEED, PlayerPropertyService.WALKING_SPEED);
    }

    /**
     * Inline negation of method references
     */
    public static <T> Predicate<T> not(Predicate<T> t) {
        return t.negate();
    }


    public static boolean isLivingEntity(Entity entity) {
        return entity.get(Keys.HEALTH).isPresent();
    }

}
