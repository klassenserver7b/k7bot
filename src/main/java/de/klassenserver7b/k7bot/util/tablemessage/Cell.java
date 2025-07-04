package de.klassenserver7b.k7bot.util.tablemessage;

import java.util.ArrayList;
import java.util.List;

public class Cell {
    public static final int STYLE_NONE = 0b0000;
    public static final int STYLE_BOLD = 0b0001;
    public static final int STYLE_UNDERLINED = 0b0010;
    public static final int STYLE_STRIKETHROUGH = 0b0100;
    public static final int STYLE_ITALIC = 0b1000;

    private List<String> content;
    private String linkURL;
    private String linkTitle;

    private boolean isBold;
    private boolean isUnderlined;
    private boolean isStrikethrough;
    private boolean isItalic;

    public Cell(List<String> content, String linkURL, String linkTitle, boolean isBold, boolean isUnderlined,
                boolean isStrikethrough, boolean isItalic) {
        this.content = content == null || content.isEmpty() ? new ArrayList<>(List.of("")) : new ArrayList<>(content);
        this.linkURL = linkURL;
        this.linkTitle = linkTitle;
        this.isBold = isBold;
        this.isUnderlined = isUnderlined;
        this.isStrikethrough = isStrikethrough;
        this.isItalic = isItalic;
    }

    /**
     * Returns the content of this cell.
     *
     * @return the content data
     */
    public List<String> getContent() {
        return content;
    }

    /**
     * Returns a specific line of this cell. This method returns an empty string if
     * the index is argument greater or equal to the total amount of lines.
     *
     * @param index the index of the line to return
     * @return the content of the specified line
     */
    public String getLine(int index) {
        return content.size() <= index ? "" : content.get(index);
    }

    /**
     * Returns a formatted cell object containing a specific line of this cell. This
     * method returns an empty cell if the index is argument greater or equal to the
     * total amount of lines.
     *
     * @param index the index of the line to return
     * @return the cell object of the specified line
     */
    public Cell getLineCell(int index) {
        return content.size() <= index ? Cell.of("")
                : Cell.of(content.get(index), linkURL, linkTitle, isBold, isUnderlined, isStrikethrough, isItalic);
    }

    /**
     * Sets the content of this cell.
     *
     * @param content the content data
     */
    public void setContent(List<String> content) {
        this.content = content == null || content.isEmpty() ? new ArrayList<>(List.of("")) : new ArrayList<>(content);
    }

    /**
     * Returns the total width of this cell, in characters.
     *
     * @return the width of this cell
     */
    public int getWidth() {
        int width = 0;
        for (String line : content)
            if (width < line.length())
                width = line.length();
        return width;
    }

    /**
     * Returns the total height of this cell, in lines.
     *
     * @return the height of this cell
     */
    public int getHeight() {
        return content.size();
    }

    /**
     * Returns whether this cell is a link.
     *
     * @return whether the cell is a link
     */
    public boolean hasLink() {
        return getLinkURL() != null;
    }

    /**
     * Returns the link url associated with this cell. This method will return null
     * if this cell isn't a link.
     *
     * @return the link url
     */
    public String getLinkURL() {
        return linkURL;
    }

    /**
     * Sets the link url associated with this cell. This method can be called
     * regardless of whether this cell is a link. Setting the link url to null will
     * mark this cell as not being a link.
     *
     * @param linkURL the link url
     */
    public void setLinkURL(String linkURL) {
        this.linkURL = linkURL;
    }

    /**
     * Returns whether this cell has a link tooltip.
     *
     * @return whether this cell has a link tooltip
     */
    public boolean hasLinkTitle() {
        return getLinkTitle() != null;
    }

    /**
     * Returns the link tooltip associated with this cell. This method will return
     * null if this cell doesn't have a link tooltip.
     *
     * @return the link tooltip
     */
    public String getLinkTitle() {
        return linkTitle;
    }

    /**
     * Sets the link tooltip associated with this cell. This method can be called
     * regardless of whether this cell has a link tooltip. Setting the link tooltip
     * to null will mark this cell as not having a link tooltip.
     *
     * @param linkTitle the link tooltip
     */
    public void setLinkTitle(String linkTitle) {
        this.linkTitle = linkTitle;
    }

    /**
     * Returns whether the content of this cell is bold.
     *
     * @return whether the content of this cell is bold
     */
    public boolean isBold() {
        return isBold;
    }

    /**
     * Specifies whether the content of this cell should be bold.
     *
     * @param bold whether the content of this cell should be bold
     */
    public void setBold(boolean bold) {
        isBold = bold;
    }

    /**
     * Returns whether the content of this cell is underlined.
     *
     * @return whether the content of this cell is underlined
     */
    public boolean isUnderlined() {
        return isUnderlined;
    }

    /**
     * Specifies whether the content of this cell should be underlined.
     *
     * @param underlined whether the content of this cell should be underlined
     */
    public void setUnderlined(boolean underlined) {
        isUnderlined = underlined;
    }

    /**
     * Returns whether the content of this cell is strikethrough.
     *
     * @return whether the content of this cell is strikethrough
     */
    public boolean isStrikethrough() {
        return isStrikethrough;
    }

    /**
     * Specifies whether the content of this cell should be strikethrough.
     *
     * @param strikethrough whether the content of this cell should be strikethrough
     */
    public void setStrikethrough(boolean strikethrough) {
        isStrikethrough = strikethrough;
    }

    /**
     * Returns whether the content of this cell is italic.
     *
     * @return whether the content of this cell is italic
     */
    public boolean isItalic() {
        return isItalic;
    }

    /**
     * Specifies whether the content of this cell should be italic.
     *
     * @param italic whether the content of this cell should be italic
     */
    public void setItalic(boolean italic) {
        isItalic = italic;
    }

    /**
     * Returns a new cell object containing the specified content.
     *
     * @param content the content
     * @return the cell object
     */
    public static Cell of(String content) {
        return new Cell(content == null ? null : List.of(content.split("\n")), null, null, false, false, false, false);
    }

    /**
     * Returns a new cell object containing the specified content and having the
     * specified link associated with it.
     *
     * @param content   the content
     * @param linkURL   the link url
     * @param linkTitle the link title
     * @return the cell object
     */
    public static Cell of(String content, String linkURL, String linkTitle) {
        return new Cell(content == null ? null : List.of(content.split("\n")), linkURL, linkTitle, false, false, false,
                false);
    }

    /**
     * Returns a new cell object containing the specified content and style.
     *
     * @param content         the content
     * @param isBold          whether the content of this cell should be bold
     * @param isUnderlined    whether the content of this cell should be underlined
     * @param isStrikethrough whether the content of this cell should be
     *                        strikethrough
     * @param isItalic        whether the content of this cell should be italic
     * @return the cell object
     */
    public static Cell of(String content, boolean isBold, boolean isUnderlined, boolean isStrikethrough,
                          boolean isItalic) {
        return new Cell(content == null ? null : List.of(content.split("\n")), null, null, isBold, isUnderlined,
                isStrikethrough, isItalic);
    }

    /**
     * Returns a new cell object containing the specified content and style and
     * having the specified link associated with it.
     *
     * @param content         the content
     * @param linkURL         the link url
     * @param linkTitle       the link title
     * @param isBold          whether the content of this cell should be bold
     * @param isUnderlined    whether the content of this cell should be underlined
     * @param isStrikethrough whether the content of this cell should be
     *                        strikethrough
     * @param isItalic        whether the content of this cell should be italic
     * @return the cell object
     */
    public static Cell of(String content, String linkURL, String linkTitle, boolean isBold, boolean isUnderlined,
                          boolean isStrikethrough, boolean isItalic) {
        return new Cell(content == null ? null : List.of(content.split("\n")), linkURL, linkTitle, isBold, isUnderlined,
                isStrikethrough, isItalic);
    }

    /**
     * Returns a new cell object containing the specified content and style.
     *
     * @param content the content
     * @param style   the style
     * @return the cell object
     */
    public static Cell of(String content, int style) {
        return new Cell(content == null ? null : List.of(content.split("\n")), null, null, (style & STYLE_BOLD) != 0,
                (style & STYLE_UNDERLINED) != 0, (style & STYLE_STRIKETHROUGH) != 0, (style & STYLE_ITALIC) != 0);
    }

    /**
     * Returns a new cell object containing the specified content and style and
     * having the specified link associated with it.
     *
     * @param content   the content
     * @param linkURL   the link url
     * @param linkTitle the link title
     * @param style     the style
     * @return the cell object
     */
    public static Cell of(String content, String linkURL, String linkTitle, int style) {
        return new Cell(content == null ? null : List.of(content.split("\n")), linkURL, linkTitle,
                (style & STYLE_BOLD) != 0, (style & STYLE_UNDERLINED) != 0, (style & STYLE_STRIKETHROUGH) != 0,
                (style & STYLE_ITALIC) != 0);
    }

    /**
     * Returns a new cell object containing the specified content.
     *
     * @param content the content
     * @return the cell object
     */
    public static Cell of(List<String> content) {
        return new Cell(content, null, null, false, false, false, false);
    }

    /**
     * Returns a new cell object containing the specified content and having the
     * specified link associated with it.
     *
     * @param content   the content
     * @param linkURL   the link url
     * @param linkTitle the link title
     * @return the cell object
     */
    public static Cell of(List<String> content, String linkURL, String linkTitle) {
        return new Cell(content, linkURL, linkTitle, false, false, false, false);
    }

    /**
     * Returns a new cell object containing the specified content and style.
     *
     * @param content         the content
     * @param isBold          whether the content of this cell should be bold
     * @param isUnderlined    whether the content of this cell should be underlined
     * @param isStrikethrough whether the content of this cell should be
     *                        strikethrough
     * @param isItalic        whether the content of this cell should be italic
     * @return the cell object
     */
    public static Cell of(List<String> content, boolean isBold, boolean isUnderlined, boolean isStrikethrough,
                          boolean isItalic) {
        return new Cell(content, null, null, isBold, isUnderlined, isStrikethrough, isItalic);
    }

    /**
     * Returns a new cell object containing the specified content and style and
     * having the specified link associated with it.
     *
     * @param content         the content
     * @param linkURL         the link url
     * @param linkTitle       the link title
     * @param isBold          whether the content of this cell should be bold
     * @param isUnderlined    whether the content of this cell should be underlined
     * @param isStrikethrough whether the content of this cell should be
     *                        strikethrough
     * @param isItalic        whether the content of this cell should be italic
     * @return the cell object
     */
    public static Cell of(List<String> content, String linkURL, String linkTitle, boolean isBold, boolean isUnderlined,
                          boolean isStrikethrough, boolean isItalic) {
        return new Cell(content, linkURL, linkTitle, isBold, isUnderlined, isStrikethrough, isItalic);
    }

    /**
     * Returns a new cell object containing the specified content and style.
     *
     * @param content the content
     * @param style   the style
     * @return the cell object
     */
    public static Cell of(List<String> content, int style) {
        return new Cell(content, null, null, (style & STYLE_BOLD) != 0, (style & STYLE_UNDERLINED) != 0,
                (style & STYLE_STRIKETHROUGH) != 0, (style & STYLE_ITALIC) != 0);
    }

    /**
     * Returns a new cell object containing the specified content and style and
     * having the specified link associated with it.
     *
     * @param content   the content
     * @param linkURL   the link url
     * @param linkTitle the link title
     * @param style     the style
     * @return the cell object
     */
    public static Cell of(List<String> content, String linkURL, String linkTitle, int style) {
        return new Cell(content, linkURL, linkTitle, (style & STYLE_BOLD) != 0, (style & STYLE_UNDERLINED) != 0,
                (style & STYLE_STRIKETHROUGH) != 0, (style & STYLE_ITALIC) != 0);
    }
}