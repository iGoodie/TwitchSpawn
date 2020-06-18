package net.programmer.igoodie.twitchspawn.util;

import java.util.HashMap;
import java.util.Map;

public class CooldownBucket {

    // TODO: Extract to config
    private static final int DEFAULT_COOLDOWN = 1 * 10 * 1000;

    public int globalCooldownMillis;
    public int cooldownMillis;

    public long globalCooldownUntil;
    public Map<String, Long> individualCooldownUntil;

    public CooldownBucket() {
        this.cooldownMillis = DEFAULT_COOLDOWN;
        this.globalCooldownMillis = DEFAULT_COOLDOWN;

        this.globalCooldownUntil = System.currentTimeMillis();
        this.individualCooldownUntil = new HashMap<>();
    }

    public float getGlobalCooldown() {
        long now = System.currentTimeMillis();
        return (globalCooldownUntil - now) / 1000f;
    }

    public boolean hasGlobalCooldown() {
        if (globalCooldownMillis == -1) return false;
        long now = System.currentTimeMillis();
        return now <= globalCooldownUntil;
    }

    public boolean hasCooldown(String nickname) {
        long now = System.currentTimeMillis();
        Long nextAvailableTime = individualCooldownUntil.get(nickname);
        return nextAvailableTime != null && now <= nextAvailableTime;
    }

    public boolean canConsume(String nickname) {
        return !hasGlobalCooldown() && !hasCooldown(nickname);
    }

    public void consume(String nickname) {
        long now = System.currentTimeMillis();
        globalCooldownUntil = now + globalCooldownMillis;
        individualCooldownUntil.put(nickname, now + cooldownMillis);
    }

}
