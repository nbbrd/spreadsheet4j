/*
 * Copyright 2018 National Bank of Belgium
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

import ec.util.spreadsheet.CellAssert;
import ec.util.spreadsheet.Sheet;
import ec.util.spreadsheet.SheetAssert;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class CompactSheetTest {

    @Test
    @SuppressWarnings("null")
    public void testBuilder() {
        assertThatNullPointerException()
                .isThrownBy(() -> CompactSheet.builder(0, 0, null, Collections.emptyList()));
        assertThatNullPointerException()
                .isThrownBy(() -> CompactSheet.builder(0, 0, "", null));
    }

    @Test
    public void testGetName() {
        assertThat(CompactSheet.builder(0, 0, "", Collections.emptyList()).build().getName()).isEqualTo("");
        assertThat(CompactSheet.builder(0, 0, "hello", Collections.emptyList()).build().getName()).isEqualTo("hello");
    }

    @Test
    public void testGetRowCount() {
        assertThat(CompactSheet.builder(0, 0, "", Collections.emptyList()).build().getRowCount()).isEqualTo(0);
        assertThat(CompactSheet.builder(10, 0, "", Collections.emptyList()).build().getRowCount()).isEqualTo(10);
    }

    @Test
    public void testGetColCount() {
        assertThat(CompactSheet.builder(0, 0, "", Collections.emptyList()).build().getColumnCount()).isEqualTo(0);
        assertThat(CompactSheet.builder(0, 10, "", Collections.emptyList()).build().getColumnCount()).isEqualTo(10);
    }

    @Test
    public void testGetCellValue() {
        CompactSheet sample = getSample().build();

        SheetAssert.assertThat(sample)
                .hasCellValue(0, 0, NOW)
                .hasCellValue(0, 1, 3.14)
                .hasCellValue(0, 2, SHARED_STRINGS.get(1))
                .hasCellValue(1, 0, null)
                .hasCellValue(1, 1, null)
                .hasCellValue(1, 2, LOCAL_STRING);

        assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> sample.getCellValue(-1, 0));

        assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> sample.getCellValue(0, -1));

        assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> sample.getCellValue(2, 0));

        assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> sample.getCellValue(0, 3));
    }

    @Test
    public void testGetCell() {
        CompactSheet sample = getSample().build();

        CellAssert.assertThat(sample.getCell(0, 0)).isDate().hasDate(NOW);
        CellAssert.assertThat(sample.getCell(0, 1)).isNumber().hasNumber(3.14);
        CellAssert.assertThat(sample.getCell(0, 2)).isString().hasString(SHARED_STRINGS.get(1));
        CellAssert.assertThat(sample.getCell(1, 0)).isNull();
        CellAssert.assertThat(sample.getCell(1, 1)).isNull();
        CellAssert.assertThat(sample.getCell(1, 2)).isString().hasString(LOCAL_STRING);

        assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> sample.getCell(-1, 0));

        assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> sample.getCell(0, -1));

        assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> sample.getCell(2, 0));

        assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> sample.getCell(0, 3));
    }

    @Test
    public void testForEach() {
        CompactSheet sample = getSample().build();
        Object[][] values = new Object[2][3];
        sample.forEach((i, j, t) -> values[i][j] = t.getValue());
        assertThat(values).containsExactly(EXPECTED_VALUES);
    }

    @Test
    public void testForEachValue() {
        CompactSheet sample = getSample().build();
        Object[][] values = new Object[2][3];
        sample.forEachValue((i, j, t) -> values[i][j] = t);
        assertThat(values).containsExactly(EXPECTED_VALUES);
    }

    @Test
    public void testInv() {
        Sheet sample = getSample().build().inv();
        Object[][] values = new Object[3][2];
        sample.forEachValue((i, j, t) -> values[i][j] = t);
        assertThat(values).containsExactly(new Object[][]{
            {NOW, null},
            {3.14, null},
            {SHARED_STRINGS.get(1), "other"}
        });
    }

    @Test
    public void test() {
        CompactSheet.Builder b = CompactSheet.builder(2, 3, "my_sheet", SHARED_STRINGS);

        assertContent(b.build(), "my_sheet", new Object[2][3]);

        b.putDate(0, 0, NOW.getTime());
        b.putNumber(0, 1, 3.14);
        b.putSharedString(0, 2, 1);
        b.putString(1, 2, LOCAL_STRING);

        assertContent(b.build(), "my_sheet", EXPECTED_VALUES);

        assertThatExceptionOfType(IndexOutOfBoundsException.class).isThrownBy(() -> b.putNull(-1, 0));
        assertThatExceptionOfType(IndexOutOfBoundsException.class).isThrownBy(() -> b.putNull(2, 0));
        assertThatExceptionOfType(IndexOutOfBoundsException.class).isThrownBy(() -> b.putNull(0, 3));
    }

    private static CompactSheet.Builder getSample() {
        CompactSheet.Builder b = CompactSheet.builder(2, 3, "my_sheet", SHARED_STRINGS);
        b.putDate(0, 0, NOW.getTime());
        b.putNumber(0, 1, 3.14);
        b.putSharedString(0, 2, 1);
        b.putString(1, 2, LOCAL_STRING);
        return b;
    }

    private void assertContent(CompactSheet sheet, String name, Object[][] data) {
        assertThat(sheet.getName()).isEqualTo(name);
        for (int i = 0; i < sheet.getRowCount(); i++) {
            for (int j = 0; j < sheet.getColumnCount(); j++) {
                assertThat(sheet.getCellValue(i, j)).isEqualTo(data[i][j]);
            }
        }
    }

    private static final List<String> SHARED_STRINGS = Arrays.asList("hello", "world");
    private static final String LOCAL_STRING = "other";
    private static final Date NOW = new Date();
    private static final Object[][] EXPECTED_VALUES = {
        {NOW, 3.14, SHARED_STRINGS.get(1)},
        {null, null, "other"}
    };
}
