package antifraud.domain;

import antifraud.exception.InvalidRegionException;

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

    public String getDescription() {
        return description;
    }

    public static Region toRegion(String region) throws InvalidRegionException {
        switch (region) {
            case "SA":
                return SA;
            case "EAP":
                return EAP;
            case "ECA":
                return ECA;
            case "HIC":
                return HIC;
            case "LAC":
                return LAC;
            case "SSA":
                return SSA;
            case "MENA":
                return MENA;
            default:
                throw new InvalidRegionException("Region is invalid!");
        }
    }
}
