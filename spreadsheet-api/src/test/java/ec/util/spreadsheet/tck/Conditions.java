package ec.util.spreadsheet.tck;

import ec.util.spreadsheet.Book;
import lombok.NonNull;
import org.assertj.core.api.Condition;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public final class Conditions {

    private Conditions() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static @NonNull Condition<Book.Factory> supportingDataType(@NonNull Class<?> dataType) {
        return new Condition<>(x -> x.isSupportedDataType(dataType), "supporting data type " + dataType);
    }

    public static @NonNull Condition<Book.Factory> ableToLoadContent() {
        return new Condition<>(Book.Factory::canLoad, "able to load content");
    }

    public static @NonNull Condition<Book.Factory> ableToStoreContent() {
        return new Condition<>(Book.Factory::canStore, "able to store content");
    }

    public static @NonNull Condition<Book.Factory> acceptingFile(@NonNull File file) {
        return new Condition<>(x -> x.accept(file), "accepting file " + file);
    }

    public static @NonNull Condition<Book.Factory> acceptingPath(@NonNull Path file) {
        return new Condition<>(x -> {
            try {
                return x.accept(file);
            } catch (IOException e) {
                return false;
            }
        }, "accepting file " + file);
    }

}
