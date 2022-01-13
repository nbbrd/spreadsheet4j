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

import _test.Top5;
import ec.util.spreadsheet.tck.BookFactoryAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class XmlssBookFactoryTest {

    @Test
    public void testCompliance(@TempDir Path temp) throws IOException {
        File valid = Top5.VALID.file(temp);
        File invalid = Top5.INVALID_FORMAT.file(temp);
        BookFactoryAssert.assertThat(new XmlssBookFactory()).isCompliant(valid, invalid);
    }

    @Test
    public void testAcceptFile(@TempDir Path temp) throws IOException {
        XmlssBookFactory x = new XmlssBookFactory();

        assertThat(x.accept(Top5.VALID.file(temp))).isTrue();
        assertThat(x.accept(Top5.VALID_WITH_TAIL.file(temp))).isTrue();
        assertThat(x.accept(Top5.MISSING.file(temp))).isTrue();
        assertThat(x.accept(Top5.INVALID_CONTENT.file(temp))).isFalse();
        assertThat(x.accept(Top5.EMPTY.file(temp))).isFalse();
        assertThat(x.accept(Top5.INVALID_FORMAT.file(temp))).isFalse();
        assertThat(x.accept(Top5.BAD_EXTENSION.file(temp))).isFalse();
    }

    @Test
    public void testAcceptPath(@TempDir Path temp) throws IOException {
        XmlssBookFactory x = new XmlssBookFactory();

        assertThat(x.accept(Top5.VALID.path(temp))).isTrue();
        assertThat(x.accept(Top5.VALID_WITH_TAIL.path(temp))).isTrue();
        assertThat(x.accept(Top5.MISSING.path(temp))).isTrue();
        assertThat(x.accept(Top5.INVALID_CONTENT.path(temp))).isFalse();
        assertThat(x.accept(Top5.EMPTY.path(temp))).isFalse();
        assertThat(x.accept(Top5.INVALID_FORMAT.path(temp))).isFalse();
        assertThat(x.accept(Top5.BAD_EXTENSION.path(temp))).isFalse();
    }
}
