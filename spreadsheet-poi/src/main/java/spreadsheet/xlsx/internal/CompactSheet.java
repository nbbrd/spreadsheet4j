/*
 * Copyright 2018 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package spreadsheet.xlsx.internal;

import ec.util.spreadsheet.Cell;
import ec.util.spreadsheet.Sheet;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.IntFunction;
import javax.annotation.Nonnull;
import lombok.AccessLevel;

/**
 *
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CompactSheet extends Sheet {

    private static final int CELL_BYTES = Byte.BYTES + Double.BYTES;
    private static final int VALUE_OFFSET = Byte.BYTES;

    private final int rowCount;
    private final int columnCount;
    private final String name;
    private final ByteBuffer data;
    private final IntFunction<String> sharedStrings;
    private final List<String> strings;

    private final FlyweightCell flyweightCell = new FlyweightCell();

    @Override
    public int getRowCount() {
        return rowCount;
    }

    @Override
    public int getColumnCount() {
        return columnCount;
    }

    @Override
    public Cell getCell(int rowIdx, int columnIdx) throws IndexOutOfBoundsException {
        Object value = getCellValue(rowIdx, columnIdx);
        return value != null ? flyweightCell.withValue(value) : null;
    }

    @Override
    public Object getCellValue(int rowIdx, int columnIdx) throws IndexOutOfBoundsException {
        int index = getIndex(rowIdx, columnIdx);
        switch (Type.of(data.get(index))) {
            case DATE:
                return new Date(data.getLong(index + VALUE_OFFSET));
            case NULL:
                return null;
            case NUMBER:
                return data.getDouble(index + VALUE_OFFSET);
            case SHARED_STRING:
                return sharedStrings.apply(data.getInt(index + VALUE_OFFSET));
            case STRING:
                return strings.get(data.getInt(index + VALUE_OFFSET));
            default:
                throw new RuntimeException();
        }
    }

    @Override
    public String getName() {
        return name;
    }

    private int getIndex(int rowIdx, int columnIdx) {
        return (rowIdx * columnCount + columnIdx) * CELL_BYTES;
    }

    private enum Type {
        NULL, NUMBER, DATE, SHARED_STRING, STRING;

        private static final Type[] VALUES = values();

        public static Type of(int ordinal) {
            return VALUES[ordinal];
        }
    }

    private static final class FlyweightCell extends Cell implements Serializable {

        private transient Object value = null;

        @Nonnull
        public FlyweightCell withValue(@Nonnull Object value) {
            this.value = value;
            return this;
        }

        @Override
        public boolean isDate() {
            return value instanceof Date;
        }

        @Override
        public boolean isNumber() {
            return value instanceof Number;
        }

        @Override
        public boolean isString() {
            return value instanceof String;
        }

        @Override
        public Date getDate() {
            try {
                return (Date) value;
            } catch (ClassCastException ex) {
                throw new UnsupportedOperationException(ex);
            }
        }

        @Override
        public Number getNumber() {
            try {
                return (Number) value;
            } catch (ClassCastException ex) {
                throw new UnsupportedOperationException(ex);
            }
        }

        @Override
        public String getString() {
            try {
                return (String) value;
            } catch (ClassCastException ex) {
                throw new UnsupportedOperationException(ex);
            }
        }

        @Override
        public String toString() {
            return value != null ? value.toString() : "Null";
        }
    }

    public static final class Builder {

        private final int rowCount;
        private final int columnCount;
        private final String name;
        private final ByteBuffer data;
        private final IntFunction<String> sharedStrings;
        private final List<String> strings;

        public Builder(int rowCount, int columnCount, String name, IntFunction<String> sharedStrings) {
            this.rowCount = rowCount;
            this.columnCount = columnCount;
            this.name = name;
            this.sharedStrings = sharedStrings;
            this.data = ByteBuffer.allocate(rowCount * columnCount * CELL_BYTES);
            this.strings = new ArrayList<>();
        }

        private int getIndex(int i, int j) {
            if (j >= columnCount) {
                throw new IndexOutOfBoundsException();
            }
            return (i * columnCount + j) * CELL_BYTES;
        }

        public Builder putNull(int i, int j) {
            int index = getIndex(i, j);
            data.put(index, (byte) Type.NULL.ordinal());
            return this;
        }

        public Builder putNumber(int i, int j, double number) {
            int index = getIndex(i, j);
            data.put(index, (byte) Type.NUMBER.ordinal());
            data.putDouble(index + VALUE_OFFSET, number);
            return this;
        }

        public Builder putDate(int i, int j, long date) {
            int index = getIndex(i, j);
            data.put(index, (byte) Type.DATE.ordinal());
            data.putLong(index + VALUE_OFFSET, date);
            return this;
        }

        public Builder putSharedString(int i, int j, int stringIndex) {
            int index = getIndex(i, j);
            data.put(index, (byte) Type.SHARED_STRING.ordinal());
            data.putInt(index + VALUE_OFFSET, stringIndex);
            return this;
        }

        public Builder putString(int i, int j, String string) {
            int index = getIndex(i, j);
            data.put(index, (byte) Type.STRING.ordinal());
            data.putInt(index + VALUE_OFFSET, strings.size());
            strings.add(string);
            return this;
        }

        public CompactSheet build() {
            return new CompactSheet(rowCount, columnCount, name, data, sharedStrings, strings);
        }
    }
}
