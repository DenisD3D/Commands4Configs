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
        types.put(Short.class, s -> NumberFormat.getInstance().parse(s));
        types.put(Long.class, s -> NumberFormat.getInstance().parse(s));
        types.put(Boolean.class, Boolean::parseBoolean);
        types.put(Byte.class, Byte::parseByte);
        types.put(Character.class, s-> s.charAt(0));
        types.put(String.class, s -> s);
    }
}
