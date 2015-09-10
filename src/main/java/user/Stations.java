package user;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class Stations {

    private static final List<Integer> ids =
            asList(9704, 9325, 9510, 9000, 9530, 9531, 9529, 9528, 9527, 9526, 9525, 9524);

    static List<String> getStations() {
        return ids.stream().map(Object::toString).collect(Collectors.toList());
    }

    public static String south(String north) {
        List<String> stations = getStations();
        int index = stations.indexOf(north);
        if (index != -1 && index != stations.size() - 1)
            return stations.get(index + 1);
        else
            return "" + (Integer.valueOf(north) - 1);
    }

    public static String north(String south) {
        List<String> stations = getStations();
        int index = stations.indexOf(south);
        if (index > 0)
            return stations.get(index - 1);
        else
            return "" + (Integer.valueOf(south) + 1);
    }
}
