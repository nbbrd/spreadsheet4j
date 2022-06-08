/*
 * Copyright 2016 National Bank of Belgium
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
package ec.util.spreadsheet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Philippe Charles
 */
public class BookTest {

    @Test
    public void testGetSheetName() throws IOException {
        Book mock = new Book() {
            @Override
            public int getSheetCount() {
                return 1;
            }

            @Override
            public Sheet getSheet(int index) throws IOException, IndexOutOfBoundsException {
                switch (index) {
                    case 0:
                        return new Sheet() {
                            @Override
                            public int getRowCount() {
                                return 0;
                            }

                            @Override
                            public int getColumnCount() {
                                return 0;
                            }

                            @Override
                            public Cell getCell(int rowIdx, int columnIdx) throws IndexOutOfBoundsException {
                                throw new IndexOutOfBoundsException();
                            }

                            @Override
                            public String getName() {
                                return "hello";
                            }
                        };
                    default:
                        throw new IndexOutOfBoundsException();
                }
            }
        };
        assertThat(mock.getSheetName(0)).isEqualTo("hello");
        assertThatThrownBy(() -> mock.getSheetName(-1)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> mock.getSheetName(1)).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    public void testIsSupportedDataType() {
        Book.Factory mock = new Book.Factory() {
            @Override
            public String getName() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Book load(InputStream stream) throws IOException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void store(OutputStream stream, Book book) throws IOException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean accept(File pathname) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        assertThat(mock.isSupportedDataType(String.class)).isTrue();
        assertThat(mock.isSupportedDataType(Number.class)).isTrue();
        assertThat(mock.isSupportedDataType(Date.class)).isTrue();
        assertThat(mock.isSupportedDataType(Double.class)).isTrue();
        assertThat(mock.isSupportedDataType(Book.class)).isFalse();
    }
}
