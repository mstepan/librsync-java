package org.max.rsync.wire;

public enum EntityType {

    NEW(true),
    EXISTING(false);

    private final boolean flag;

    EntityType(boolean flag) {
        this.flag = flag;
    }

    public EntityType fromFlag(boolean flag) {
        return flag ? NEW : EXISTING;
    }
}
