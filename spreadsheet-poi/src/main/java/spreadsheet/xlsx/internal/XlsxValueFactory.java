/*
 * Copyright 2013 National Bank of Belgium
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

import spreadsheet.xlsx.XlsxDataType;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import spreadsheet.xlsx.XlsxDateSystem;

/**
 *
 * @author Philippe Charles
 */
final class XlsxValueFactory {

    // http://openxmldeveloper.org/blog/b/openxmldeveloper/archive/2012/03/08/dates-in-strict-spreadsheetml-files.aspx
    private static final String ISO_DATE_FORMAT = "yyyy-MM-dd";

    private final XlsxDateSystem dateSystem;
    private final IntFunction<String> sharedStrings;
    private final IntPredicate dateFormats;
    private final Calendar calendar;
    private final DateFormat isoDateFormat;

    XlsxValueFactory(XlsxDateSystem dateSystem, IntFunction<String> sharedStrings, IntPredicate dateFormats) {
        this.dateSystem = dateSystem;
        this.sharedStrings = sharedStrings;
        this.dateFormats = dateFormats;
        // using default time-zone
        this.calendar = new GregorianCalendar();
        this.isoDateFormat = new SimpleDateFormat(ISO_DATE_FORMAT);
    }

    private boolean isDate(double number, int styleIndex) throws IndexOutOfBoundsException {
        return dateFormats.test(styleIndex) && dateSystem.isValidExcelDate(number);
    }

    @Nullable
    private Object getNumberOrDate(@Nonnull String rawValue, int styleIndex) {
        try {
            double number = Double.parseDouble(rawValue);
            switch (styleIndex) {
                case NULL_STYLE_INDEX:
                    return number;
                case INVALID_STYLE_INDEX:
                    return null;
                default:
                    return isDate(number, styleIndex) ? dateSystem.getJavaDate(calendar, number) : number;
            }
        } catch (NumberFormatException | IndexOutOfBoundsException ex) {
            return null;
        }
    }

    @Nullable
    private Object getSharedString(@Nonnull String rawValue) {
        try {
            return sharedStrings.apply(Integer.parseInt(rawValue));
        } catch (NumberFormatException | IndexOutOfBoundsException ex) {
            return null;
        }
    }

    @Nullable
    private Object getDate(@Nonnull String rawValue) {
        try {
            return isoDateFormat.parse(rawValue);
        } catch (ParseException ex) {
            return null;
        }
    }

    @Nullable
    public Object getValue(@Nonnull XlsxDataType dataType, @Nonnull String rawValue, @Nullable int styleIndex) {
        switch (dataType) {
            case UNDEFINED:
                return getNumberOrDate(rawValue, styleIndex);
            case NUMBER:
                return getNumberOrDate(rawValue, styleIndex);
            case SHARED_STRING:
                return getSharedString(rawValue);
            case DATE:
                return getDate(rawValue);
            case STRING:
                return rawValue;
            case INLINE_STRING:
                // TODO: rawValue might contain rich text
                return rawValue;
            default:
                // BOOLEAN or ERROR or UNKNOWN
                return null;
        }
    }

    public static final int NULL_STYLE_INDEX = Integer.MAX_VALUE;
    public static final int INVALID_STYLE_INDEX = Integer.MIN_VALUE;

    public static int parseStyleIndex(@Nullable String rawStyleIndex) {
        if (rawStyleIndex != null) {
            try {
                return Integer.parseInt(rawStyleIndex);
            } catch (NumberFormatException ex) {
                return INVALID_STYLE_INDEX;
            }
        }
        return NULL_STYLE_INDEX;
    }

    public static boolean isStyleRequired(@Nonnull XlsxDataType dataType) {
        switch (dataType) {
            case UNDEFINED:
            case NUMBER:
                return true;
            default:
                return false;
        }
    }
}
