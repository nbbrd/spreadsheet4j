/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package ec.util.spreadsheet.helpers;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;


/**
 *
 * @author Philippe Charles
 */
public final class CellRefHelper {

    private boolean match = false;
    private int col = 0;
    private int row = 0;

    private boolean withMatch(boolean condition) {
        return match = condition;
    }

    public boolean parseEnd(@Nullable CharSequence dimension) {
        if (dimension != null) {
            int length = dimension.length();
            for (int i = 0; i < length; i++) {
                if (dimension.charAt(i) == ':') {
                    return parse(dimension, i + 1, length);
                }
            }
        }
        return withMatch(false);
    }

    public boolean parse(@Nullable CharSequence ref) {
        return ref != null
                ? parse(ref, 0, ref.length())
                : withMatch(false);
    }

    private boolean parse(@NonNull CharSequence ref, int begIdx, int endIdx) {
        if (endIdx <= begIdx) {
            return withMatch(false);
        }

        int i = begIdx;

        col = 0;
        while (i < endIdx) {
            char c = ref.charAt(i);
            if (c < 'A' || c > 'Z') {
                break;
            }
            col = col * 26 + ((byte) c - (byte) 'A') + 1;
            i++;
        }
        col--;

        row = 0;
        while (i < endIdx) {
            char c = ref.charAt(i);
            if (c < '0' || c > '9') {
                break;
            }
            row = row * 10 + ((byte) c - (byte) '0');
            i++;
        }
        row--;

        return withMatch(i == endIdx && col >= 0 && row >= 0);
    }

    /**
     * Returns a zero-based column index.
     *
     * @return
     */
    @NonNegative
    public int getColumnIndex() throws IllegalStateException {
        checkMatch();
        return col;
    }

    /**
     * Returns a zero-based row index.
     *
     * @return
     */
    @NonNegative
    public int getRowIndex() throws IllegalStateException {
        checkMatch();
        return row;
    }

    private void checkMatch() throws IllegalStateException {
        if (!match) {
            throw new IllegalStateException();
        }
    }

    @NonNull
    public static String getRowLabel(@NonNegative int rowIndex) throws IndexOutOfBoundsException {
        checkNonNegative(rowIndex);
        return Integer.toString(rowIndex + 1);
    }

    @NonNull
    public static String getColumnLabel(@NonNegative int columnIndex) throws IndexOutOfBoundsException {
        checkNonNegative(columnIndex);
        int dividend = columnIndex + 1;
        String result = "";
        int modulo;

        while (dividend > 0) {
            modulo = (dividend - 1) % 26;
            result = (char) (65 + modulo) + result;
            dividend = (dividend - modulo) / 26;
        }

        return result;
    }

    @NonNull
    public static String getCellRef(@NonNegative int rowIndex, @NonNegative int columnIndex) throws IndexOutOfBoundsException {
        return getColumnLabel(columnIndex) + getRowLabel(rowIndex);
    }

    private static void checkNonNegative(int index) throws IndexOutOfBoundsException {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Expected: non-negative index, found:" + index);
        }
    }
}
