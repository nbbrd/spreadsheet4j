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
package ec.util.spreadsheet.xmlss;

import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.tck.BookFactoryAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Date;

import static _test.XmlssSamples.XML_TOP5;
import static ec.util.spreadsheet.tck.Conditions.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class XmlssBookFactoryTest {

    @Test
    public void testCompliance(@TempDir Path temp) throws IOException {
        BookFactoryAssert.assertThat(new XmlssBookFactory())
                .isCompliant(XML_TOP5, temp);
    }

    @Test
    public void testContent(@TempDir Path temp) throws IOException {
        assertThat(new XmlssBookFactory())
                .is(ableToLoadContent())
                .is(ableToStoreContent());

        Book.Factory x = new XmlssBookFactory();
        BookFactoryAssert.assertReadWrite(x, x,
                XML_TOP5.getValid().file(temp),
                Files.createTempFile(temp, "output", ".xml").toFile()
        );
    }

    @Test
    public void testIsSupportedDataType() {
        assertThat(new XmlssBookFactory())
                .is(supportingDataType(Date.class))
                .is(supportingDataType(Number.class))
                .is(supportingDataType(String.class))
                .isNot(supportingDataType(LocalDateTime.class));
    }

    @Test
    public void testAcceptFile(@TempDir Path temp) {
        assertThat(new XmlssBookFactory())
                .is(acceptingFile(XML_TOP5.getValid().file(temp)))
                .is(acceptingFile(XML_TOP5.getMissing().file(temp)))
                .is(acceptingFile(XML_TOP5.getValidWithTail().file(temp)))
                .isNot(acceptingFile(XML_TOP5.getBadExtension().file(temp)))
                .isNot(acceptingFile(XML_TOP5.getInvalidFormat().file(temp)))
                .isNot(acceptingFile(XML_TOP5.getEmpty().file(temp)))
                .isNot(acceptingFile(XML_TOP5.getInvalidContent().file(temp)));
    }

    @Test
    public void testAcceptPath(@TempDir Path temp) {
        assertThat(new XmlssBookFactory())
                .is(acceptingPath(XML_TOP5.getValid().path(temp)))
                .is(acceptingPath(XML_TOP5.getMissing().path(temp)))
                .is(acceptingPath(XML_TOP5.getValidWithTail().path(temp)))
                .isNot(acceptingPath(XML_TOP5.getBadExtension().path(temp)))
                .isNot(acceptingPath(XML_TOP5.getInvalidFormat().path(temp)))
                .isNot(acceptingPath(XML_TOP5.getEmpty().path(temp)))
                .isNot(acceptingPath(XML_TOP5.getInvalidContent().path(temp)));
    }
}
