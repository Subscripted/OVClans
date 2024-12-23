package dev.subscripted.eloriseClans.utils;

import dev.subscripted.eloriseClans.Main;
import lombok.SneakyThrows;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class SmartConfig {

    private final File file;
    private final YamlConfiguration config;

    public SmartConfig(String fileName) {
        this.file = new File(Main.getInstance().getDataFolder(), fileName);

        if (file.exists()) {
            System.out.println("This configuration file already exists: " + fileName);
        } else {
            try {
                file.getParentFile().mkdirs();
                if (file.createNewFile()) {
                    System.out.println("New configuration file created: " + fileName);
                }
            } catch (IOException e) {
                System.err.println("File could not be created: " + fileName);
                e.printStackTrace();
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public static SmartConfig load(String fileName) {
        return new SmartConfig(fileName);
    }

    @SneakyThrows
    private boolean pathExistsAndValueEqual(String path, Object value) {
        if (!config.contains(path)) {
            return false;
        }
        Object currentValue = config.get(path);
        return currentValue != null && currentValue.equals(value);
    }

    @SneakyThrows
    public void setString(String path, String value) {
        if (!pathExistsAndValueEqual(path, value)) {
            if (!config.contains(path)) {
                config.set(path, value);
                config.save(file);
                System.out.println("New value saved to: " + path + " = " + value);
            }
        }
    }

    @SneakyThrows
    public void setInt(String path, int value) {
        if (!pathExistsAndValueEqual(path, value)) {
            if (!config.contains(path)) {
                config.set(path, value);
                config.save(file);
                System.out.println("New value saved to: " + path + " = " + value);
            }
        }
    }

    @SneakyThrows
    public void setBool(String path, boolean value) {
        if (!pathExistsAndValueEqual(path, value)) {
            if (!config.contains(path)) {
                config.set(path, value);
                config.save(file);
                System.out.println("New value saved to: " + path + " = " + value);
            }
        }
    }

    @SneakyThrows
    public void addList(String path, List<?> list) {
        if (!pathExistsAndValueEqual(path, list)) {
            if (!config.contains(path)) {
                config.set(path, list);
                config.save(file);
                System.out.println("New value saved to: " + path + " = " + list);
            }
        }
    }

    @SneakyThrows
    public void clearPath(String path) {
        if (config.contains(path)) {
            config.set(path, null);
            config.save(file);
            System.out.println("Path deleted: " + path);
        }
    }

    public void save() {
        try {
            config.save(file);
            System.out.println("Configuration saved: " + file.getName());
        } catch (IOException e) {
            System.err.println("Error / Warning while trying to save the config: " + file.getName());
            e.printStackTrace();
        }
    }

    public String getString(String path) {
        return config.getString(path);
    }

    public int getInt(String path) {
        return config.getInt(path);
    }

    public boolean getBool(String path) {
        return config.getBoolean(path);
    }

    public List<?> getList(String path) {
        return config.getList(path);
    }

    @SneakyThrows
    public <T> void set(String path, T value) {
        if (!pathExistsAndValueEqual(path, value)) {
            if (!config.contains(path)) {
                config.set(path, value);
                config.save(file);
                System.out.println("New value saved to: " + path + " = " + value);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String path, Class<T> type) {
        Object value = config.get(path);
        if (value == null) return null;
        if (type.isInstance(value)) {
            return (T) value;
        }
        throw new IllegalArgumentException("Value at path \"" + path + "\" is not of type " + type.getName());
    }
}
