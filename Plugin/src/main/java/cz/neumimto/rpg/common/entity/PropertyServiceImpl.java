package cz.neumimto.rpg.common.entity;

import cz.neumimto.rpg.api.entity.PropertyService;
import cz.neumimto.rpg.api.logging.Log;
import cz.neumimto.rpg.api.utils.Console;
import cz.neumimto.rpg.players.attributes.Attribute;
import cz.neumimto.rpg.properties.Property;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

import static cz.neumimto.rpg.NtRpgPlugin.pluginConfig;
import static cz.neumimto.rpg.api.logging.Log.info;

public abstract class PropertyServiceImpl implements PropertyService {


    public static final double WALKING_SPEED = 0.1d;
    public static int LAST_ID = 0;
    public static final Supplier<Integer> getAndIncrement = () -> {
        int t = new Integer(LAST_ID);
        LAST_ID++;
        return t;
    };

    protected Map<String, Integer> idMap = new HashMap<>();
    private Map<Integer, String> nameMap = new HashMap<>();

    private Map<Integer, Float> defaults = new HashMap<>();

    private Set<Integer> damageRecalc = new HashSet<>();

    protected float[] maxValues;


    public void registerProperty(String name, int id) {
        info("A new property " + name + "; assigned id: " + id, pluginConfig.DEBUG);
        idMap.put(name, id);
        nameMap.put(id, name);
    }

    @Override
    public int getIdByName(String name) {
        return idMap.get(name);
    }

    @Override
    public boolean exists(String property) {
        return idMap.containsKey(property);
    }

    @Override
    public String getNameById(Integer id) {
        return nameMap.get(id);
    }

    @Override
    public void registerDefaultValue(int id, float def) {
        defaults.put(id, def);
    }

    @Override
    public float getDefaultValue(int id) {
        return defaults.get(id);
    }

    @Override
    public Map<Integer, Float> getDefaults() {
        return defaults;
    }

    @Override
    public void processContainer(Class<?> container) {
        int value;
        for (Field f : container.getDeclaredFields()) {
            if (f.isAnnotationPresent(Property.class)) {
                Property p = f.getAnnotation(Property.class);
                value = PropertyServiceImpl.getAndIncrement.get();
                try {
                    f.setInt(null, value);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    continue;
                }
                if (!p.name().trim().equalsIgnoreCase("")) {
                    registerProperty(p.name(), value);
                }
                if (p.default_() != 0f) {
                    registerDefaultValue(value, p.default_());
                }
            }
        }
    }

    @Override
    public float getDefault(Integer key) {
        Float f = defaults.get(key);
        if (f == null) {
            return 0;
        }
        return f;
    }

    @Override
    public float getMaxPropertyValue(int index) {
        return maxValues[index];
    }

    @Override
    public Collection<String> getAllProperties() {
        return nameMap.values();
    }

    @Override
    public void overrideMaxPropertyValue(String s, Float aFloat) {
        if (!nameMap.containsValue(s)) {
            Log.info("Attempt to override default value for a property \""+s+"\". But such property does not exists yet. THe property will be created");
            registerProperty(s, getAndIncrement.get());
        }
        defaults.put(getIdByName(s), aFloat);
        Log.info(" Property \"" + s + "\" default value is now " + aFloat + ". This change wont affect already joined players!");
    }

    @Override
    public boolean updatingRequiresDamageRecalc(int propertyId) {
        return damageRecalc.contains(propertyId);
    }

    @Override
    public void addPropertyToRequiresDamageRecalc(int i) {
        damageRecalc.add(i);
    }

    @Override
    public void loadMaximalServerPropertyValues(Path path) {
        maxValues = new float[LAST_ID];
        for (int i = 0; i < maxValues.length; i++) {
            maxValues[i] = Float.MAX_VALUE;
        }
        File file = path.toFile();
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Set<String> missing = new HashSet<>();
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            Properties properties = new Properties();
            properties.load(fileInputStream);
            for (String s : idMap.keySet()) {
                Object o = properties.get(s);
                if (o == null) {
                    missing.add(s);
                    Log.info("Missing property \"" + Console.GREEN + s + Console.RESET + "\" in the file max_server_property_values.properties");
                    Log.info(" - Appending the file and setting its default value to 1000; You might want to reconfigure that file.");
                    maxValues[getIdByName(s)] = 1000f;
                } else {
                    maxValues[getIdByName(s)] = Float.parseFloat(o.toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!missing.isEmpty()) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                for (String a : missing) {
                    writer.write(a + "=1000" + System.lineSeparator());
                }
            } catch (IOException e) {
                Log.error("Could not append file max_server_property_values.properties", e);
            }
        }
    }

    @Override
    public Optional<Attribute> getAttributeById(String attribute) {
        return Optional.ofNullable(getAttributes().get(attribute));
    }

}
