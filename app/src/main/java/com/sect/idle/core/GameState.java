package com.sect.idle.core;

public enum GameState {
    LOADING(0), MENU(1), SECT(2), DISCIPLES(3), MARKET(4),
    BATTLE(5), STORY(6), RECRUIT(7), INVENTORY(8), BUILDING(9),
    TOURNAMENT(10), MAP(11), SETTINGS(12), DIALOG(13), CUTSCENE(14);

    public final int id;
    private static final GameState[] VALUES = values();
    private static final GameState[] ID_MAP = new GameState[16];

    static {
        for (int i = 0; i < ID_MAP.length; i++) ID_MAP[i] = MENU;
        for (GameState s : VALUES) {
            if (s.id >= 0 && s.id < ID_MAP.length) ID_MAP[s.id] = s;
        }
    }

    GameState(int id) { this.id = id; }

    public static GameState fromId(int id) {
        return (id >= 0 && id < ID_MAP.length) ? ID_MAP[id] : MENU;
    }
}
