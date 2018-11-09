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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class XlsxValueFactoryTest {

    private static final int NO_STYLE = XlsxValueFactory.NULL_STYLE_INDEX;
    private static final int OUT_OF_BOUNDS_STYLE = -1;

    private static XlsxValueFactory newFactory() {
        return new XlsxValueFactory(
                DefaultDateSystem.X1904,
                Arrays.asList("hello", "world")::get,
                Arrays.asList(false, true)::get);
    }

    private Date toDate(int year, int month, int day) {
        return new Date(year - 1900, month - 1, day);
    }

    @Test
    public void testGetNull() {
        XlsxValueFactory f = newFactory();

        assertThat(f.getValue(XlsxDataType.UNDEFINED, "other", NO_STYLE)).isNull();
        assertThat(f.getValue(XlsxDataType.UNDEFINED, "2010-02-01", NO_STYLE)).isNull();

        assertThat(f.getValue(XlsxDataType.UNDEFINED, "1", OUT_OF_BOUNDS_STYLE)).isNull();
        assertThat(f.getValue(XlsxDataType.UNDEFINED, "3.14", OUT_OF_BOUNDS_STYLE)).isNull();

        assertThat(f.getValue(XlsxDataType.NUMBER, "1", OUT_OF_BOUNDS_STYLE)).isNull();

        assertThat(f.getValue(XlsxDataType.DATE, "1", NO_STYLE)).isNull();
        assertThat(f.getValue(XlsxDataType.DATE, "3.14", NO_STYLE)).isNull();
        assertThat(f.getValue(XlsxDataType.DATE, "other", NO_STYLE)).isNull();
    }

    @Test
    public void testGetNumber() {
        XlsxValueFactory f = newFactory();

        assertThat(f.getValue(XlsxDataType.UNDEFINED, "1", NO_STYLE)).isEqualTo(1d);

        assertThat(f.getValue(XlsxDataType.UNDEFINED, "3.14", NO_STYLE)).isEqualTo(3.14);
        assertThat(f.getValue(XlsxDataType.UNDEFINED, "3.14", 0)).isEqualTo(3.14);
        assertThat(f.getValue(XlsxDataType.UNDEFINED, "3.14", 1)).isNotInstanceOf(Number.class);

        assertThat(f.getValue(XlsxDataType.NUMBER, "3.14", NO_STYLE)).isEqualTo(3.14);
        assertThat(f.getValue(XlsxDataType.NUMBER, "3.14", 0)).isEqualTo(3.14);
        assertThat(f.getValue(XlsxDataType.NUMBER, "3.14", 1)).isNotInstanceOf(Number.class);
    }

    @Test
    public void testGetDate() {
        Calendar cal = new GregorianCalendar();

        XlsxValueFactory f = newFactory();

        assertThat(f.getValue(XlsxDataType.UNDEFINED, "1", NO_STYLE)).isNotInstanceOf(Date.class);
        assertThat(f.getValue(XlsxDataType.UNDEFINED, "1", 0)).isNotInstanceOf(Date.class);
        assertThat(f.getValue(XlsxDataType.UNDEFINED, "1", 1)).isEqualTo(DefaultDateSystem.X1904.getJavaDate(cal, 1));

        assertThat(f.getValue(XlsxDataType.NUMBER, "1", NO_STYLE)).isNotInstanceOf(Date.class);
        assertThat(f.getValue(XlsxDataType.NUMBER, "1", 0)).isNotInstanceOf(Date.class);
        assertThat(f.getValue(XlsxDataType.NUMBER, "1", 1)).isEqualTo(DefaultDateSystem.X1904.getJavaDate(cal, 1));

        assertThat(f.getValue(XlsxDataType.DATE, "2010-02-01", NO_STYLE)).isEqualTo(toDate(2010, 2, 1));

        assertThat(f.getValue(XlsxDataType.UNDEFINED, "3.14", 1)).isInstanceOf(Date.class).isNotEqualTo(f.getValue(XlsxDataType.UNDEFINED, "3", 1));
        assertThat(f.getValue(XlsxDataType.UNDEFINED, "3.99", 1)).isInstanceOf(Date.class).isNotEqualTo(f.getValue(XlsxDataType.UNDEFINED, "4", 1));
    }

    @Test
    public void testGetString() {
        XlsxValueFactory f = newFactory();

        assertThat(f.getValue(XlsxDataType.SHARED_STRING, "0", NO_STYLE)).isEqualTo("hello");
        assertThat(f.getValue(XlsxDataType.SHARED_STRING, "1", NO_STYLE)).isEqualTo("world");
        assertThat(f.getValue(XlsxDataType.SHARED_STRING, "-1", NO_STYLE)).isNull();
        assertThat(f.getValue(XlsxDataType.SHARED_STRING, "other", NO_STYLE)).isNull();

        assertThat(f.getValue(XlsxDataType.STRING, "0", NO_STYLE)).isEqualTo("0");
        assertThat(f.getValue(XlsxDataType.STRING, "other", NO_STYLE)).isEqualTo("other");

        assertThat(f.getValue(XlsxDataType.INLINE_STRING, "0", NO_STYLE)).isEqualTo("0");
        assertThat(f.getValue(XlsxDataType.INLINE_STRING, "other", NO_STYLE)).isEqualTo("other");
    }
}
