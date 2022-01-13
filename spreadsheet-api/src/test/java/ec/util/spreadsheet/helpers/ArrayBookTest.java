/*
 * Copyright 2015 National Bank of Belgium
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
package ec.util.spreadsheet.helpers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 *
 * @author Philippe Charles
 */
public class ArrayBookTest {

    private ArrayBook newBook(ArraySheet... sheets) {
        return new ArrayBook(sheets);
    }

    final ArraySheet s1 = ArraySheet.builder().name("S1").value(0, 0, 123).build();
    final ArraySheet s2 = ArraySheet.builder().name("S2").value(2, 0, "hello").build();

    @Test
    public void testCopy() {
        ArrayBook book;

        book = newBook();
        Assertions.assertEquals(book, book.copy());
        Assertions.assertNotSame(book, book.copy());

        book = newBook(s1, s2);
        Assertions.assertEquals(book, book.copy());
        Assertions.assertNotSame(book, book.copy());
    }

    @Test
    public void testEquals() {
        ArrayBook book;

        book = newBook();
        Assertions.assertEquals(book, newBook());
        Assertions.assertNotEquals(book, newBook(s1));

        book = newBook(s1, s2);
        Assertions.assertEquals(book, newBook(s1, s2));
        Assertions.assertNotEquals(book, newBook(s1));
        Assertions.assertNotEquals(book, newBook(s2, s1));
    }

    @Test
    public void testGetSheetName() {
        ArrayBook book = newBook(s1, s2);
        assertThat(book.getSheetName(0)).isEqualTo("S1");
        assertThat(book.getSheetName(1)).isEqualTo("S2");
        assertThatThrownBy(() -> book.getSheetName(-1)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> book.getSheetName(2)).isInstanceOf(IndexOutOfBoundsException.class);
    }
}
