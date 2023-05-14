package com.studiomediatech.utils;

/**
 *
 * Human-oriented base-32 encoding implementation for Java.
 *
 * http://philzimmermann.com/docs/human-oriented-base-32-encoding.txt
 *
 * Ported from the JavaScript implementation at https://github.com/cryptii/cryptii with notice, as per the original MIT
 * license below - also retained here.
 *
 * Copyright © 2023 Olle Törnström
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the “Software”), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * ------------------------[NOTICE]------------------------
 *
 * Copyright © 2021 Fränz Friederes
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
public class ZBase32 {

    private static final char[] ALPHABET = "ybndrfg8ejkmcpqxot1uwisza345h769".toCharArray();
    private static final int BITWIDTH = 5;

    /**
     * {@return The z-base32 encoded string for the given string value.}
     *
     * @param value
     *            to encode.
     */
    public static String encode(String value) {

        byte[] input = value.getBytes();
        var result = new StringBuilder();

        int shift = 3;
        int carry = 0;

        for (int i = 0; i < input.length; i++) {

            byte b = input[i];
            int index = carry | (b >> shift);
            result.append(ALPHABET[index & 0x1f]);

            if (shift > BITWIDTH) {
                shift -= BITWIDTH;
                index = (b >> shift);
                result.append(ALPHABET[index & 0x1f]);
            }

            shift = BITWIDTH - shift;
            carry = b << shift;
            shift = 8 - shift;
        }

        if (shift != 3) {
            result.append(ALPHABET[carry & 0x1f]);
        }

        return result.toString();
    }

    /**
     * {@return The decoded string from the given string value. }
     *
     * @param value
     *            to decode
     */
    public static String decode(String value) {

        char[] input = value.toCharArray();
        StringBuilder result = new StringBuilder();

        int shift = 8;
        int carry = 0;

        for (int i = 0; i < input.length; i++) {
            char codePoint = input[i];

            int index = scanTo(codePoint, i) & 0xff;

            shift -= BITWIDTH;
            if (shift > 0) {
                carry |= index << shift;
            } else if (shift < 0) {
                result.append((char) (carry | (index >> -shift)));
                shift += 8;
                carry = (index << shift) & 0xff;
            } else {
                result.append((char) (carry | index));
                shift = 8;
                carry = 0;
            }
        }

        return result.toString();
    }

    private static int scanTo(char codePoint, int index) {

        for (int i = 0; i < ALPHABET.length; i++) {
            if (ALPHABET[i] == codePoint) {
                return i;
            }
        }

        throw new IllegalStateException("Unexpected code point '%s' at index %d".formatted(codePoint, index));
    }
}
