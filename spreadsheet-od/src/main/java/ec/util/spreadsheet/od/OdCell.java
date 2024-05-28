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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author Philippe Charles
 */
//@FlyweightPattern
@Deprecated
@lombok.RequiredArgsConstructor
final class OdCell extends ec.util.spreadsheet.Cell {

    private final @lombok.NonNull ZoneId zoneId;
    private transient Object value = null;

    @Nullable
    OdCell withValue(@NonNull Object value) {
        this.value = value;
        return isValidValue(value) ? this : null;
    }

    private static boolean isValidValue(Object value) {
        return value instanceof LocalDateTime
                || value instanceof LocalDate
                || value instanceof Number
                || value instanceof String;
    }

    @Override
    public boolean isDate() {
        return value instanceof LocalDateTime || value instanceof LocalDate;
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
    public @NonNull Date getDate() {
        try {
            return OdSheet.toDate((LocalDateTime) value, zoneId);
        } catch (ClassCastException ex1) {
            try {
                return OdSheet.toDate((LocalDate) value, zoneId);
            } catch (ClassCastException ex2) {
                throw new UnsupportedOperationException(ex2);
            }
        }
    }

    @Override
    public @NonNull Number getNumber() {
        try {
            return (Number) value;
        } catch (ClassCastException ex) {
            throw new UnsupportedOperationException(ex);
        }
    }

    @Override
    public @NonNull String getString() {
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
