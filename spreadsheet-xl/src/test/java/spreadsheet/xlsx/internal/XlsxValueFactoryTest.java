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

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Philippe Charles
 */
public class XlsxValueFactoryTest {

    private static final int NO_STYLE = XlsxValueFactory.NULL_STYLE_INDEX;
    private static final int OUT_OF_BOUNDS_STYLE = -1;

    private static final IntFunction<String> SHARED_STRINGS = Arrays.asList("hello", "world")::get;
    private static final IntPredicate DATE_FORMATS = Arrays.asList(false, true)::get;

    @Test
    public void testGetNumberOrDate() {
        XlsxValueFactory.ParserWithStyle f = new XlsxValueFactory.NumberOrDateParser(DefaultDateSystem.X1904, DATE_FORMATS, XlsxValueFactory.NumberOrDateParser.newCalendar());
        CustomCallback c = new CustomCallback();
        Calendar cal = XlsxValueFactory.NumberOrDateParser.newCalendar();

        f.parse(c, "1", NO_STYLE);
        assertThat(c.result).isEqualTo(1d);

        f.parse(c, "3.14", NO_STYLE);
        assertThat(c.result).isEqualTo(3.14);

        f.parse(c, "3.14", 0);
        assertThat(c.result).isEqualTo(3.14);

        f.parse(c, "3.14", 1);
        assertThat(c.result).isNotInstanceOf(Number.class);

        f.parse(c, "other", NO_STYLE);
        assertThat(c.result).isNull();

        f.parse(c, "2010-02-01", NO_STYLE);
        assertThat(c.result).isNull();

        f.parse(c, "1", OUT_OF_BOUNDS_STYLE);
        assertThat(c.result).isNull();

        f.parse(c, "3.14", OUT_OF_BOUNDS_STYLE);
        assertThat(c.result).isNull();

        f.parse(c, "1", OUT_OF_BOUNDS_STYLE);
        assertThat(c.result).isNull();

        f.parse(c, "1", NO_STYLE);
        assertThat(c.result).isNotInstanceOf(Date.class);

        f.parse(c, "1", 0);
        assertThat(c.result).isNotInstanceOf(Date.class);

        f.parse(c, "1", 1);
        assertThat(c.result).isEqualTo(DefaultDateSystem.X1904.getJavaDate(cal, 1));

        f.parse(c, "3.14", 1);
        assertThat(c.result).isInstanceOf(Date.class);

        f.parse(c, "31149.427430555555", 1);
        assertThat((Date) c.result).isEqualTo("1989-04-13T10:15:30");
    }

    @Test
    public void testGetDate() {
        XlsxValueFactory.Parser f = new XlsxValueFactory.DateParser();
        CustomCallback c = new CustomCallback();

        f.parse(c, "2010-02-01");
        assertThat((Date) c.result).isEqualTo("2010-02-01");

        f.parse(c, "2010-02-01T10");
        assertThat(c.result).isNull();

        f.parse(c, "1989-04-13T10:15:30");
        assertThat((Date) c.result).isEqualTo("1989-04-13T10:15:30");

        f.parse(c, "1989-04-13T10:15:30:15");
        assertThat(c.result).isNull();

        f.parse(c, "1");
        assertThat(c.result).isNull();

        f.parse(c, "3.14");
        assertThat(c.result).isNull();

        f.parse(c, "other");
        assertThat(c.result).isNull();
    }

    @Test
    public void testGetSharedString() {
        XlsxValueFactory.Parser f = new XlsxValueFactory.SharedStringParser();
        CustomCallback c = new CustomCallback();

        f.parse(c, "0");
        assertThat(c.result).isEqualTo("hello");

        f.parse(c, "1");
        assertThat(c.result).isEqualTo("world");

        f.parse(c, "-1");
        assertThat(c.result).isNull();

        f.parse(c, "other");
        assertThat(c.result).isNull();
    }

    private static final class CustomCallback implements XlsxValueFactory.Callback {

        Object result = null;

        @Override
        public void onNumber(double number) {
            result = number;
        }

        @Override
        public void onDate(long date) {
            result = new Date(date);
        }

        @Override
        public void onSharedString(int index) {
            result = SHARED_STRINGS.apply(index);
        }

        @Override
        public void onString(CharSequence string) {
            result = string;
        }

        @Override
        public void onNull() {
            result = null;
        }
    }
}
