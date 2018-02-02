package it.innove;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Iterator;
import java.util.Set;

public class Utils
{
    private Utils()
    {
    }

    /**
     * Tests if a String value is null or empty.
     *
     * @param value the String value to test
     * @return true if the String is null, zero length, or ""
     */
    public static boolean isNullOrEmpty(String value)
    {
        return (value == null || value.length() == 0 || value.equals(""));
    }

    public static boolean isNullOrEmpty(CharSequence value)
    {
        return (value == null || value.length() == 0 || value.equals(""));
    }

    public static boolean isNullOrEmpty(Object object)
    {
        return object == null || isNullOrEmpty(object.toString());
    }

    @NonNull
    public static String getShortClassName(String className)
    {
        if (isNullOrEmpty(className))
        {
            return "null";
        }
        final int dot = className.lastIndexOf(".");
        if (dot > 0)
        {
            className = className.substring(dot + 1); // strip the package name
        }
        return className;
    }

    @Nullable
    public static String getShortClassName(Object o)
    {
        return getShortClassName(o == null ? null : o.getClass());
    }

    @Nullable
    public static String getShortClassName(Class c)
    {
        return c == null ? null : c.getSimpleName();
    }

    /**
     * Identical to {@link #repr}, but grammatically intended for Strings.
     *
     * @param value value
     * @return "null", or '\"' + value.toString + '\"', or value.toString()
     */
    public static String quote(Object value)
    {
        return repr(value, false);
    }

    /**
     * Identical to {@link #quote}, but grammatically intended for Objects.
     *
     * @param value value
     * @return "null", or '\"' + value.toString + '\"', or value.toString()
     */
    public static String repr(Object value)
    {
        return repr(value, false);
    }

    /**
     * @param value    value
     * @param typeOnly typeOnly
     * @return "null", or '\"' + value.toString + '\"', or value.toString(), or getShortClassName(value)
     */
    public static String repr(Object value, boolean typeOnly)
    {
        if (value == null)
        {
            return "null";
        }

        if (value instanceof String)
        {
            return '\"' + value.toString() + '\"';
        }

        if (typeOnly)
        {
            return getShortClassName(value);
        }

        if (value instanceof Object[])
        {
            return toString((Object[]) value);
        }

        return value.toString();
    }

    /*
    public static String toString(Object value)
    {
        if (value == null)
        {
            return "null";
        }

        if (value instanceof Object[])
        {
            return toString((Object[]) value);
        }

        return value.toString();
    }
    */

    public static String toString(Object[] items)
    {
        StringBuilder sb = new StringBuilder();

        if (items == null)
        {
            sb.append("null");
        }
        else
        {
            sb.append('[');
            for (int i = 0; i < items.length; i++)
            {
                if (i != 0)
                {
                    sb.append(", ");
                }
                Object item = items[i];
                sb.append(quote(item));
            }
            sb.append(']');
        }

        return sb.toString();
    }

    public static String toString(Intent intent)
    {
        if (intent == null)
        {
            return "null";
        }

        StringBuilder sb = new StringBuilder();

        sb.append(intent.toString());

        Bundle bundle = intent.getExtras();
        sb.append(", extras=").append(toString(bundle));

        return sb.toString();
    }

    public static String toString(Bundle bundle)
    {
        if (bundle == null)
        {
            return "null";
        }

        StringBuilder sb = new StringBuilder();

        Set<String> keys = bundle.keySet();
        Iterator<String> it = keys.iterator();

        sb.append('{');
        while (it.hasNext())
        {
            String key = it.next();
            Object value = bundle.get(key);

            sb.append(quote(key)).append('=');

            if (key.toLowerCase().contains("password"))
            {
                value = "*CENSORED*";
            }

            if (value instanceof Bundle)
            {
                sb.append(toString((Bundle) value));
            }
            else if (value instanceof Intent)
            {
                sb.append(toString((Intent) value));
            }
            else
            {
                sb.append(quote(value));
            }

            if (it.hasNext())
            {
                sb.append(", ");
            }
        }
        sb.append('}');

        return sb.toString();
    }
}
