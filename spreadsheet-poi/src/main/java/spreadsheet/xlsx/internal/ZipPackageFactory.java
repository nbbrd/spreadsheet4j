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
package spreadsheet.xlsx.internal;

import ioutil.IO;
import ioutil.Zip;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.zip.ZipEntry;
import spreadsheet.xlsx.XlsxPackage;

/**
 *
 * @author Philippe Charles
 */
public enum ZipPackageFactory implements XlsxPackage.Factory {

    INSTANCE;

    @Override
    public XlsxPackage open(InputStream stream) throws IOException {
        return open(() -> Zip.loaderCopyOf(stream, ZipPackageFactory::isUsefulEntry));
    }

    @Override
    public XlsxPackage open(Path path) throws IOException {
        Optional<File> file = IO.getFile(path);
        return file.isPresent()
                ? open(file.get())
                : open(Files.newInputStream(path));
    }

    @Override
    public XlsxPackage open(File file) throws IOException {
        return open(() -> Zip.loaderOf(file));
    }

    private XlsxPackage open(IO.Supplier<IO.ResourceLoader<String>> source) throws IOException {
        return new DefaultPackage(source.getWithIO());
    }

    private static boolean isUsefulEntry(ZipEntry entry) {
        return DefaultPackage.isUsefulEntryName(entry.getName());
    }
}
