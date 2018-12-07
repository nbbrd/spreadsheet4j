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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class FileHelper {

    private boolean hasExtension(String filename, String... exts) {
        switch (exts.length) {
            case 1:
                return filename.endsWith(exts[0]);
            default:
                for (String o : exts) {
                    if (filename.endsWith(o)) {
                        return true;
                    }
                }
                return false;
        }
    }

    public boolean hasExtension(@Nonnull Path file, @Nonnull String... exts) {
        String filename = file.getName(file.getNameCount() - 1).toString().toLowerCase(Locale.ROOT);
        return hasExtension(filename, exts);
    }

    public boolean hasExtension(@Nonnull File file, @Nonnull String... exts) {
        String filename = file.getName().toLowerCase(Locale.ROOT);
        return hasExtension(filename, exts);
    }

    private boolean hasMagicNumber(InputStream stream, byte... header) throws IOException {
        byte[] actual = new byte[header.length];
        return header.length == stream.read(actual) && Arrays.equals(actual, header);
    }

    public boolean hasMagicNumber(@Nonnull Path file, @Nonnull byte... header) {
        try {
            try (InputStream stream = Files.newInputStream(file)) {
                return hasMagicNumber(stream, header);
            }
        } catch (IOException ex) {
            return false;
        }
    }

    public boolean hasMagicNumber(@Nonnull File file, @Nonnull byte... header) {
        try {
            try (InputStream stream = new FileInputStream(file)) {
                return hasMagicNumber(stream, header);
            }
        } catch (IOException ex) {
            return false;
        }
    }
}
