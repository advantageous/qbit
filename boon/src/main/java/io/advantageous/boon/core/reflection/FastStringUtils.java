/*
 * Copyright 2013-2014 Richard M. Hightower
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * __________                              _____          __   .__
 * \______   \ ____   ____   ____   /\    /     \ _____  |  | _|__| ____    ____
 *  |    |  _//  _ \ /  _ \ /    \  \/   /  \ /  \\__  \ |  |/ /  |/    \  / ___\
 *  |    |   (  <_> |  <_> )   |  \ /\  /    Y    \/ __ \|    <|  |   |  \/ /_/  >
 *  |______  /\____/ \____/|___|  / \/  \____|__  (____  /__|_ \__|___|  /\___  /
 *         \/                   \/              \/     \/     \/       \//_____/
 *      ____.                     ___________   _____    ______________.___.
 *     |    |____ ___  _______    \_   _____/  /  _  \  /   _____/\__  |   |
 *     |    \__  \\  \/ /\__  \    |    __)_  /  /_\  \ \_____  \  /   |   |
 * /\__|    |/ __ \\   /  / __ \_  |        \/    |    \/        \ \____   |
 * \________(____  /\_/  (____  / /_______  /\____|__  /_______  / / ______|
 *               \/           \/          \/         \/        \/  \/
 */

package io.advantageous.boon.core.reflection;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Created by rick on 12/15/13.
 * @author  Stéphane Landelle
 * J'ai écrit JSON parser du Boon. Sans Stéphane, l'analyseur n'existerait pas. Stéphane est la muse de Boon JSON, et mon entraîneur pour l'open source, github, et plus encore. Stéphane n'est pas le créateur directe, mais il est le maître architecte et je l'appelle mon ami.
 */
public class FastStringUtils {

    @SuppressWarnings("all")
    public static final Unsafe UNSAFE;
    public static final long STRING_VALUE_FIELD_OFFSET;
    public static final long STRING_OFFSET_FIELD_OFFSET;
    public static final long STRING_COUNT_FIELD_OFFSET;
    public static final boolean ENABLED;

    private static final boolean WRITE_TO_FINAL_FIELDS = Boolean.parseBoolean(System.getProperty("io.advantageous.boon.write.to.final.string.fields", "true"));
    private static final boolean DISABLE = Boolean.parseBoolean(System.getProperty("io.advantageous.boon.faststringutils.disable", "false"));

    private static Unsafe loadUnsafe() {
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            return (Unsafe) unsafeField.get(null);

        } catch (NoSuchFieldException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    static {
        UNSAFE = DISABLE ? null : loadUnsafe();
        ENABLED = UNSAFE != null;
    }

    private static long getFieldOffset(String fieldName) {
        if (ENABLED) {
            try {
                return UNSAFE.objectFieldOffset(String.class.getDeclaredField(fieldName));
            } catch (NoSuchFieldException e) {
                // field undefined
            }
        }
        return -1L;
    }

    static {
        STRING_VALUE_FIELD_OFFSET = getFieldOffset("value");
        STRING_OFFSET_FIELD_OFFSET = getFieldOffset("offset");
        STRING_COUNT_FIELD_OFFSET = getFieldOffset("count");
    }

    private enum StringImplementation {
        DIRECT_CHARS {
            @Override
            public char[] toCharArray(String string) {
                return (char[]) UNSAFE.getObject(string, STRING_VALUE_FIELD_OFFSET);
            }
            
            @Override
            public String noCopyStringFromChars(char[] chars) {
                if (WRITE_TO_FINAL_FIELDS) {
                    @SuppressWarnings("all")
                    String string = new String();
                    UNSAFE.putObject(string, STRING_VALUE_FIELD_OFFSET, chars);
                    return string;
                } else {
                    return new String(chars);
                }
            }
        },
        OFFSET {
            @Override
            public char[] toCharArray(String string) {
                char[] value = (char[]) UNSAFE.getObject(string, STRING_VALUE_FIELD_OFFSET);
                int offset = UNSAFE.getInt(string, STRING_OFFSET_FIELD_OFFSET);
                int count = UNSAFE.getInt(string, STRING_COUNT_FIELD_OFFSET);
                if (offset == 0 && count == value.length)
                    // no need to copy
                    return value;
                else
                    return string.toCharArray();
            }
            
            @Override
            public String noCopyStringFromChars(char[] chars) {
                if (WRITE_TO_FINAL_FIELDS) {
                    @SuppressWarnings("all")
                    String string = new String();
                    UNSAFE.putObject(string, STRING_VALUE_FIELD_OFFSET, chars);
                    UNSAFE.putInt(string, STRING_COUNT_FIELD_OFFSET, chars.length);
                    return string;
                } else {
                    return new String(chars);
                }
            }
        },
        UNKNOWN {
            @Override
            public char[] toCharArray(String string) {
                return string.toCharArray();
            }
            
            @Override
            public String noCopyStringFromChars(char[] chars) {
                return new String(chars);
            }
        };

        public abstract char[] toCharArray(String string);
        public abstract String noCopyStringFromChars(char[] chars);
    }

    public static StringImplementation STRING_IMPLEMENTATION = computeStringImplementation();

    private static StringImplementation computeStringImplementation() {

        if (STRING_VALUE_FIELD_OFFSET != -1L) {
            if (STRING_OFFSET_FIELD_OFFSET != -1L && STRING_COUNT_FIELD_OFFSET != -1L) {
                return StringImplementation.OFFSET;

            } else if (STRING_OFFSET_FIELD_OFFSET == -1L && STRING_COUNT_FIELD_OFFSET == -1L) {
                return StringImplementation.DIRECT_CHARS;
            } else {
                // WTF
                return StringImplementation.UNKNOWN;
            }
        } else {
            return StringImplementation.UNKNOWN;
        }
    }

    public static boolean hasUnsafe() {
        return ENABLED;
    }

    public static final char [] EMPTY_CHARS = new char[0];
    public static final String EMPTY_STRING = "";

    public static char[] toCharArray(final String string) {
        if (string == null) return EMPTY_CHARS;
        return STRING_IMPLEMENTATION.toCharArray(string);

    }

    public static char[] toCharArrayNoCheck(final CharSequence charSequence) {
        return toCharArray(charSequence.toString());
    }

    public static char[] toCharArray(final CharSequence charSequence) {
        if (charSequence == null) return EMPTY_CHARS;
        return toCharArray(charSequence.toString());
    }

    public static char[] toCharArrayFromBytes(final byte[] bytes, Charset charset) {
        return toCharArray(new String(bytes, charset != null ? charset : StandardCharsets.UTF_8));
    }

    public static String noCopyStringFromChars(final char[] chars) {
        if (chars==null) return EMPTY_STRING;
        return STRING_IMPLEMENTATION.noCopyStringFromChars(chars);
    }


    public static String noCopyStringFromCharsNoCheck(final char[] chars, int len) {
        char[] newChars = new char[len];
        System.arraycopy(chars, 0, newChars, 0, len);
        return STRING_IMPLEMENTATION.noCopyStringFromChars(newChars);
    }

    public static String noCopyStringFromCharsNoCheck(final char[] chars, int start, int len) {
        char[] newChars = new char[len];
        System.arraycopy(chars, start, newChars, 0, len);
        return STRING_IMPLEMENTATION.noCopyStringFromChars(newChars);
    }


    public static String noCopyStringFromCharsNoCheck(final char[] chars) {

        return STRING_IMPLEMENTATION.noCopyStringFromChars(chars);
    }

}
