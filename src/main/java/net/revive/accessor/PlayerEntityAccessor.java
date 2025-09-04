package net.revive.accessor;

public interface PlayerEntityAccessor {

    public void setCanRevive(boolean canRevive, boolean outOfWorld, boolean supportiveRevival);

    public boolean canRevive();

    public boolean isOutOfWorld();

    public boolean isSupportiveRevival();

    public void setHandReviveTime(int time);

    public int getHandReviveTime();
}
