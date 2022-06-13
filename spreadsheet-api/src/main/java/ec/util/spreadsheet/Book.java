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
package ec.util.spreadsheet;

import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.ObjIntConsumer;
import nbbrd.service.Mutability;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceSorter;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Facade that represents <b>a book in a spreadsheet</b>. It is created by a
 * factory.
 * <br>Note that you should not store directly a sheet since some
 * implementations may use the flyweight pattern.
 * <br>This facade might also lock some resources. Therefore it is recommended
 * to {@link #close() close} it after use.
 *
 * @see Factory
 * @see Sheet
 * @see Cell
 * @author Philippe Charles
 */
//@FacadePattern
public abstract class Book implements Closeable {

    /**
     * Returns the number of sheets contained in this book.
     *
     * @return a sheet count
     */
    @NonNegative
    abstract public int getSheetCount();

    /**
     * Returns a sheet based on its index in this book.
     *
     * @param index a zero-based index
     * @return a non-null sheet
     * @throws IOException if something goes wrong during loading
     * @throws IndexOutOfBoundsException if the index is out of bounds
     * (0<=index<sheetCount)
     */
    @NonNull
    abstract public Sheet getSheet(@NonNegative int index) throws IOException, IndexOutOfBoundsException;

    /**
     * Returns the name of a sheet based on its index in this book.
     *
     * @param index a zero-based index
     * @return a non-null name
     * @throws IOException if something goes wrong during loading
     * @throws IndexOutOfBoundsException if the index is out of bounds
     */
    @NonNull
    public String getSheetName(@NonNegative int index) throws IOException, IndexOutOfBoundsException {
        return getSheet(index).getName();
    }

    /**
     * Performs the given action for each sheet of the book until all sheets
     * have been processed or an exception has been thrown.
     *
     * @implSpec
     * <p>
     * The default implementation behaves as if:<pre>{@code
     *     for (int index = 0; index < getSheetCount(); index++) {
     *         action.accept(getSheet(index), index);
     *     }
     * }</pre>
     *
     * @param action The action to be performed for each sheet
     * @throws NullPointerException if the specified action is null
     * @throws IOException if something goes wrong during loading
     * @since 2.2.0
     */
    public void forEach(@NonNull ObjIntConsumer<? super Sheet> action) throws IOException {
        Objects.requireNonNull(action);
        for (int index = 0; index < getSheetCount(); index++) {
            action.accept(getSheet(index), index);
        }
    }

    /**
     * Performs in parallel the given action for each sheet of the book until
     * all sheets have been processed or an exception has been thrown.
     *
     * @implSpec
     * <p>
     * The default implementation behaves as the regular foreach.
     *
     * @param action The action to be performed for each sheet
     * @throws NullPointerException if the specified action is null
     * @throws IOException if something goes wrong during loading
     * @since 2.2.2
     */
    public void parallelForEach(@NonNull ObjIntConsumer<? super Sheet> action) throws IOException {
        forEach(action);
    }

    /**
     * Closes this book and releases any resources associated with it.
     *
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
    }

    /**
     * Factory used to store/load books in/from spreadsheets.
     */
    @ServiceDefinition(
            singleton = true,
            quantifier = Quantifier.MULTIPLE,
            mutability = Mutability.CONCURRENT,
            loaderName = "ec.util.spreadsheet.BookFactoryLoader"
    )
    public abstract static class Factory implements FileFilter, DirectoryStream.Filter<Path> {

        /**
         * Returns a unique identifier of this factory.
         *
         * @return a non-null identifier
         */
        @NonNull
        abstract public String getName();

        @ServiceSorter(reverse = true)
        public int getRank() {
            return UNKNOWN_RANK;
        }

        //<editor-fold defaultstate="collapsed" desc="Loading methods">
        /**
         * Checks if this factory can load a book from a spreadsheet.
         *
         * @return true if loading is supported; false otherwise
         */
        public boolean canLoad() {
            return true;
        }

        /**
         * Loads a book from a Path.
         *
         * @param file a non-null spreadsheet file
         * @return a non-null book
         * @throws IOException if something goes wrong during the loading.
         */
        @NonNull
        public Book load(@NonNull Path file) throws IOException {
            try {
                return load(file.toFile());
            } catch (UnsupportedOperationException ex) {
                // if this Path is not associated with the default provider 
                try (InputStream stream = Files.newInputStream(file)) {
                    return load(stream);
                }
            }
        }

        /**
         * Loads a book from a File.
         *
         * @param file a non-null spreadsheet file
         * @return a non-null book
         * @throws IOException if something goes wrong during the loading.
         */
        @NonNull
        public Book load(@NonNull File file) throws IOException {
            try (InputStream stream = new FileInputStream(file)) {
                return load(stream);
            } catch (FileNotFoundException ex) {
                throw translate(ex);
            }
        }

        /**
         * Loads a book from an URL.
         *
         * @param url a non-null spreadsheet URL
         * @return a non-null book
         * @throws IOException if something goes wrong during the loading.
         */
        @NonNull
        public Book load(@NonNull URL url) throws IOException {
            try (InputStream stream = url.openStream()) {
                return load(stream);
            } catch (FileNotFoundException ex) {
                throw translate(ex);
            }
        }

        /**
         * Loads a book from an InputStream.<br>This method <u>does not
         * close</u> the stream after use.
         *
         * @param stream a non-null spreadsheet stream
         * @return a non-null book
         * @throws IOException if something goes wrong during the loading.
         */
        @NonNull
        abstract public Book load(@NonNull InputStream stream) throws IOException;
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="Storing methods">
        /**
         * Checks if this factory can store a book in a spreadsheet.
         *
         * @return true if storing is supported; false otherwise
         */
        public boolean canStore() {
            return true;
        }

        /**
         * Stores a book in a file.
         *
         * @param file a non-null spreadsheet file
         * @param book the data to be stored
         * @throws IOException if something goes wrong during the storing.
         */
        public void store(@NonNull Path file, @NonNull Book book) throws IOException {
            try {
                store(file.toFile(), book);
            } catch (UnsupportedOperationException ex) {
                // if this Path is not associated with the default provider 
                try (OutputStream stream = Files.newOutputStream(file)) {
                    store(stream, book);
                }
            }
        }

        /**
         * Stores a book in a file.
         *
         * @param file a non-null spreadsheet file
         * @param book the data to be stored
         * @throws IOException if something goes wrong during the storing.
         */
        public void store(@NonNull File file, @NonNull Book book) throws IOException {
            try (OutputStream stream = new FileOutputStream(file, false)) {
                store(stream, book);
            }
        }

        /**
         * Stores a book in an OutputStream.<br>This method <u>does not
         * close</u> the stream after use.
         *
         * @param stream a non-null spreadsheet stream
         * @param book the data to be stored
         * @throws IOException if something goes wrong during the storing.
         */
        abstract public void store(@NonNull OutputStream stream, @NonNull Book book) throws IOException;
        //</editor-fold>

        public @NonNull Map<String, List<String>> getExtensionsByMediaType() {
            return Collections.emptyMap();
        }

        @Override
        public boolean accept(Path entry) throws IOException {
            try {
                return accept(entry.toFile());
            } catch (UnsupportedOperationException ex) {
                // if this Path is not associated with the default provider 
                return false;
            }
        }

        public boolean isSupportedDataType(@NonNull Class<?> type) {
            return Date.class.isAssignableFrom(type)
                    || Number.class.isAssignableFrom(type)
                    || String.class.isAssignableFrom(type);
        }

        public static final int NATIVE_RANK = Byte.MAX_VALUE;
        public static final int WRAPPED_RANK = 0;
        public static final int UNKNOWN_RANK = -1;
    }

    private static IOException translate(FileNotFoundException ex) {
        String msg = ex.getMessage();
        if (msg != null && !msg.isEmpty()) {
            return new NoSuchFileException(msg);
        }
        return ex;
    }
}
