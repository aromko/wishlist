package aromko.de.wishlist.model;

import java.util.HashMap;
import java.util.Map;

public class Lists {
    private int counterWishes;
    private int members;
    private String name;

    public int getCounterWishes() {
        return counterWishes;
    }

    public void setCounterWishes(int counterWishes) {
        this.counterWishes = counterWishes;
    }

    public int getMembers() {
        return members;
    }

    public void setMembers(int members) {
        this.members = members;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Lists() {
    }

    public Lists(int counterWishes, int members, String name) {
        this.counterWishes = counterWishes;
        this.members = members;
        this.name = name;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("counterWishes", counterWishes);
        result.put("members", members);
        result.put("name", name);

        return result;
    }


    @Override
    public String toString() {
        return "Lists{" +
                "counterWishes=" + counterWishes +
                ", members=" + members +
                ", name='" + name + '\'' +
                '}';
    }
}
