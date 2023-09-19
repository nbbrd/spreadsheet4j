/*
 * Copyright 2015 National Bank of Belgium
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
package ec.util.spreadsheet.od;

import _test.OdSamples;
import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.helpers.ArrayBook;
import ec.util.spreadsheet.tck.Assertions;
import ec.util.spreadsheet.tck.BookFactoryAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Date;

import static _test.OdSamples.ODS_TOP5;
import static ec.util.spreadsheet.tck.Conditions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIOException;

/**
 * @author Philippe Charles
 */
public class OpenDocumentBookFactoryTest {

    @Test
    public void testCompliance(@TempDir Path temp) throws IOException {
        Assertions.assertThat(new OpenDocumentBookFactory())
                .isCompliant(ODS_TOP5, temp);
    }

    @Test
    public void testContent(@TempDir Path temp) throws IOException {
        assertThat(new OpenDocumentBookFactory())
                .is(ableToLoadContent())
                .is(ableToStoreContent());

        Book.Factory x = new OpenDocumentBookFactory();
        BookFactoryAssert.assertReadWrite(x, x,
                ODS_TOP5.getValid().file(temp),
                Files.createTempFile(temp, "output", ".ods").toFile()
        );
    }

    @Test
    public void testIsSupportedDataType() {
        assertThat(new OpenDocumentBookFactory())
                .is(supportingDataType(Date.class))
                .is(supportingDataType(Number.class))
                .is(supportingDataType(String.class))
                .isNot(supportingDataType(LocalDateTime.class));
    }

    @Test
    public void testAcceptFile(@TempDir Path temp) {
        assertThat(new OpenDocumentBookFactory())
                .is(acceptingFile(ODS_TOP5.getValid().file(temp)))
                .is(acceptingFile(ODS_TOP5.getMissing().file(temp)))
                .is(acceptingFile(ODS_TOP5.getValidWithTail().file(temp)))
                .isNot(acceptingFile(ODS_TOP5.getBadExtension().file(temp)))
                .isNot(acceptingFile(ODS_TOP5.getInvalidFormat().file(temp)))
                .isNot(acceptingFile(ODS_TOP5.getEmpty().file(temp)));
    }

    @Test
    public void testAcceptPath(@TempDir Path temp) {
        assertThat(new OpenDocumentBookFactory())
                .is(acceptingPath(ODS_TOP5.getValid().path(temp)))
                .is(acceptingPath(ODS_TOP5.getMissing().path(temp)))
                .is(acceptingPath(ODS_TOP5.getValidWithTail().path(temp)))
                .isNot(acceptingPath(ODS_TOP5.getBadExtension().path(temp)))
                .isNot(acceptingPath(ODS_TOP5.getInvalidFormat().path(temp)))
                .isNot(acceptingPath(ODS_TOP5.getEmpty().path(temp)));
    }

    @Test
    public void testLoadFile(@TempDir Path temp) throws IOException {
        OpenDocumentBookFactory x = new OpenDocumentBookFactory();

        try (Book book = x.load(ODS_TOP5.getValid().file(temp))) {
            OdSamples.assertTop5Book(book);
        }
        try (Book book = x.load(ODS_TOP5.getBadExtension().file(temp))) {
            OdSamples.assertTop5Book(book);
        }
        try (Book book = x.load(ODS_TOP5.getValidWithTail().file(temp))) {
            OdSamples.assertTop5Book(book);
        }
        assertThatIOException().isThrownBy(() -> x.load(ODS_TOP5.getInvalidFormat().file(temp)));
        assertThatIOException().isThrownBy(() -> x.load(ODS_TOP5.getEmpty().file(temp)));
        assertThatIOException().isThrownBy(() -> x.load(ODS_TOP5.getMissing().file(temp)));
    }

    @Test
    public void testLoadStream() throws IOException {
        OpenDocumentBookFactory x = new OpenDocumentBookFactory();

        try (ArrayBook book = ODS_TOP5.getValid().loadStream(x)) {
            OdSamples.assertTop5Book(book);
        }
        try (ArrayBook book = ODS_TOP5.getBadExtension().loadStream(x)) {
            OdSamples.assertTop5Book(book);
        }
        try (ArrayBook book = ODS_TOP5.getValidWithTail().loadStream(x)) {
            OdSamples.assertTop5Book(book);
        }
        assertThatIOException().isThrownBy(() -> ODS_TOP5.getInvalidFormat().loadStream(x));
        assertThatIOException().isThrownBy(() -> ODS_TOP5.getEmpty().loadStream(x));
    }
}
