package dev.subscripted.eloriseClans.utils;

public class ClanLevelModifier {
    /**
     * Berechnet den Schadensmultiplikator basierend auf dem Clan-Level.
     *
     * @param clanLevel Der Clan-Level des Angreifers
     * @return Der Schadensmultiplikator (z.B. 1.05 für 5% mehr Schaden)
     */
    public static double getDamageMultiplier(int clanLevel) {

        return switch (clanLevel) {
            case 10 -> 5.0;
            case 9 -> 4.5;
            case 8 -> 4.0;
            case 7 -> 3.8;
            case 6 -> 3.6;
            case 5 -> 1.80;
            case 4 -> 1.40;
            case 3 -> 1.20;
            case 2 -> 1.10;
            case 1 -> 1.05;
            default -> 1.00;
        };

    }
}
