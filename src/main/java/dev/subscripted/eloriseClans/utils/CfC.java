package dev.subscripted.eloriseClans.utils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CfC {

    static SmartConfig config;

    public static void createSomeDefaults() {
        config = new SmartConfig("mysql.yml");
        config = new SmartConfig("messages.yml");
    }

    public static void setSomeDefaults(){
        config = SmartConfig.load("mysql.yml");
        config.setString("host", "localhost");
        config.setInt("port", 3306);
        config.setString("password", "");
        config.setString("username", "root");
        config.setString("database", "survival");
    }
}
