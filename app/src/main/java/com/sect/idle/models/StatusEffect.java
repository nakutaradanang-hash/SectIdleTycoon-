package com.sect.idle.models;

public class StatusEffect {
    public String name;
    public int type; // 0=buff, 1=debuff, 2=dot, 3=hot
    public int duration;
    public int power;
    public int statTarget; // -1=all, 0=str, 1=agi, etc
    public String iconId;
    
    public StatusEffect(String name, int type, int duration, int power, int stat) {
        this.name = name; this.type = type; this.duration = duration;
        this.power = power; this.statTarget = stat;
    }
}
