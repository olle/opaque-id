package com.studiomediatech.utils;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Human-oriented base-32 encoding implementation for Java.
 * http://philzimmermann.com/docs/human-oriented-base-32-encoding.txt Ported from the JavaScript implementation at
 * https://github.com/cryptii/cryptii with notice, as per the original MIT license below - also retained here. Copyright
 * © 2023 Olle Törnström Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the “Software”), to deal in the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The
 * above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software. THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE. ------------------------[NOTICE]------------------------ Copyright © 2021 Fränz Friederes Permission is
 * hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions: The above copyright notice and this permission
 * notice shall be included in all copies or substantial portions of the Software. THE SOFTWARE IS PROVIDED "AS IS",
 * WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF
 * OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
public final class ZBase32 {

    private static final String ALPHABET = "ybndrfg8ejkmcpqxot1uwisza345h769";
    private static final int BITWIDTH = 5;
    private static final int MASK = 0x1f;

    private ZBase32() {
        // Hidden
    }

    /**
     * {@return The z-base32 encoded string for the given string value.}
     *
     * @param value
     *            to encode.
     */
    public static String encode(String value) {

        Objects.requireNonNull(value, "value");

        byte[] input = value.getBytes(StandardCharsets.UTF_8);
        var result = new StringBuilder((input.length * 8 + 4) / BITWIDTH);

        int shift = 3;
        int carry = 0;

        for (byte b : input) {

            int octet = b & 0xff;
            int index = carry | (octet >> shift);
            result.append(ALPHABET.charAt(index & MASK));

            if (shift > BITWIDTH) {
                shift -= BITWIDTH;
                index = (octet >> shift);
                result.append(ALPHABET.charAt(index & MASK));
            }

            shift = BITWIDTH - shift;
            carry = octet << shift;
            shift = 8 - shift;
        }

        if (shift != 3) {
            result.append(ALPHABET.charAt(carry & MASK));
        }

        return result.toString();
    }

    /**
     * {@return The decoded string from the given string value. }
     *
     * @param value
     *            to decode
     *
     * @throws IllegalArgumentException
     *             if the value contains characters outside the z-base32 alphabet.
     */
    public static String decode(String value) {

        Objects.requireNonNull(value, "value");

        var result = new ByteArrayOutputStream(value.length() * BITWIDTH / 8);

        int shift = 8;
        int carry = 0;

        for (int i = 0; i < value.length(); i++) {

            int index = scanTo(value.charAt(i), i);

            shift -= BITWIDTH;

            if (shift > 0) {
                carry |= index << shift;
            } else if (shift < 0) {
                result.write(carry | (index >> -shift));
                shift += 8;
                carry = (index << shift) & 0xff;
            } else {
                result.write(carry | index);
                shift = 8;
                carry = 0;
            }
        }

        return result.toString(StandardCharsets.UTF_8);
    }

    private static int scanTo(char codePoint, int index) {

        int alphabetIndex = ALPHABET.indexOf(codePoint);

        if (alphabetIndex < 0) {
            throw new IllegalArgumentException("Unexpected code point '%s' at index %d".formatted(codePoint, index));
        }

        return alphabetIndex;
    }

}
