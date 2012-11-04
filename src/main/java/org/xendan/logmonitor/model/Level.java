package org.xendan.logmonitor.model;

public enum Level {
    WARN,
    UNKNOWN;

    public static Level fromString(String name) {
        for (Level level : Level.values()) {
           if (level.toString().equals(name)) {
               return level;
           } 
        }
        return UNKNOWN;
    }

}
