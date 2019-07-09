package Components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class MergedState {
    public int time;
    public int[] offsets;
    public String id;
    public MergedState prev = null;

    public MergedState(int time, int[] offsets) {
        this.time = time;
        this.offsets = offsets;
        ArrayList<String> strings = new ArrayList<>();
        for (Integer i : offsets) strings.add(String.valueOf(i));
        this.id = time+":"+String.join(" ", strings);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MergedState)) return false;
        MergedState that = (MergedState) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id;
    }
}
