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

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Philippe Charles
 */
public final class CellRefHelper {

    private boolean match = false;
    private int col = 0;
    private int row = 0;

    public boolean parse(@Nullable CharSequence ref) {
        if (ref == null) {
            return match = false;
        }

        int length = ref.length();
        int i = 0;

        col = 0;
        while (i < length) {
            char c = ref.charAt(i);
            if (c < 'A' || c > 'Z') {
                break;
            }
            col = col * 26 + ((byte) c - (byte) 'A') + 1;
            i++;
        }
        col--;

        row = 0;
        while (i < length) {
            char c = ref.charAt(i);
            if (c < '0' || c > '9') {
                break;
            }
            row = row * 10 + ((byte) c - (byte) '0');
            i++;
        }
        row--;

        return match = (i == length && col >= 0 && row >= 0);
    }

    /**
     * Returns a zero-based column index.
     *
     * @return
     */
    @Nonnegative
    public int getColumnIndex() throws IllegalStateException {
        checkMatch();
        return col;
    }

    /**
     * Returns a zero-based row index.
     *
     * @return
     */
    @Nonnegative
    public int getRowIndex() throws IllegalStateException {
        checkMatch();
        return row;
    }

    private void checkMatch() throws IllegalStateException {
        if (!match) {
            throw new IllegalStateException();
        }
    }

    @Nonnull
    public static String getRowLabel(@Nonnegative int rowIndex) throws IndexOutOfBoundsException {
        checkNonNegative(rowIndex);
        return Integer.toString(rowIndex + 1);
    }

    @Nonnull
    public static String getColumnLabel(@Nonnegative int columnIndex) throws IndexOutOfBoundsException {
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

    @Nonnull
    public static String getCellRef(@Nonnegative int rowIndex, @Nonnegative int columnIndex) throws IndexOutOfBoundsException {
        return getColumnLabel(columnIndex) + getRowLabel(rowIndex);
    }

    private static void checkNonNegative(int index) throws IndexOutOfBoundsException {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Expected: non-negative index, found:" + index);
        }
    }
}
