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
import ec.util.spreadsheet.BookFactoryAssert;
import java.io.File;
import java.io.IOException;
import static org.assertj.core.api.Assertions.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Philippe Charles
 */
public class XmlssBookFactoryTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void testCompliance() throws IOException {
        File valid = Top5.ORIGINAL.file(temp);
        File invalid = Top5.NOT_XML.file(temp);
        BookFactoryAssert.assertThat(new XmlssBookFactory()).isCompliant(valid, invalid);
    }

    @Test
    public void testAcceptFile() throws IOException {
        XmlssBookFactory x = new XmlssBookFactory();

        assertThat(x.accept(Top5.ORIGINAL.file(temp))).isTrue();
        assertThat(x.accept(Top5.WITH_TRAILING_SECTION.file(temp))).isTrue();
        assertThat(x.accept(Top5.MISSING.file(temp))).isTrue();
        assertThat(x.accept(Top5.WITHOUT_HEADER.file(temp))).isFalse();
        assertThat(x.accept(Top5.EMPTY.file(temp))).isFalse();
        assertThat(x.accept(Top5.NOT_XML.file(temp))).isFalse();
        assertThat(x.accept(Top5.BAD_EXTENSION.file(temp))).isFalse();
    }

    @Test
    public void testAcceptPath() throws IOException {
        XmlssBookFactory x = new XmlssBookFactory();

        assertThat(x.accept(Top5.ORIGINAL.path(temp))).isTrue();
        assertThat(x.accept(Top5.WITH_TRAILING_SECTION.path(temp))).isTrue();
        assertThat(x.accept(Top5.MISSING.path(temp))).isTrue();
        assertThat(x.accept(Top5.WITHOUT_HEADER.path(temp))).isFalse();
        assertThat(x.accept(Top5.EMPTY.path(temp))).isFalse();
        assertThat(x.accept(Top5.NOT_XML.path(temp))).isFalse();
        assertThat(x.accept(Top5.BAD_EXTENSION.path(temp))).isFalse();
    }
}
