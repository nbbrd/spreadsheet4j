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
package ec.util.spreadsheet.tck;

import java.util.Locale;

/**
 * Entry point for assertions of different data types. Each method in this class
 * is a static factory for the type-specific assertion objects.
 */
public class Assertions {

    /**
     * Creates a new instance of
     * <code>{@link BookAssert}</code>.
     *
     * @param actual the actual value.
     * @return the created assertion object.
     */
    public static BookAssert assertThat(ec.util.spreadsheet.Book actual) {
        return new BookAssert(actual);
    }

    /**
     * Creates a new instance of
     * <code>{@link BookFactoryAssert}</code>.
     *
     * @param actual the actual value.
     * @return the created assertion object.
     */
    public static BookFactoryAssert assertThat(ec.util.spreadsheet.Book.Factory actual) {
        return new BookFactoryAssert(actual);
    }

    /**
     * Creates a new instance of
     * <code>{@link CellAssert}</code>.
     *
     * @param actual the actual value.
     * @return the created assertion object.
     */
    public static CellAssert assertThat(ec.util.spreadsheet.Cell actual) {
        return new CellAssert(actual);
    }

    /**
     * Creates a new instance of
     * <code>{@link SheetAssert}</code>.
     *
     * @param actual the actual value.
     * @return the created assertion object.
     */
    public static SheetAssert assertThat(ec.util.spreadsheet.Sheet actual) {
        return new SheetAssert(actual);
    }

    /**
     * Creates a new <code>{@link Assertions}</code>.
     */
    protected Assertions() {
        // empty
    }

    static String msg(Object o, String code, Class<? extends Throwable> exClass) {
        return msg(o.getClass(), code, exClass);
    }

    static String msg(Class<?> codeClass, String code, Class<? extends Throwable> exClass) {
        return String.format(Locale.ROOT, "Expecting '%s#%s' to raise '%s'", codeClass.getName(), code, exClass.getName());
    }
}
