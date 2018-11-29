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
package ec.util.spreadsheet.helpers;

import static ec.util.spreadsheet.helpers.CellRefHelper.getCellRef;
import static ec.util.spreadsheet.helpers.CellRefHelper.getColumnLabel;
import static ec.util.spreadsheet.helpers.CellRefHelper.getRowLabel;
import java.util.function.Predicate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class CellRefHelperTest {

    @Test
    public void testGetCellRef() {
        assertThat(getCellRef(0, 0)).isEqualTo("A1");
        assertThat(getCellRef(0, 1)).isEqualTo("B1");
        assertThat(getCellRef(1, 0)).isEqualTo("A2");
        assertThat(getCellRef(0, 26)).isEqualTo("AA1");
        assertThatThrownBy(() -> getCellRef(-1, 0)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> getCellRef(0, -1)).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    public void testGetColumnLabel() {
        assertThat(getColumnLabel(0)).isEqualTo("A");
        assertThat(getColumnLabel(26)).isEqualTo("AA");
        assertThatThrownBy(() -> getColumnLabel(-1)).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    public void testGetRowLabel() {
        assertThat(getRowLabel(0)).isEqualTo("1");
        assertThat(getRowLabel(1)).isEqualTo("2");
        assertThatThrownBy(() -> getRowLabel(-1)).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    public void testParse() {
        CellRefHelper r = new CellRefHelper();
        assertThatThrownBy(() -> r.getColumnIndex()).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> r.getRowIndex()).isInstanceOf(IllegalStateException.class);

        assertMatch(r, o -> o.parse("A1"), 0, 0);
        assertMatch(r, o -> o.parse("A10"), 0, 9);
        assertMatch(r, o -> o.parse("A2"), 0, 1);
        assertMatch(r, o -> o.parse("B1"), 1, 0);
        assertMatch(r, o -> o.parse("Z9"), 25, 8);
        assertMatch(r, o -> o.parse("AA9"), 26, 8);
        assertMatch(r, o -> o.parse("AB9"), 27, 8);
        assertMatch(r, o -> o.parse("BA9"), 52, 8);

        assertMismatch(r, o -> o.parse(null));
        assertMismatch(r, o -> o.parse(""));
        assertMismatch(r, o -> o.parse("2"));
        assertMismatch(r, o -> o.parse("A"));
        assertMismatch(r, o -> o.parse("A0"));
        assertMismatch(r, o -> o.parse("a2"));
        assertMismatch(r, o -> o.parse("ä2"));
        assertMismatch(r, o -> o.parse("_A2"));
        assertMismatch(r, o -> o.parse("A_2"));
        assertMismatch(r, o -> o.parse("A2_"));
        assertMismatch(r, o -> o.parse("hello"));
    }

    @Test
    public void testParseEnd() {
        CellRefHelper r = new CellRefHelper();

        assertMismatch(r, o -> o.parseEnd(null));
        assertMismatch(r, o -> o.parseEnd(""));
        assertMismatch(r, o -> o.parseEnd("A1"));
        assertMismatch(r, o -> o.parseEnd("A1:"));
        assertMismatch(r, o -> o.parseEnd("A1:2"));
        assertMismatch(r, o -> o.parseEnd("A1:B2_"));

        assertMatch(r, o -> o.parseEnd("A1:B10"), 1, 9);
    }

    private static void assertMatch(CellRefHelper r, Predicate<CellRefHelper> test, int columnIndex, int rowIndex) {
        assertThat(test.test(r)).isTrue();
        assertThat(r.getColumnIndex()).isEqualTo(columnIndex);
        assertThat(r.getRowIndex()).isEqualTo(rowIndex);
    }

    private static void assertMismatch(CellRefHelper r, Predicate<CellRefHelper> test) {
        assertThat(test.test(r)).isFalse();
        assertThatThrownBy(() -> r.getColumnIndex()).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> r.getRowIndex()).isInstanceOf(IllegalStateException.class);
    }
}
