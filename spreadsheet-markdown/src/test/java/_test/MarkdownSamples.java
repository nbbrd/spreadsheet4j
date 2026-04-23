package _test;

import ec.util.spreadsheet.tck.Sample;
import ec.util.spreadsheet.tck.SampleSet;

/**
 * Test samples for markdown tables
 */
public final class MarkdownSamples {

    private MarkdownSamples() {}

    private static final String MD_TWO_TABLES =
            "# Title\n" +
            "\n" +
            "| H1 | H2 |\n" +
            "|----|----|\n" +
            "| a  | b  |\n" +
            "| c  | d  |\n" +
            "\n" +
            "Some text between\n" +
            "\n" +
            "| X | Y | Z |\n" +
            "|---|---|---|\n" +
            "| 1 | 2 | 3 |\n";

    public static final SampleSet MD = SampleSet
            .builder()
            .valid(Sample.of("valid.md", MD_TWO_TABLES))
            .validWithTail(null)
            .invalidContent(null)
            .invalidFormat(null)
            .empty(Sample.of("empty.md", ""))
            .missing(Sample.of("missing.md"))
            .badExtension(Sample.of("badExtension.zip", MD_TWO_TABLES))
            .build();
}

