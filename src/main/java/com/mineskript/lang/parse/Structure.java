package com.mineskript.lang.parse;

import java.util.List;

/**
 * A top-level block of a script that an addon registered with {@link SyntaxRegistry#addStructure}, such as
 * {@code import:}. It is handed over before the script's functions and triggers are parsed.
 *
 * @param file the script file
 * @param line the line of the header
 * @param header the header as written, with its capitals and without the final colon
 * @param entries the lines indented under it, with their capitals
 */
public record Structure(String file, int line, String header, List<Entry> entries) {
    /** One line under the header. */
    public record Entry(int line, String text) {
    }

    public Structure {
        entries = List.copyOf(entries);
    }
}
