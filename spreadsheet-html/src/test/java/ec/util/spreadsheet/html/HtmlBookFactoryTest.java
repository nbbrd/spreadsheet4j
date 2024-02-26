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
package ec.util.spreadsheet.html;

import ec.util.spreadsheet.tck.BookFactoryAssert;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Date;

import static _test.HtmlSamples.HTML;
import static ec.util.spreadsheet.tck.Assertions.assertThat;
import static ec.util.spreadsheet.tck.Conditions.*;

/**
 * @author Philippe Charles
 */
public class HtmlBookFactoryTest {

    @Test
    public void testCompliance(@TempDir Path temp) throws IOException {
        assertThat(new HtmlBookFactory())
                .isCompliant(HTML, temp);
    }

    @Test
    public void testContent(@TempDir Path temp) throws IOException {
        Assertions.assertThat(new HtmlBookFactory())
                .is(ableToLoadContent())
                .is(ableToStoreContent());

        BookFactoryAssert.assertReadWrite(
                new HtmlBookFactory(),
                new HtmlBookFactory(),
                HTML.getValid().file(temp),
                Files.createTempFile(temp, "output", ".htm").toFile()
        );
    }

    @Test
    public void testIsSupportedDataType() {
        Assertions.assertThat(new HtmlBookFactory())
                .isNot(supportingDataType(Date.class))
                .isNot(supportingDataType(Number.class))
                .is(supportingDataType(String.class))
                .isNot(supportingDataType(LocalDateTime.class));
    }

    @Test
    public void testAcceptFile(@TempDir Path temp) {
        Assertions.assertThat(new HtmlBookFactory())
                .is(acceptingFile(HTML.getValid().file(temp)))
                .is(acceptingFile(HTML.getMissing().file(temp)))
                .isNot(acceptingFile(HTML.getBadExtension().file(temp)))
                .isNot(acceptingFile(HTML.getEmpty().file(temp)));
    }

    @Test
    public void testAcceptPath(@TempDir Path temp) {
        Assertions.assertThat(new HtmlBookFactory())
                .is(acceptingPath(HTML.getValid().path(temp)))
                .is(acceptingPath(HTML.getMissing().path(temp)))
                .isNot(acceptingPath(HTML.getBadExtension().path(temp)))
                .isNot(acceptingPath(HTML.getEmpty().path(temp)));
    }
}
