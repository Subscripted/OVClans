package dev.subscripted.eloriseClans.utils;

import lombok.Getter;
import org.bukkit.Sound;

@Getter
public enum CustomSound {

    SUCCESSFULL(Sound.ENTITY_PLAYER_LEVELUP),
    NOT_ALLOWED(Sound.ENTITY_VILLAGER_NO),
    NO_PERMISSION(Sound.BLOCK_ANVIL_PLACE),
    ACTIVATED(Sound.BLOCK_BEACON_ACTIVATE),
    DEACTIVATED(Sound.BLOCK_BEACON_DEACTIVATE),
    GUI_SOUND(Sound.UI_BUTTON_CLICK),
    WRONG_USAGE(Sound.UI_TOAST_IN),
    QUESTION(Sound.ENTITY_VILLAGER_TRADE),
    GLASS_GUI_BUILD(Sound.BLOCK_GLASS_PLACE),
    LOADING_FINISHED(Sound.BLOCK_LAVA_EXTINGUISH),
    LOBBY_HIDER_SWITCH(Sound.ENTITY_ARROW_HIT),
    GUI_OPEN(Sound.BLOCK_CHEST_OPEN),
    WARNING(Sound.ENTITY_WITHER_AMBIENT),
    CLAN_OPEN(Sound.ITEM_GOAT_HORN_SOUND_7),
    PAGE_TURN(Sound.ITEM_BOOK_PAGE_TURN),
    UPGRADING(Sound.UI_TOAST_CHALLENGE_COMPLETE),

    BELL(Sound.BLOCK_BELL_USE),
    BELL2(Sound.BLOCK_BELL_RESONATE),
    TEST(Sound.BLOCK_RESPAWN_ANCHOR_CHARGE),

    MEMORY(Sound.BLOCK_PORTAL_TRIGGER),
    MEMORY2(Sound.BLOCK_END_PORTAL_FRAME_FILL),
    MEMORY3(Sound.BLOCK_END_PORTAL_SPAWN),

    DEATH(Sound.ENTITY_WITHER_DEATH);

    @Getter
    private Sound sound;

    CustomSound(Sound sound) {
        this.sound = sound;
    }
}

