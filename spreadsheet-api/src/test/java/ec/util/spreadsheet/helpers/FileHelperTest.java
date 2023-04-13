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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

/**
 * @author Philippe Charles
 */
public class FileHelperTest {

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
    public void testHasMagicNumberFromFile(@TempDir Path temp) throws IOException {
        byte[] magic = "abc".getBytes(UTF_8);

        File file = Files.createFile(temp.resolve("hello.bin")).toFile();
        Files.write(file.toPath(), magic);

        assertThat(FileHelper.hasMagicNumber(file, magic)).isTrue();

        Files.write(file.toPath(), "".getBytes(UTF_8));
        assertThat(FileHelper.hasMagicNumber(file, magic)).isFalse();

        Files.write(file.toPath(), "abcdefg".getBytes(UTF_8));
        assertThat(FileHelper.hasMagicNumber(file, magic)).isTrue();

        Files.write(file.toPath(), "bcdefg".getBytes(UTF_8));
        assertThat(FileHelper.hasMagicNumber(file, magic)).isFalse();

        Files.write(file.toPath(), "ab".getBytes(UTF_8));
        assertThat(FileHelper.hasMagicNumber(file, magic)).isFalse();

        Files.write(file.toPath(), "abdefg".getBytes(UTF_8));
        assertThat(FileHelper.hasMagicNumber(file, magic)).isFalse();

        Files.write(file.toPath(), "xxx".getBytes(UTF_8));
        assertThat(FileHelper.hasMagicNumber(file, "".getBytes(UTF_8))).isTrue();

        Files.write(file.toPath(), "".getBytes(UTF_8));
        assertThat(FileHelper.hasMagicNumber(file, "".getBytes(UTF_8))).isTrue();

        assertThatNullPointerException().isThrownBy(() -> FileHelper.hasMagicNumber((File) null, magic));
        assertThatNullPointerException().isThrownBy(() -> FileHelper.hasMagicNumber(file, null));
    }

    @Test
    @SuppressWarnings("null")
    public void testHasMagicNumberFromPath(@TempDir Path temp) throws IOException {
        byte[] magic = "abc".getBytes(UTF_8);

        Path file = Files.createFile(temp.resolve("hello.bin"));
        Files.write(file, magic);

        assertThat(FileHelper.hasMagicNumber(file, magic)).isTrue();

        Files.write(file, "".getBytes(UTF_8));
        assertThat(FileHelper.hasMagicNumber(file, magic)).isFalse();

        Files.write(file, "abcdefg".getBytes(UTF_8));
        assertThat(FileHelper.hasMagicNumber(file, magic)).isTrue();

        Files.write(file, "bcdefg".getBytes(UTF_8));
        assertThat(FileHelper.hasMagicNumber(file, magic)).isFalse();

        Files.write(file, "ab".getBytes(UTF_8));
        assertThat(FileHelper.hasMagicNumber(file, magic)).isFalse();

        Files.write(file, "abdefg".getBytes(UTF_8));
        assertThat(FileHelper.hasMagicNumber(file, magic)).isFalse();

        Files.write(file, "xxx".getBytes(UTF_8));
        assertThat(FileHelper.hasMagicNumber(file, "".getBytes(UTF_8))).isTrue();

        Files.write(file, "".getBytes(UTF_8));
        assertThat(FileHelper.hasMagicNumber(file, "".getBytes(UTF_8))).isTrue();

        assertThatNullPointerException().isThrownBy(() -> FileHelper.hasMagicNumber((Path) null, magic));
        assertThatNullPointerException().isThrownBy(() -> FileHelper.hasMagicNumber(file, null));
    }

    @Test
    public void testAccept() {
        assertThatNullPointerException().isThrownBy(() -> FileHelper.accept(null, path -> false));
        assertThatNullPointerException().isThrownBy(() -> FileHelper.accept(new File(""), null));

        assertThat(FileHelper.accept(new File("hello.txt"), path -> true)).isTrue();

        assertThat(FileHelper.accept(new File("hello.txt"), path -> false)).isFalse();

        assertThat(FileHelper.accept(new File("hello.txt"), path -> {
            throw new IOException();
        })).isFalse();

        assertThat(FileHelper.accept(new File("mapi16:\\{9054}\\x@y($ddab4c7c)\\0\\Inbox\\at=abc:hello.xml\0"), path -> true)).isFalse();
    }
}
