package net.swordie.ms.flag;

import net.swordie.ms.util.MovementHistory;

import java.util.List;

public class Ghost implements Comparable<Ghost> {
    public Long time;
    public List<MovementHistory> history;
    public Integer id;

    @Override
    public int compareTo(Ghost o) {
        if (time == o.time) return 0;
        return time > o.time ? 1 : -1;
    }
}
