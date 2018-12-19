/*
 * Copyright 2018 National Bank of Belgium
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
package ec.util.spreadsheet.helpers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.assertj.core.api.Assertions.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Philippe Charles
 */
public class FileHelperTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    @SuppressWarnings("null")
    public void testHasExtensionFromFile() {
        File f = new File("hello.xml");
        assertThat(FileHelper.hasExtension(f)).isFalse();
        assertThat(FileHelper.hasExtension(f, ".xml")).isTrue();
        assertThat(FileHelper.hasExtension(f, ".xml", ".zip")).isTrue();
        assertThat(FileHelper.hasExtension(f, ".zip", ".xml")).isTrue();
        assertThat(FileHelper.hasExtension(f, ".zip", ".txt")).isFalse();

        assertThatNullPointerException().isThrownBy(() -> FileHelper.hasExtension((File) null, ".xml"));
        assertThatNullPointerException().isThrownBy(() -> FileHelper.hasExtension(f, (String[]) null));
    }

    @Test
    @SuppressWarnings("null")
    public void testHasExtensionFromPath() {
        Path f = new File("hello.xml").toPath();
        assertThat(FileHelper.hasExtension(f)).isFalse();
        assertThat(FileHelper.hasExtension(f, ".xml")).isTrue();
        assertThat(FileHelper.hasExtension(f, ".xml", ".zip")).isTrue();
        assertThat(FileHelper.hasExtension(f, ".zip", ".xml")).isTrue();
        assertThat(FileHelper.hasExtension(f, ".zip", ".txt")).isFalse();

        assertThatNullPointerException().isThrownBy(() -> FileHelper.hasExtension((Path) null, ".xml"));
        assertThatNullPointerException().isThrownBy(() -> FileHelper.hasExtension(f, (String[]) null));
    }

    @Test
    @SuppressWarnings("null")
    public void testHasMagicNumberFromFile() throws IOException {
        byte[] magic = "abc".getBytes();

        File file = temp.newFile("hello.bin");
        Files.write(file.toPath(), magic);

        assertThat(FileHelper.hasMagicNumber(file, magic)).isTrue();

        Files.write(file.toPath(), "".getBytes());
        assertThat(FileHelper.hasMagicNumber(file, magic)).isFalse();

        Files.write(file.toPath(), "abcdefg".getBytes());
        assertThat(FileHelper.hasMagicNumber(file, magic)).isTrue();

        Files.write(file.toPath(), "bcdefg".getBytes());
        assertThat(FileHelper.hasMagicNumber(file, magic)).isFalse();

        Files.write(file.toPath(), "ab".getBytes());
        assertThat(FileHelper.hasMagicNumber(file, magic)).isFalse();

        Files.write(file.toPath(), "abdefg".getBytes());
        assertThat(FileHelper.hasMagicNumber(file, magic)).isFalse();

        Files.write(file.toPath(), "xxx".getBytes());
        assertThat(FileHelper.hasMagicNumber(file, "".getBytes())).isTrue();

        Files.write(file.toPath(), "".getBytes());
        assertThat(FileHelper.hasMagicNumber(file, "".getBytes())).isTrue();

        assertThatNullPointerException().isThrownBy(() -> FileHelper.hasMagicNumber((File) null, magic));
        assertThatNullPointerException().isThrownBy(() -> FileHelper.hasMagicNumber(file, null));
    }

    @Test
    @SuppressWarnings("null")
    public void testHasMagicNumberFromPath() throws IOException {
        byte[] magic = "abc".getBytes();

        Path file = temp.newFile("hello.bin").toPath();
        Files.write(file, magic);

        assertThat(FileHelper.hasMagicNumber(file, magic)).isTrue();

        Files.write(file, "".getBytes());
        assertThat(FileHelper.hasMagicNumber(file, magic)).isFalse();

        Files.write(file, "abcdefg".getBytes());
        assertThat(FileHelper.hasMagicNumber(file, magic)).isTrue();

        Files.write(file, "bcdefg".getBytes());
        assertThat(FileHelper.hasMagicNumber(file, magic)).isFalse();

        Files.write(file, "ab".getBytes());
        assertThat(FileHelper.hasMagicNumber(file, magic)).isFalse();

        Files.write(file, "abdefg".getBytes());
        assertThat(FileHelper.hasMagicNumber(file, magic)).isFalse();

        Files.write(file, "xxx".getBytes());
        assertThat(FileHelper.hasMagicNumber(file, "".getBytes())).isTrue();

        Files.write(file, "".getBytes());
        assertThat(FileHelper.hasMagicNumber(file, "".getBytes())).isTrue();

        assertThatNullPointerException().isThrownBy(() -> FileHelper.hasMagicNumber((Path) null, magic));
        assertThatNullPointerException().isThrownBy(() -> FileHelper.hasMagicNumber(file, null));
    }
}
