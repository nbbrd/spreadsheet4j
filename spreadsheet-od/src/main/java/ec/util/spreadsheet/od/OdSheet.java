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
package ec.util.spreadsheet.od;

import com.github.miachm.sods.Range;
import com.github.miachm.sods.Sheet;
import ec.util.spreadsheet.Cell;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author Philippe Charles
 */
final class OdSheet extends ec.util.spreadsheet.Sheet {

    private final static int BUGGED_COLUMN_COUNT = 16384;
    private final String name;
    private final Range sheet;
    private final int columnCount;
    @Deprecated
    private final OdCell flyweightCell;
    private final ZoneId zoneId = ZoneId.systemDefault();

    public OdSheet(Sheet sheet) {
        this.name = sheet.getName();
        this.sheet = sheet.getDataRange();
        this.columnCount = computeColumnCount(this.sheet);
        this.flyweightCell = new OdCell();
    }

    static int computeColumnCount(Range sheet) {
        if (sheet.getNumRows() == 0) {
            return 0;
        }
        int result = sheet.getNumColumns();
        if (result != BUGGED_COLUMN_COUNT) {
            return result;
        }
        // dichotomic search
        int min = 0;
        int max = BUGGED_COLUMN_COUNT;
        do {
            result = (min + max) / 2;
            if (!isNullOrEmpty(sheet, 0, result)) {
                min = result + 1;
            } else {
                max = result - 1;
            }
        } while (min <= max);
        return result + 1;
    }

    static boolean isNullOrEmpty(Range sheet, int rowIdx, int columnIdx) throws IndexOutOfBoundsException {
        return sheet.getCell(rowIdx, columnIdx).getValue() == null;
    }

    @Override
    public int getRowCount() {
        return sheet.getNumRows();
    }

    @Override
    public int getColumnCount() {
        return columnCount;
    }

    @Override
    public Cell getCell(int rowIdx, int columnIdx) {
        Object value = sheet.getCell(rowIdx, columnIdx).getValue();
        return value != null ? flyweightCell.withValue(value) : null;
    }

    @Override
    public @Nullable Object getCellValue(@NonNegative int rowIdx, @NonNegative int columnIdx) throws IndexOutOfBoundsException {
        Object value = sheet.getCell(rowIdx, columnIdx).getValue();
        if (OdCell.isValid(value)) {
            if (value instanceof LocalDateTime) {
                return Date.from(((LocalDateTime) value).atZone(zoneId).toInstant());
            }
            return value;
        }
        return null;
    }

    @Override
    public String getName() {
        return name.replace("_", " ");
    }
}
