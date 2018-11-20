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

        assertThat(r.parse("A1")).isTrue();
        assertThat(r.getColumnIndex()).isEqualTo(0);
        assertThat(r.getRowIndex()).isEqualTo(0);

        assertThat(r.parse("A10")).isTrue();
        assertThat(r.getColumnIndex()).isEqualTo(0);
        assertThat(r.getRowIndex()).isEqualTo(9);

        assertThat(r.parse("A2")).isTrue();
        assertThat(r.getColumnIndex()).isEqualTo(0);
        assertThat(r.getRowIndex()).isEqualTo(1);

        assertThat(r.parse("B1")).isTrue();
        assertThat(r.getColumnIndex()).isEqualTo(1);
        assertThat(r.getRowIndex()).isEqualTo(0);

        assertThat(r.parse("Z9")).isTrue();
        assertThat(r.getColumnIndex()).isEqualTo(25);
        assertThat(r.getRowIndex()).isEqualTo(8);

        assertThat(r.parse("AA9")).isTrue();
        assertThat(r.getColumnIndex()).isEqualTo(26);
        assertThat(r.getRowIndex()).isEqualTo(8);

        assertThat(r.parse("AB9")).isTrue();
        assertThat(r.getColumnIndex()).isEqualTo(27);
        assertThat(r.getRowIndex()).isEqualTo(8);

        assertThat(r.parse("BA9")).isTrue();
        assertThat(r.getColumnIndex()).isEqualTo(52);
        assertThat(r.getRowIndex()).isEqualTo(8);

        assertThat(r.parse("hello")).isFalse();
        assertThatThrownBy(() -> r.getColumnIndex()).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> r.getRowIndex()).isInstanceOf(IllegalStateException.class);

        assertThat(r.parse(null)).isFalse();
        assertThat(r.parse("")).isFalse();
        assertThat(r.parse("2")).isFalse();
        assertThat(r.parse("A")).isFalse();
        assertThat(r.parse("A0")).isFalse();
        assertThat(r.parse("a2")).isFalse();
        assertThat(r.parse("ä2")).isFalse();
        assertThat(r.parse("_A2")).isFalse();
        assertThat(r.parse("A_2")).isFalse();
        assertThat(r.parse("A2_")).isFalse();
    }
}
