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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.IntFunction;
import javax.annotation.Nonnegative;
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
    private final List<String> localStrings;

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
        int index = getIndex(rowIdx, columnIdx);
        return getTypeAt(index) != Type.NULL ? flyweightCell.withValue(index) : null;
    }

    @Override
    public Object getCellValue(int rowIdx, int columnIdx) throws IndexOutOfBoundsException {
        int index = getIndex(rowIdx, columnIdx);
        return getValueAt(index);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "CompactSheet[" + rowCount + "x" + columnCount + "]";
    }

    private int getIndex(int rowIdx, int columnIdx) {
        if (columnIdx >= columnCount) {
            throw new IndexOutOfBoundsException();
        }
        return (rowIdx * columnCount + columnIdx) * CELL_BYTES;
    }

    private Type getTypeAt(int index) {
        return Type.of(data.get(index));
    }

    private Object getValueAt(int index) {
        switch (getTypeAt(index)) {
            case DATE:
                return getDateAt(index);
            case NULL:
                return null;
            case NUMBER:
                return getNumberAt(index);
            case SHARED_STRING:
                return getSharedStringAt(index);
            case LOCAL_STRING:
                return getLocalStringAt(index);
            default:
                throw new RuntimeException();
        }
    }

    private Date getDateAt(int index) {
        return new Date(data.getLong(index + VALUE_OFFSET));
    }

    private double getNumberAt(int index) {
        return data.getDouble(index + VALUE_OFFSET);
    }

    private String getSharedStringAt(int index) {
        return sharedStrings.apply(data.getInt(index + VALUE_OFFSET));
    }

    private String getLocalStringAt(int index) {
        return localStrings.get(data.getInt(index + VALUE_OFFSET));
    }

    private enum Type {
        NULL, NUMBER, DATE, SHARED_STRING, LOCAL_STRING;

        private static final Type[] VALUES = values();

        public static Type of(int ordinal) {
            return VALUES[ordinal];
        }
    }

    @Nonnull
    public static Builder builder(
            @Nonnegative int rowCount, @Nonnegative int columnCount,
            @Nonnull String name, @Nonnull IntFunction<String> sharedStrings) {
        return new Builder(rowCount, columnCount, name, sharedStrings);
    }

    private final class FlyweightCell extends Cell implements Serializable {

        private int index = -1;
        private CompactSheet.Type type = CompactSheet.Type.NULL;

        @Nonnull
        FlyweightCell withValue(int index) {
            this.index = index;
            this.type = getTypeAt(index);
            return this;
        }

        @Override
        public boolean isDate() {
            return type == CompactSheet.Type.DATE;
        }

        @Override
        public boolean isNumber() {
            return type == CompactSheet.Type.NUMBER;
        }

        @Override
        public boolean isString() {
            switch (type) {
                case SHARED_STRING:
                case LOCAL_STRING:
                    return true;
            }
            return false;
        }

        @Override
        public Date getDate() {
            if (!isDate()) {
                throw new UnsupportedOperationException();
            }
            return getDateAt(index);
        }

        @Override
        public Number getNumber() {
            if (!isNumber()) {
                throw new UnsupportedOperationException();
            }
            return getNumberAt(index);
        }

        @Override
        public String getString() {
            switch (type) {
                case SHARED_STRING:
                    return getSharedStringAt(index);
                case LOCAL_STRING:
                    return getLocalStringAt(index);
                default:
                    throw new UnsupportedOperationException();
            }
        }

        @Override
        public Type getType() {
            switch (type) {
                case DATE:
                    return Type.DATE;
                case NUMBER:
                    return Type.NUMBER;
                case SHARED_STRING:
                case LOCAL_STRING:
                    return Type.STRING;
                default:
                    throw new RuntimeException();
            }
        }

        @Override
        public Object getValue() {
            return getValueAt(index);
        }

        @Override
        public String toString() {
            return index + "";
        }
    }

    public static final class Builder {

        private final int rowCount;
        private final int columnCount;
        private final String name;
        private final ByteBuffer data;
        private final IntFunction<String> sharedStrings;
        private List<String> localStrings;

        private Builder(int rowCount, int columnCount, String name, IntFunction<String> sharedStrings) {
            this.rowCount = rowCount;
            this.columnCount = columnCount;
            this.name = Objects.requireNonNull(name);
            this.sharedStrings = Objects.requireNonNull(sharedStrings);
            this.data = ByteBuffer.allocate(rowCount * columnCount * CELL_BYTES);
            this.localStrings = null;
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
            if (localStrings == null) {
                localStrings = new ArrayList<>();
            }
            int index = getIndex(i, j);
            data.put(index, (byte) Type.LOCAL_STRING.ordinal());
            data.putInt(index + VALUE_OFFSET, localStrings.size());
            localStrings.add(string);
            return this;
        }

        public CompactSheet build() {
            return new CompactSheet(rowCount, columnCount, name, data,
                    sharedStrings, localStrings != null ? localStrings : Collections.emptyList());
        }
    }
}
