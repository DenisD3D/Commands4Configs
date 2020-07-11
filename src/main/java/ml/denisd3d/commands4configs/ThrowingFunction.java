package ml.denisd3d.commands4configs;

import java.text.ParseException;

public interface ThrowingFunction<T, R>
{
    R apply(T t) throws ParseException;
}
