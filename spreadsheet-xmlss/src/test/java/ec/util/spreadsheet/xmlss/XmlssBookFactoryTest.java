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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
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
        File valid = Top5.ORIGINAL.newFile(temp);
        File invalid = temp.newFile("invalid.xml");
        Files.write(invalid.toPath(), Arrays.asList("..."));
        BookFactoryAssert.assertThat(new XmlssBookFactory()).isCompliant(valid, invalid);
    }

    @Test
    public void testAcceptFile() throws IOException {
        XmlssBookFactory factory = new XmlssBookFactory();

        File valid = Top5.ORIGINAL.newFile(temp);
        assertThat(factory.accept(valid)).isTrue();

        File missing = temp.newFile("other.xml");
        missing.delete();
        assertThat(factory.accept(missing)).isTrue();

        File badExtension = temp.newFile("other.zip");
        assertThat(factory.accept(badExtension)).isFalse();
    }

    @Test
    public void testAcceptPath() throws IOException {
        XmlssBookFactory factory = new XmlssBookFactory();

        Path valid = Top5.ORIGINAL.newFile(temp).toPath();
        assertThat(factory.accept(valid)).isTrue();

        Path missing = temp.newFile("other.xml").toPath();
        Files.delete(missing);
        assertThat(factory.accept(missing)).isTrue();

        Path badExtension = temp.newFile("other.zip").toPath();
        assertThat(factory.accept(badExtension)).isFalse();
    }
}
