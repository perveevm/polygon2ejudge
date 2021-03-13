package ru.strategy48.ejudge.polygon2ejudge.polygon;

import org.json.JSONObject;
import ru.strategy48.ejudge.polygon2ejudge.polygon.objects.Package;
import ru.strategy48.ejudge.polygon2ejudge.polygon.objects.PackageState;
import ru.strategy48.ejudge.polygon2ejudge.polygon.objects.Problem;

/**
 * @author Perveev Mike (perveev_m@mail.ru)
 * Provides methods for converting JSON to Polygon objects
 */
public class JSONUtils {
    public static Package packageFromJSON(final JSONObject json) {
        return new Package(json.getInt("id"), json.getInt("revision"), json.getInt("creationTimeSeconds"), json.getEnum(PackageState.class, "state"), json.getString("comment"));
    }

    public static Problem problemFromJSON(final JSONObject json) {
        return new Problem(json.getInt("id"), json.getString("owner"), json.getString("name"), json.getBoolean("deleted"), json.getBoolean("favourite"), json.getInt("revision"), json.getInt("latestPackage"), json.getBoolean("modified"));
    }
}
