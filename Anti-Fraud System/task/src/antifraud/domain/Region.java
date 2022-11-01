package antifraud.domain;

import antifraud.exception.InvalidRegionException;

import java.util.Arrays;

public enum Region {

    EAP("East Asia and Pacific"),
    ECA("Europe and Central Asia"),
    HIC("High-Income countries"),
    LAC("Latin America and the Caribbean"),
    MENA("The Middle East and North Africa"),
    SA("South Asia"),
    SSA("Sub-Saharan Africa");

    private final String description;

    Region(String description) {
        this.description = description;
    }

    public static Region toRegion(String region) throws InvalidRegionException {
        return Arrays.stream(Region.values())
                .filter(value -> value.name().equals(region))
                .findFirst()
                .orElseThrow(() -> new InvalidRegionException("Region is invalid!"));
    }

    @Override
    public String toString() {
        return description;
    }
}
