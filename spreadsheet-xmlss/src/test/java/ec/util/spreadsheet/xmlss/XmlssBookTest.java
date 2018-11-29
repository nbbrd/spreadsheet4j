/*
 * Copyright 2017 National Bank of Belgium
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
package ec.util.spreadsheet.xmlss;

import ec.util.spreadsheet.helpers.ArrayBook;
import ec.util.spreadsheet.helpers.ArraySheet;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.xml.stream.XMLOutputFactory;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class XmlssBookTest {

    @Test
    public void test() throws IOException {
        ArrayBook book = ArraySheet.builder().name("sheet1").value(0, 0, 3.14).build().toBook();
        String original;
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            new XmlssBookWriter(XMLOutputFactory.newInstance(), StandardCharsets.UTF_8).write(stream, book);
            original = new String(stream.toByteArray());
        }

        try (ByteArrayInputStream stream = new ByteArrayInputStream(original.getBytes())) {
            try (XmlssBook xxx = XmlssBook.parse(stream)) {
                assertThat(ArrayBook.copyOf(xxx)).isEqualTo(book);
            }
        }

        String xmlWithTrailingSection = original + "\0";
        try (ByteArrayInputStream stream = new ByteArrayInputStream(xmlWithTrailingSection.getBytes())) {
            try (XmlssBook xxx = XmlssBook.parse(stream)) {
                assertThat(ArrayBook.copyOf(xxx)).isEqualTo(book);
            }
        }
    }
}
