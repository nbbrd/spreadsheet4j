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
import spreadsheet.xlsx.XlsxDateSystem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.function.IntPredicate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Philippe Charles
 */
final class XlsxValueFactory {

    interface Callback {

        void onNumber(double number);

        void onDate(long date);

        void onSharedString(int index);

        void onString(CharSequence string);

        void onNull();
    }

    private final ParserWithStyle numberOrDate;
    private final Parser sharedString;
    private final Parser date;

    XlsxValueFactory(XlsxDateSystem dateSystem, IntPredicate dateFormats) {
        this.numberOrDate = new NumberOrDateParser(dateSystem, dateFormats, NumberOrDateParser.newCalendar());
        this.sharedString = new SharedStringParser();
        this.date = new DateParser();
    }

    public void parse(Callback callback, CharSequence value, XlsxDataType dataType, int styleIndex) {
        switch (dataType) {
            case UNDEFINED:
                numberOrDate.parse(callback, value.toString(), styleIndex);
                break;
            case NUMBER:
                numberOrDate.parse(callback, value.toString(), styleIndex);
                break;
            case SHARED_STRING:
                sharedString.parse(callback, value.toString());
                break;
            case DATE:
                date.parse(callback, value.toString());
                break;
            case STRING:
                callback.onString(value.toString());
                break;
            case INLINE_STRING:
                callback.onString(value.toString());
                break;
            default:
                // BOOLEAN or ERROR or UNKNOWN
                callback.onNull();
                break;
        }
    }

    interface Parser {

        void parse(@NonNull Callback callback, @NonNull CharSequence rawValue);
    }

    interface ParserWithStyle {

        void parse(@NonNull Callback callback, @NonNull CharSequence rawValue, int styleIndex);
    }

    @lombok.AllArgsConstructor
    static final class NumberOrDateParser implements ParserWithStyle {

        static GregorianCalendar newCalendar() {
            return new GregorianCalendar(TimeZone.getDefault(), Locale.getDefault(Locale.Category.FORMAT));
        }

        private final XlsxDateSystem dateSystem;
        private final IntPredicate dateFormats;
        // using default time-zone
        private final Calendar calendar;

        private boolean isDate(double number, int styleIndex) throws IndexOutOfBoundsException {
            return dateFormats.test(styleIndex) && dateSystem.isValidExcelDate(number);
        }

        @Override
        public void parse(Callback callback, CharSequence rawValue, int styleIndex) {
            try {
                double number = Double.parseDouble(rawValue.toString());
                switch (styleIndex) {
                    case NULL_STYLE_INDEX:
                        callback.onNumber(number);
                        break;
                    case INVALID_STYLE_INDEX:
                        callback.onNull();
                        break;
                    default:
                        if (isDate(number, styleIndex)) {
                            callback.onDate(dateSystem.getJavaDateInMillis(calendar, number));
                        } else {
                            callback.onNumber(number);
                        }
                        break;
                }
            } catch (NumberFormatException | IndexOutOfBoundsException ex) {
                callback.onNull();
            }
        }
    }

    static final class SharedStringParser implements Parser {

        @Override
        public void parse(Callback callback, CharSequence rawValue) {
            try {
                callback.onSharedString(Integer.parseInt(rawValue.toString()));
//            return sharedStrings.apply(Integer.parseInt(rawValue));
            } catch (NumberFormatException | IndexOutOfBoundsException ex) {
                callback.onNull();
            }
        }
    }

    static final class DateParser implements Parser {

        // http://openxmldeveloper.org/blog/b/openxmldeveloper/archive/2012/03/08/dates-in-strict-spreadsheetml-files.aspx
        private final ZoneId zoneId = ZoneId.systemDefault();

        @Override
        public void parse(Callback callback, CharSequence rawValue) {
            try {
                callback.onDate(parseLocalDateTime(rawValue));
            } catch (DateTimeParseException dateTimeEx) {
                try {
                    callback.onDate(parseLocalDate(rawValue));
                } catch (DateTimeParseException dateEx) {
                    callback.onNull();
                }
            }
        }

        private long parseLocalDateTime(CharSequence rawValue) {
            return DateTimeFormatter.ISO_LOCAL_DATE_TIME
                    .parse(rawValue, LocalDateTime::from)
                    .atZone(zoneId)
                    .toInstant()
                    .toEpochMilli();
        }

        private long parseLocalDate(CharSequence rawValue) {
            return DateTimeFormatter.ISO_LOCAL_DATE
                    .parse(rawValue, LocalDate::from)
                    .atStartOfDay(zoneId)
                    .toInstant()
                    .toEpochMilli();
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

    public static boolean isStyleRequired(@NonNull XlsxDataType dataType) {
        switch (dataType) {
            case UNDEFINED:
            case NUMBER:
                return true;
            default:
                return false;
        }
    }

    private static final XlsxDataType[] DTYPES = XlsxDataType.values();

    public static XlsxDataType getDataTypeByOrdinal(int ordinal) {
        return DTYPES[ordinal];
    }
}
