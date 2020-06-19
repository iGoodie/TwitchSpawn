package net.programmer.igoodie.twitchspawn.util;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class CooldownBucket {

    public int globalCooldownMillis;
    public int cooldownMillis;

    public long globalCooldownUntil;
    public Map<String, Long> individualCooldownUntil;

    public CooldownBucket(int globalCooldown, int individualCooldown) {
        this.cooldownMillis = individualCooldown;
        this.globalCooldownMillis = globalCooldown;

        this.globalCooldownUntil = now();
        this.individualCooldownUntil = new HashMap<>();
    }

    public float getGlobalCooldown() {
        long now = now();
        return Math.max(0, (globalCooldownUntil - now) / 1000f);
    }

    public long getGlobalCooldownTimestamp() {
        return globalCooldownUntil;
    }

    public boolean hasGlobalCooldown() {
        if (globalCooldownMillis == 0) return false;
        long now = now();
        return now <= globalCooldownUntil;
    }

    public boolean hasCooldown(String nickname) {
        long now = now();
        Long nextAvailableTime = individualCooldownUntil.get(nickname);
        return nextAvailableTime != null && now <= nextAvailableTime;
    }

    public long getCooldown(String nickname) {
        return individualCooldownUntil.get(nickname) - now();
    }

    public boolean canConsume(String nickname) {
        return !hasGlobalCooldown() && !hasCooldown(nickname);
    }

    public void consume(String nickname) {
        long now = now();
        globalCooldownUntil = now + globalCooldownMillis;
        individualCooldownUntil.put(nickname, now + cooldownMillis);
    }

    public static long now() {
        return Instant.now().getEpochSecond() * 1000;
    }

}
