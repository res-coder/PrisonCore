package com.seandoyle.prisoncore.model;

public final class Progression {
    private Progression() {}
    public static double xpRequired(int level, double base, double exponent) {
        if (level < 1 || base <= 0 || exponent <= 0) throw new IllegalArgumentException("Invalid progression input");
        return Math.max(1.0, Math.floor(base * Math.pow(level, exponent)));
    }
    public static long upgradeCost(long base, double multiplier, int currentLevel) {
        if (base < 0 || multiplier < 1.0 || currentLevel < 0) throw new IllegalArgumentException("Invalid cost input");
        double value = base * Math.pow(multiplier, currentLevel);
        return value >= Long.MAX_VALUE ? Long.MAX_VALUE : Math.max(0L, Math.round(value));
    }
}
