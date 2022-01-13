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
import ec.util.spreadsheet.tck.BookAssert;
import ec.util.spreadsheet.helpers.ArrayBook;
import ec.util.spreadsheet.helpers.ArraySheet;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import javax.xml.stream.XMLOutputFactory;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import shaded.spreadsheet.nbbrd.io.function.IOSupplier;

/**
 *
 * @author Philippe Charles
 */
public class XmlssBookReaderTest {

    private ArrayBook doParseStream(IOSupplier<InputStream> byteSource) throws IOException {
        try (InputStream stream = byteSource.getWithIO()) {
            return XmlssBookReader.parseStream(stream);
        }
    }

    @Test
    @SuppressWarnings("null")
    public void testParseStream() throws IOException {
        assertThatNullPointerException().isThrownBy(() -> XmlssBookReader.parseStream(null));

        ArrayBook original = doParseStream(Top5.VALID::stream);
        Top5.assertTop5Book(original);

        BookAssert
                .assertThat(doParseStream(Top5.VALID_WITH_TAIL::stream))
                .hasSameContentAs(original, true);

        BookAssert
                .assertThat(doParseStream(Top5.BAD_EXTENSION::stream))
                .hasSameContentAs(original, true);

        assertThatExceptionOfType(XmlssContentException.class)
                .isThrownBy(() -> doParseStream(Top5.INVALID_CONTENT::stream));

        assertThatExceptionOfType(XmlssFormatException.class)
                .isThrownBy(() -> doParseStream(Top5.INVALID_FORMAT::stream));

        assertThatExceptionOfType(EOFException.class)
                .isThrownBy(() -> doParseStream(Top5.EMPTY::stream));
    }

    @Test
    @SuppressWarnings("null")
    public void testParseFile(@TempDir Path temp) throws IOException {
        assertThatNullPointerException().isThrownBy(() -> XmlssBookReader.parseFile(null));

        ArrayBook original = XmlssBookReader.parseFile(Top5.VALID.file(temp));
        Top5.assertTop5Book(original);

        BookAssert
                .assertThat(XmlssBookReader.parseFile(Top5.VALID_WITH_TAIL.file(temp)))
                .hasSameContentAs(original, true);

        BookAssert
                .assertThat(XmlssBookReader.parseFile(Top5.BAD_EXTENSION.file(temp)))
                .hasSameContentAs(original, true);

        assertThatExceptionOfType(XmlssContentException.class)
                .isThrownBy(() -> XmlssBookReader.parseFile(Top5.INVALID_CONTENT.file(temp)))
                .withMessageContaining("file:/");

        assertThatExceptionOfType(XmlssFormatException.class)
                .isThrownBy(() -> XmlssBookReader.parseFile(Top5.INVALID_FORMAT.file(temp)))
                .withMessageContaining("file:/");

        assertThatExceptionOfType(EOFException.class)
                .isThrownBy(() -> XmlssBookReader.parseFile(Top5.EMPTY.file(temp)))
                .withMessageContaining(Top5.EMPTY.getFileName());

        assertThatExceptionOfType(NoSuchFileException.class)
                .isThrownBy(() -> XmlssBookReader.parseFile(Top5.MISSING.file(temp)))
                .withMessageContaining(Top5.MISSING.getFileName());
    }

    @Test
    public void test() throws IOException {
        ArrayBook book = ArraySheet.builder().name("sheet1").value(0, 0, 3.14).build().toBook();
        String original;
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            new XmlssBookWriter(XMLOutputFactory.newInstance(), StandardCharsets.UTF_8).write(stream, book);
            original = new String(stream.toByteArray());
        }

        try (InputStream stream = new ByteArrayInputStream(original.getBytes())) {
            try (ArrayBook xxx = XmlssBookReader.parseStream(stream)) {
                assertThat(xxx).isEqualTo(book);
            }
        }

        String xmlWithTrailingSection = original + "\0";
        try (InputStream stream = new ByteArrayInputStream(xmlWithTrailingSection.getBytes())) {
            try (ArrayBook xxx = XmlssBookReader.parseStream(stream)) {
                assertThat(xxx).isEqualTo(book);
            }
        }
    }
}
