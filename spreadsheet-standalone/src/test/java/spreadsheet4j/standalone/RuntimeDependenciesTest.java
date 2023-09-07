package spreadsheet4j.standalone;

import _test.DependencyResolver;
import nbbrd.io.text.TextParser;
import org.assertj.core.api.Condition;
import org.assertj.core.api.ListAssert;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class RuntimeDependenciesTest {

    @Test
    public void testRuntimeDependencies() throws IOException {
        assertThat(getRuntimeDependencies())
                .describedAs("Check runtime dependencies")
                .satisfies(RuntimeDependenciesTest::checkJavaIoUtil)
                .satisfies(RuntimeDependenciesTest::checkSpreadsheet4j)
                .satisfies(RuntimeDependenciesTest::checkFastExcel)
                .satisfies(RuntimeDependenciesTest::checkSods)
                .satisfies(RuntimeDependenciesTest::checkJsoup)
                .hasSize(12);
    }

    private static void checkSpreadsheet4j(List<? extends DependencyResolver.GAV> coordinates) {
        assertThatGroupId(coordinates, "com.github.nbbrd.spreadsheet4j")
                .has(sameVersion())
                .extracting(DependencyResolver.GAV::getArtifactId)
                .containsExactlyInAnyOrder(
                        "spreadsheet-api",
                        "spreadsheet-fastexcel",
                        "spreadsheet-html",
                        "spreadsheet-od",
                        "spreadsheet-xl",
                        "spreadsheet-xmlss");
    }

    private static void checkJavaIoUtil(List<? extends DependencyResolver.GAV> coordinates) {
        assertThatGroupId(coordinates, "com.github.nbbrd.java-io-util")
                .has(sameVersion())
                .extracting(DependencyResolver.GAV::getArtifactId)
                .containsExactlyInAnyOrder("java-io-xml", "java-io-base");
    }

    private static void checkFastExcel(List<? extends DependencyResolver.GAV> coordinates) {
        assertThatGroupId(coordinates, "org.dhatim")
                .extracting(DependencyResolver.GAV::getArtifactId)
                .containsExactlyInAnyOrder("fastexcel");

        assertThatGroupId(coordinates, "com.github.rzymek")
                .extracting(DependencyResolver.GAV::getArtifactId)
                .containsExactlyInAnyOrder("opczip");
    }

    private static void checkSods(List<? extends DependencyResolver.GAV> coordinates) {
        assertThatGroupId(coordinates, "com.github.miachm.sods")
                .extracting(DependencyResolver.GAV::getArtifactId)
                .containsExactlyInAnyOrder("SODS");
    }

    private static void checkJsoup(List<? extends DependencyResolver.GAV> coordinates) {
        assertThatGroupId(coordinates, "org.jsoup")
                .extracting(DependencyResolver.GAV::getArtifactId)
                .containsExactlyInAnyOrder("jsoup");
    }

    private static ListAssert<? extends DependencyResolver.GAV> assertThatGroupId(List<? extends DependencyResolver.GAV> coordinates, String groupId) {
        return assertThat(coordinates)
                .describedAs("Check " + groupId)
                .filteredOn(DependencyResolver.GAV::getGroupId, groupId);
    }

    private static Condition<List<? extends DependencyResolver.GAV>> sameVersion() {
        return new Condition<>(DependencyResolver.GAV::haveSameVersion, "same version");
    }

    private static List<DependencyResolver.GAV> getRuntimeDependencies() throws IOException {
        return TextParser.onParsingReader(reader -> DependencyResolver.parse(asBufferedReader(reader).lines()))
                .parseResource(RuntimeDependenciesTest.class, "/runtime-dependencies.txt", UTF_8);
    }

    private static BufferedReader asBufferedReader(Reader reader) {
        return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
    }
}
