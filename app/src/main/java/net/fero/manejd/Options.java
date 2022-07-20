package net.fero.manejd;

public enum Options {
    SETUP("Setup"),
    ADD("Add"),
    Get("Get");

    public final String label;

    Options(String get) {
        label = get;
    }
}
