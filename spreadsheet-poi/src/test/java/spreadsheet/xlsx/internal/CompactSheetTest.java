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

import java.util.Arrays;
import java.util.Date;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class CompactSheetTest {

    @Test
    public void test() {
        CompactSheet.Builder b = new CompactSheet.Builder(2, 3, "my_sheet", Arrays.asList("hello", "world")::get);

        assertContent(b.build(), "my_sheet", new Object[2][3]);

        Date now = new Date();
        b.putDate(0, 0, now.getTime());
        b.putNumber(0, 1, 3.14);
        b.putSharedString(0, 2, 1);
        b.putString(1, 2, "other");

        assertContent(b.build(), "my_sheet", new Object[][]{{now, 3.14, "world"}, {null, null, "other"}});

        assertThatExceptionOfType(IndexOutOfBoundsException.class).isThrownBy(() -> b.putNull(-1, 0));
        assertThatExceptionOfType(IndexOutOfBoundsException.class).isThrownBy(() -> b.putNull(2, 0));
        assertThatExceptionOfType(IndexOutOfBoundsException.class).isThrownBy(() -> b.putNull(0, 3));
    }

    private void assertContent(CompactSheet sheet, String name, Object[][] data) {
        assertThat(sheet.getName()).isEqualTo(name);
        for (int i = 0; i < sheet.getRowCount(); i++) {
            for (int j = 0; j < sheet.getColumnCount(); j++) {
                assertThat(sheet.getCellValue(i, j)).isEqualTo(data[i][j]);
            }
        }
    }
}
