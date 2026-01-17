package ro.pub.cs.systems.eim.colocviu_api;

public final class AutocompleteBroadcast {
    private AutocompleteBroadcast() {}

    public static final String ACTION_AUTOCOMPLETE_READY =
            "ro.pub.cs.systems.eim.colocviu_api.ACTION_AUTOCOMPLETE_READY";

    public static final String EXTRA_PREFIX = "extra_prefix";
    public static final String EXTRA_RAW = "extra_raw";
    public static final String EXTRA_THIRD = "extra_third";
    public static final String EXTRA_SUGGESTIONS_JOINED = "extra_suggestions_joined"; // ",\n"
}
