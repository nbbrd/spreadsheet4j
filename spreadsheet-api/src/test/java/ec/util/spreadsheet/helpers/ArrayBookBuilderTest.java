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

import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.Sheet;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Philippe Charles
 */
public class ArrayBookBuilderTest {

    final Sheet emptySheet = ArraySheet.builder().name("test").build();

    @Test
    public void testEmptyBuilder() {
        ArrayBook book = ArrayBook.builder().build();
        Assertions.assertEquals(0, book.getSheetCount2());
    }

    @Test
    public void testEmptySheet() {
        ArrayBook book = ArrayBook.builder().sheet(emptySheet).build();
        Assertions.assertEquals(1, book.getSheetCount2());
        Sheet sheet = book.getSheet(0);
        Assertions.assertEquals("test", sheet.getName());
        Assertions.assertEquals(0, sheet.getRowCount());
        Assertions.assertEquals(0, sheet.getColumnCount());
    }

    @Test
    public void testClear() {
        ArrayBook book = ArrayBook.builder().sheet(emptySheet).clear().build();
        Assertions.assertEquals(0, book.getSheetCount2());
    }
}
