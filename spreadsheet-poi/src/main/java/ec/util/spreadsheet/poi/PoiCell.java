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
package ec.util.spreadsheet.poi;

import java.util.Date;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Philippe Charles
 */
//@FlyweightPattern
@Deprecated
final class PoiCell extends ec.util.spreadsheet.Cell {

    private Cell cell = null;
    private Type type;

    @Nullable
    PoiCell withCell(@NonNull Cell cell) {
        this.cell = cell;
        this.type = getCellType(cell);
        return type != null ? this : null;
    }

    @Override
    public String getString() {
        if (!isString()) {
            throw new UnsupportedOperationException();
        }
        return cell.getStringCellValue();
    }

    @Override
    public Date getDate() {
        if (!isDate()) {
            throw new UnsupportedOperationException();
        }
        return cell.getDateCellValue();
    }

    @Override
    public Number getNumber() {
        return getDouble();
    }

    @Override
    public boolean isNumber() {
        return type == Type.NUMBER;
    }

    @Override
    public boolean isString() {
        return type == Type.STRING;
    }

    @Override
    public boolean isDate() {
        return type == Type.DATE;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public double getDouble() throws UnsupportedOperationException {
        if (!isNumber()) {
            throw new UnsupportedOperationException();
        }
        return cell.getNumericCellValue();
    }

    static @Nullable Type getCellType(Cell poiCell) {
        switch (getFinalType(poiCell)) {
            case STRING:
                return Type.STRING;
            case NUMERIC:
                return DateUtil.isCellDateFormatted(poiCell) ? Type.DATE : Type.NUMBER;
            default:
                return null;
        }
    }

    private static CellType getFinalType(Cell poiCell) {
        CellType result = poiCell.getCellType();
        return result != CellType.FORMULA ? result : poiCell.getCachedFormulaResultType();
    }
}
