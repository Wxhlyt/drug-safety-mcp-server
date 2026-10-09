package com.drugsafety.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** The single server-side definition of the 30 FRDB classroom demonstration drugs. */
public final class DemoDrugScope {

    private static final Map<Long, Long> DRUG_TO_COMPOUND;

    static {
        Map<Long, Long> pairs = new LinkedHashMap<>();
        long[][] values = {
                {3030, 10052}, {2478, 7490}, {26, 21}, {2643, 8172}, {2900, 9171},
                {1087, 1574}, {623, 1085}, {2702, 8257}, {3180, 10298}, {678, 1143},
                {2065, 2998}, {1965, 2827}, {2594, 8058}, {2669, 8204}, {2605, 8074},
                {61, 65}, {2608, 8079}, {1765, 2498}, {717, 1185}, {1587, 2306},
                {1589, 2309}, {1590, 2310}, {631, 1094}, {1505, 2218}, {1547, 2264},
                {1550, 2268}, {2584, 8040}, {3041, 10066}, {1683, 2408}, {2344, 6717}
        };
        for (long[] pair : values) pairs.put(pair[0], pair[1]);
        DRUG_TO_COMPOUND = Map.copyOf(pairs);
    }

    private DemoDrugScope() {}

    public static List<Long> drugIds() {
        return List.copyOf(DRUG_TO_COMPOUND.keySet());
    }

    public static Long expectedCompound(long drugId) {
        return DRUG_TO_COMPOUND.get(drugId);
    }
}
