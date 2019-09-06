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

import java.util.Date;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
//@FlyweightPattern
final class OdCell extends ec.util.spreadsheet.Cell {

    private transient Object value = null;

    @NonNull
    public OdCell withValue(@NonNull Object value) {
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
