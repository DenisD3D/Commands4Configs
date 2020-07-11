package ml.denisd3d.commands4configs;

import java.text.NumberFormat;
import java.util.HashMap;

public class Types
{
    public static HashMap<Class<?>, ThrowingFunction<String, Object>> types;

    static {
        types = new HashMap<>();
        types.put(Double.class, s -> NumberFormat.getInstance().parse(s));
        types.put(Integer.class, s -> NumberFormat.getInstance().parse(s));
        types.put(Float.class, s -> NumberFormat.getInstance().parse(s));
        types.put(Long.class, s -> NumberFormat.getInstance().parse(s));
        types.put(String.class, s -> s);
    }
}
