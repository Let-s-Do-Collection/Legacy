package net.satisfy.legacy.core.title;

public enum TitleForm {
    MASCULINE,
    FEMININE;

    private static final TitleForm[] VALUES = values();

    public static TitleForm byOrdinal(int ordinal) {
        return ordinal >= 0 && ordinal < VALUES.length ? VALUES[ordinal] : MASCULINE;
    }

    public TitleForm toggled() {
        return this == MASCULINE ? FEMININE : MASCULINE;
    }

    public String suffix() {
        return this == FEMININE ? ".female" : ".male";
    }
}
