package eu.revamp.practice.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ListUtil {

    public static List<UUID> newList(UUID... uuids) {
        return new ArrayList<>(Arrays.asList(uuids));
    }
}
