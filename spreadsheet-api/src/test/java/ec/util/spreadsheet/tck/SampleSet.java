package ec.util.spreadsheet.tck;

@lombok.Value
@lombok.Builder
public class SampleSet {

    Sample valid;
    Sample validWithTail;
    Sample invalidContent;
    Sample invalidFormat;
    Sample empty;
    Sample missing;
    Sample badExtension;
}
