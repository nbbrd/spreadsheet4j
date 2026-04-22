module nbbrd.spreadsheet.markdown {

    requires static org.jspecify;
    requires static nbbrd.service;
    requires static lombok;

    requires nbbrd.spreadsheet.api;

    provides ec.util.spreadsheet.Book.Factory with
            ec.util.spreadsheet.markdown.MarkdownBookFactory;
}

