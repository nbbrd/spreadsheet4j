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

import _test.XmlssSamples;
import ec.util.spreadsheet.helpers.ArrayBook;
import ec.util.spreadsheet.helpers.ArraySheet;
import ec.util.spreadsheet.tck.BookAssert;
import nbbrd.io.function.IOSupplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.xml.stream.XMLOutputFactory;
import java.io.*;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import static _test.XmlssSamples.XML_TOP5;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.*;

/**
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

        ArrayBook original = doParseStream(XML_TOP5.getValid()::stream);
        XmlssSamples.assertTop5Book(original);

        BookAssert
                .assertThat(doParseStream(XML_TOP5.getValidWithTail()::stream))
                .hasSameContentAs(original, true);

        BookAssert
                .assertThat(doParseStream(XML_TOP5.getBadExtension()::stream))
                .hasSameContentAs(original, true);

        assertThatExceptionOfType(XmlssContentException.class)
                .isThrownBy(() -> doParseStream(XML_TOP5.getInvalidContent()::stream));

        assertThatExceptionOfType(XmlssFormatException.class)
                .isThrownBy(() -> doParseStream(XML_TOP5.getInvalidFormat()::stream));

        assertThatExceptionOfType(EOFException.class)
                .isThrownBy(() -> doParseStream(XML_TOP5.getEmpty()::stream));
    }

    @Test
    @SuppressWarnings("null")
    public void testParseFile(@TempDir Path temp) throws IOException {
        assertThatNullPointerException().isThrownBy(() -> XmlssBookReader.parseFile(null));

        ArrayBook original = XmlssBookReader.parseFile(XML_TOP5.getValid().file(temp));
        XmlssSamples.assertTop5Book(original);

        BookAssert
                .assertThat(XmlssBookReader.parseFile(XML_TOP5.getValidWithTail().file(temp)))
                .hasSameContentAs(original, true);

        BookAssert
                .assertThat(XmlssBookReader.parseFile(XML_TOP5.getBadExtension().file(temp)))
                .hasSameContentAs(original, true);

        assertThatExceptionOfType(XmlssContentException.class)
                .isThrownBy(() -> XmlssBookReader.parseFile(XML_TOP5.getInvalidContent().file(temp)))
                .withMessageContaining("file:/");

        assertThatExceptionOfType(XmlssFormatException.class)
                .isThrownBy(() -> XmlssBookReader.parseFile(XML_TOP5.getInvalidFormat().file(temp)))
                .withMessageContaining("file:/");

        assertThatExceptionOfType(EOFException.class)
                .isThrownBy(() -> XmlssBookReader.parseFile(XML_TOP5.getEmpty().file(temp)))
                .withMessageContaining(XML_TOP5.getEmpty().getFileName());

        assertThatExceptionOfType(NoSuchFileException.class)
                .isThrownBy(() -> XmlssBookReader.parseFile(XML_TOP5.getMissing().file(temp)))
                .withMessageContaining(XML_TOP5.getMissing().getFileName());
    }

    @Test
    public void test() throws IOException {
        ArrayBook book = ArraySheet.builder().name("sheet1").value(0, 0, 3.14).build().toBook();
        String original;
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            new XmlssBookWriter(XMLOutputFactory.newInstance(), UTF_8).write(stream, book);
            original = new String(stream.toByteArray(), UTF_8);
        }

        try (InputStream stream = new ByteArrayInputStream(original.getBytes(UTF_8))) {
            try (ArrayBook xxx = XmlssBookReader.parseStream(stream)) {
                assertThat(xxx).isEqualTo(book);
            }
        }

        String xmlWithTrailingSection = original + "\0";
        try (InputStream stream = new ByteArrayInputStream(xmlWithTrailingSection.getBytes(UTF_8))) {
            try (ArrayBook xxx = XmlssBookReader.parseStream(stream)) {
                assertThat(xxx).isEqualTo(book);
            }
        }
    }
}
