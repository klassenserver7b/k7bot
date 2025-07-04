package de.klassenserver7b.k7bot.util.tablemessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class TableMessage {
    public static final String EMPTY_CHARACTER = "\u200E";

    public static final double MAX_WIDTH_EMBED_DESKTOP_PX = 488;
    public static final double INLINE_CODE_PADDING_DESKTOP_PX = 4.76; // padding: .2em
    public static final double INLINE_CODE_CHAR_WIDTH_PX = 6.54;

    private final List<Cell> content;
    private int columns;

    public TableMessage() {
        columns = 0;
        content = new ArrayList<>();
    }

    /**
     * Returns the number of rows in this table.
     *
     * @return the number of rows
     */

    public int getRows() {
        return content.size() / columns + (content.size() % columns > 0 ? 1 : 0);
    }

    /**
     * Returns the number of columns in this table.
     *
     * @return the number of columns
     */

    public int getColumns() {
        return columns;
    }

    /**
     * Returns the content of this table.
     *
     * @return the content data
     */

    public List<Cell> getContent() {
        return content;
    }

    /**
     * Returns whether this table has any cell, excluding the headline.
     *
     * @return whether the table has any data
     */

    public boolean hasData() {
        return getContent().size() > getColumns();
    }

    /**
     * Appends a new header entry to this table and increments the column counter.
     * <p>
     * Note: All headers have to be added to the table before the first data entry
     * is written.
     *
     * @param header the contents of this header entry
     * @return this table
     */

    public TableMessage addHeader(String header) {
        content.add(Cell.of(header, Cell.STYLE_BOLD | Cell.STYLE_UNDERLINED));
        columns++;
        return this;
    }

    /**
     * Appends multiple new header entries to this table and increments the column
     * counter.
     * <p>
     * Note: All headers have to be added to the table before the first data entry
     * is written.
     *
     * @param headers the contents of the header entries
     * @return this table
     */

    public TableMessage addHeadline(String... headers) {
        content.addAll(Stream.of(headers).map(header -> Cell.of(header, Cell.STYLE_BOLD | Cell.STYLE_UNDERLINED))
                .toList());
        columns += headers.length;
        return this;
    }

    /**
     * Appends a new cell to this table.
     * <p>
     * Note: All headers have to be added to the table before the first data entry
     * is written.
     *
     * @param cell the contents of this cell
     * @return this table
     */

    public TableMessage addCell(String cell) {
        content.add(Cell.of(cell));
        return this;
    }

    /**
     * Appends a cell to this table.
     * <p>
     * Note: All headers have to be added to the table before the first data entry
     * is written.
     *
     * @param cell the cell
     * @return this table
     */

    public TableMessage addCell(Cell cell) {
        content.add(cell);
        return this;
    }

    /**
     * Appends multiple new cells to this table.
     * <p>
     * Note: All headers have to be added to the table before the first data entry
     * is written.
     *
     * @param cells the contents of this cell
     * @return this table
     */

    public TableMessage addRow(String... cells) {
        content.addAll(Stream.of(cells).map(Cell::of).toList());
        return this;
    }

    /**
     * Appends multiple cells to this table.
     * <p>
     * Note: All headers have to be added to the table before the first data entry
     * is written.
     *
     * @param cells the contents of this cell
     * @return this table
     */

    public TableMessage addRow(Cell... cells) {
        content.addAll(Arrays.asList(cells));
        return this;
    }

    /**
     * Appends multiple cells to this table. This method accepts an array with each
     * element being either a cell object or a string containing the contents of the
     * new cell.
     * <p>
     * Note: All headers have to be added to the table before the first data entry
     * is written.
     *
     * @param objCells the cells
     * @return this table
     */
    public TableMessage addRow(Object... objCells) {
        List<Cell> cells = new ArrayList<>();

        for (Object cell : objCells) {
            if (cell == null)
                cells.add(Cell.of(""));
            else if (cell.getClass().isAssignableFrom(String.class))
                cells.add(Cell.of((String) cell));
            else if (cell.getClass().isAssignableFrom(Cell.class))
                cells.add((Cell) cell);
            else
                cells.add(Cell.of(""));
        }

        content.addAll(cells);

        return this;
    }

    public void setColums(int count) {
        this.columns = count;
    }

    /**
     * Inserts line breaks in the cells of the specified column so that the table
     * matches the width of an embed on a desktop computer. This method will do
     * nothing if the operation is impossible due to an invalid column index or
     * because there is not enough space for the table.
     *
     * @param column the column in which the line breaks are to be inserted
     * @return this table
     */
    public TableMessage automaticLineBreaks(int column) {
        if (column >= getColumns())
            return this;

        int maxChars = (int) ((MAX_WIDTH_EMBED_DESKTOP_PX - (3 * getColumns() - 1) * INLINE_CODE_PADDING_DESKTOP_PX)
                / INLINE_CODE_CHAR_WIDTH_PX) - getColumns() + 1;

        for (int c = 0; c < getColumns(); c++) {
            if (c == column)
                continue;

            int maxWidth = 0;

            for (int r = 0; r < getRows(); r++) {
                Cell cell = getCell(r, c);
                if (cell.getWidth() > maxWidth)
                    maxWidth = cell.getWidth();
            }

            maxChars -= maxWidth;
        }

        if (maxChars <= 0)
            return this;

        for (int r = 0; r < getRows(); r++) {
            List<String> resultLines = new ArrayList<>();
            Cell cell = getCell(r, column);

            for (int l = 0; l < cell.getHeight(); l++) {
                if (cell.getLine(l).length() <= maxChars) {
                    resultLines.add(cell.getLine(l));
                    continue;
                }

                String[] words = cell.getLine(l).split(" ", -1);
                StringBuilder line = new StringBuilder();

                for (String word : words) {
                    if (line.length() + 1 + word.length() <= maxChars) {
                        if (!line.isEmpty())
                            line.append(" ");
                        line.append(word);
                    } else {
                        resultLines.add(line.toString());
                        line = new StringBuilder(word);
                    }
                }

                resultLines.add(line.toString());
            }

            cell.setContent(resultLines);
        }

        return this;
    }

    /**
     * Returns the format code sequence representing this table.
     *
     * @return the formatted contents of this table
     */
    public String build() {
        int rows = content.size() / columns + (content.size() % columns > 0 ? 1 : 0);

        List<Integer> columnWidths = new ArrayList<>();
        List<Integer> rowHeights = new ArrayList<>();
        int fullHeight = 0;
        for (int c = 0; c < columns; c++) {
            int maxWidth = 0;
            for (int r = 0; r < rows; r++) {
                Cell cell = getCell(r, c);
                if (cell.getWidth() > maxWidth)
                    maxWidth = cell.getWidth();
            }
            columnWidths.add(maxWidth);
        }
        for (int r = 0; r < rows; r++) {
            int maxHeight = 0;
            for (int c = 0; c < columns; c++) {
                Cell cell = getCell(r, c);
                if (cell.getHeight() > maxHeight)
                    maxHeight = cell.getHeight();
            }
            rowHeights.add(maxHeight);
            fullHeight += maxHeight;
        }

        List<StringBuilder> lines = new ArrayList<>();

        for (int r = 0; r < fullHeight; r++) {
            StringBuilder line = new StringBuilder();

            for (int c = 0; c < columns; c++) {
                Cell cell = getCellData(r, c, rowHeights);
                String formatted = formatCell(cell);

                if (c != columns - 1) {
                    line.append(formatted).append(EMPTY_CHARACTER).append("`")
                            .append(whiteSpace(columnWidths.get(c) - cell.getWidth())).append("`")
                            .append(EMPTY_CHARACTER).append("**`|`**").append(EMPTY_CHARACTER);
                } else {
                    line.append(formatted).append(EMPTY_CHARACTER).append("`")
                            .append(whiteSpace(columnWidths.get(c) - cell.getWidth())).append("`");
                }
            }

            lines.add(line);
        }

        return String.join("\n", lines);
    }

    /**
     * Returns an object representing a single cell in the table on the screen.
     * Multiline rows will be treated as individual rows by this method.
     *
     * @param r          the row on the screen
     * @param c          the column on the screen
     * @param rowHeights an array containing the precomputed heights of the rows of
     *                   the table
     * @return the cell on the screen
     */
    private Cell getCellData(int r, int c, List<Integer> rowHeights) {
        if (c >= columns)
            return Cell.of("");

        int height = 0;
        for (int i = 0; i < rowHeights.size(); i++) {
            if (height + rowHeights.get(i) > r)
                return getCell(i, c).getLineCell(r - height);
            height += rowHeights.get(i);
        }
        return Cell.of("");
    }

    /**
     * Returns the cell at the specified position in the table. This method will
     * return an empty cell if the requested cell index is not present in the table.
     * A column argument greater or equal to the total amount of columns will result
     * in undefined behaviour.
     *
     * @param r the row of the requested cell
     * @param c the column of the requested cell
     * @return the cell at the specified position
     */
    private Cell getCell(int r, int c) {
        int index = columns * r + c;
        if (index >= content.size())
            return Cell.of("");
        return content.get(index);
    }

    /**
     * Returns a string containing space characters. This method will return an
     * empty character if the length argument is zero.
     *
     * @param length the number of spaces
     * @return the string containing the spaces
     */
    private String whiteSpace(int length) {
        if (length < 1)
            return EMPTY_CHARACTER;
        return " ".repeat(length);
    }

    /**
     * Returns the format code sequence representing this cell.
     *
     * @param cell the cell to format
     * @return the formatted contents of the cell
     */
    private String formatCell(Cell cell) {
        if (cell.getWidth() == 0)
            return "`" + EMPTY_CHARACTER + "`";

        StringBuilder builder = new StringBuilder();

        if (cell.hasLink())
            builder.append("[");

        if (cell.isBold())
            builder.append("**");
        if (cell.isUnderlined())
            builder.append("__");
        if (cell.isStrikethrough())
            builder.append("~~");
        if (cell.isItalic())
            builder.append("*");

        builder.append("`");
        builder.append(String.join("\n", cell.getContent()));
        builder.append("`");

        if (cell.isItalic())
            builder.append("*");
        if (cell.isStrikethrough())
            builder.append("~~");
        if (cell.isUnderlined())
            builder.append("__");
        if (cell.isBold())
            builder.append("**");

        if (cell.hasLink()) {
            builder.append("](");
            builder.append(cell.getLinkURL());

            if (cell.hasLinkTitle())
                builder.append(" \"").append(cell.getLinkTitle()).append("\"");

            builder.append(")");
        }

        return builder.toString();
    }
}