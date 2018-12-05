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

import _test.Top5;
import ec.util.spreadsheet.BookAssert;
import ec.util.spreadsheet.helpers.ArrayBook;
import ec.util.spreadsheet.helpers.ArraySheet;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.xml.stream.XMLOutputFactory;
import static org.assertj.core.api.Assertions.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.xml.sax.SAXException;

/**
 *
 * @author Philippe Charles
 */
public class XmlssBookReaderTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    @SuppressWarnings("null")
    public void testParseStream() throws IOException {
        assertThatNullPointerException().isThrownBy(() -> XmlssBookReader.parseStream(null));

        ArrayBook original;
        try (InputStream stream = Top5.ORIGINAL.newStream()) {
            original = XmlssBookReader.parseStream(stream);
            Top5.assertTop5Book(original);
        }

        try (InputStream actual = Top5.WITH_TRAILING_SECTION.newStream()) {
            BookAssert
                    .assertThat(XmlssBookReader.parseStream(actual))
                    .hasSameContentAs(original, true);
        }

        assertThatIOException().isThrownBy(() -> {
            try (InputStream stream = Top5.WITHOUT_HEADER.newStream()) {
                XmlssBookReader.parseStream(stream);
            }
        }).withCauseInstanceOf(SAXException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testParseFile() throws IOException {
        assertThatNullPointerException().isThrownBy(() -> XmlssBookReader.parseFile(null));

        ArrayBook original = XmlssBookReader.parseFile(Top5.ORIGINAL.newFile(temp));
        Top5.assertTop5Book(original);

        BookAssert
                .assertThat(XmlssBookReader.parseFile(Top5.WITH_TRAILING_SECTION.newFile(temp)))
                .hasSameContentAs(original, true);

        assertThatIOException()
                .isThrownBy(() -> XmlssBookReader.parseFile(Top5.WITHOUT_HEADER.newFile(temp)))
                .withCauseInstanceOf(SAXException.class);
    }

    @Test
    public void test() throws IOException {
        ArrayBook book = ArraySheet.builder().name("sheet1").value(0, 0, 3.14).build().toBook();
        String original;
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            new XmlssBookWriter(XMLOutputFactory.newInstance(), StandardCharsets.UTF_8).write(stream, book);
            original = new String(stream.toByteArray());
        }

        try (InputStream stream = Top5.newInputStream(original)) {
            try (ArrayBook xxx = XmlssBookReader.parseStream(stream)) {
                assertThat(xxx).isEqualTo(book);
            }
        }

        String xmlWithTrailingSection = original + "\0";
        try (InputStream stream = Top5.newInputStream(xmlWithTrailingSection)) {
            try (ArrayBook xxx = XmlssBookReader.parseStream(stream)) {
                assertThat(xxx).isEqualTo(book);
            }
        }
    }
}
