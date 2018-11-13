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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.function.IntPredicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import spreadsheet.xlsx.XlsxDateSystem;

/**
 *
 * @author Philippe Charles
 */
final class XlsxValueFactory {

    interface Callback {

        void onNumber(double number);

        void onDate(long date);

        void onSharedString(int index);

        void onString(String string);

        void onNull();
    }

    private final ParserWithStyle numberOrDate;
    private final Parser sharedString;
    private final Parser date;

    XlsxValueFactory(XlsxDateSystem dateSystem, IntPredicate dateFormats) {
        this.numberOrDate = new NumberOrDateParser(dateSystem, dateFormats, new GregorianCalendar());
        this.sharedString = new SharedStringParser();
        this.date = new DateParser(new SimpleDateFormat());
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
                // TODO: rawValue might contain rich text
                callback.onString(value.toString());
                break;
            default:
                // BOOLEAN or ERROR or UNKNOWN
                callback.onNull();
                break;
        }
    }

    interface Parser {

        void parse(@Nonnull Callback callback, @Nonnull String rawValue);
    }

    interface ParserWithStyle {

        void parse(@Nonnull Callback callback, @Nonnull String rawValue, int styleIndex);
    }

    @lombok.AllArgsConstructor
    static final class NumberOrDateParser implements ParserWithStyle {

        private final XlsxDateSystem dateSystem;
        private final IntPredicate dateFormats;
        // using default time-zone
        private final Calendar calendar;

        private boolean isDate(double number, int styleIndex) throws IndexOutOfBoundsException {
            return dateFormats.test(styleIndex) && dateSystem.isValidExcelDate(number);
        }

        @Override
        public void parse(Callback callback, String rawValue, int styleIndex) {
            try {
                double number = Double.parseDouble(rawValue);
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
        public void parse(Callback callback, String rawValue) {
            try {
                callback.onSharedString(Integer.parseInt(rawValue));
//            return sharedStrings.apply(Integer.parseInt(rawValue));
            } catch (NumberFormatException | IndexOutOfBoundsException ex) {
                callback.onNull();
            }
        }
    }

    static final class DateParser implements Parser {

        // http://openxmldeveloper.org/blog/b/openxmldeveloper/archive/2012/03/08/dates-in-strict-spreadsheetml-files.aspx
        private static final String ISO_DATE_FORMAT = "yyyy-MM-dd";

        private final SimpleDateFormat isoDateFormat;

        public DateParser(SimpleDateFormat isoDateFormat) {
            this.isoDateFormat = isoDateFormat;
            isoDateFormat.applyPattern(ISO_DATE_FORMAT);
        }

        @Override
        public void parse(Callback callback, String rawValue) {
            try {
                callback.onDate(isoDateFormat.parse(rawValue).getTime());
            } catch (ParseException ex) {
                callback.onNull();
            }
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
