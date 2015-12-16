package user;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

class Stations {

    private static final List<String> ids =
            asList("Spå", "Sub", "Ke", "Cst", "Sst", "Åbe", "Äs", "Sta", "Hu", "Flb", "Tul", "Tu");

    static List<String> getStations() {
        return ids.stream().map(Object::toString).collect(Collectors.toList());
    }

    static List<String> getSouthwestStations() {
        return ids.subList(7, 12).stream().map(Object::toString).collect(Collectors.toList());
    }

    static String south(String north) {
        List<String> stations = getStations();
        int index = stations.indexOf(north);
        if (index != -1 && index != stations.size() - 1)
            return stations.get(index + 1);
        else
            return "" + (Integer.valueOf(north) - 1);
    }

    static String north(String south) {
        List<String> stations = getStations();
        int index = stations.indexOf(south);
        if (index > 0)
            return stations.get(index - 1);
        else
            return "" + (Integer.valueOf(south) + 1);
    }
}
