package com.twasyl.slideshowfx.content.extension.quote;

import com.twasyl.slideshowfx.content.extension.AbstractContentExtension;
import com.twasyl.slideshowfx.content.extension.quote.controllers.QuoteContentExtensionController;
import com.twasyl.slideshowfx.markup.IMarkup;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The QuoteContentExtension extends the AbstractContentExtension. It allows to build a content containing quote to insert
 * inside a SlideshowFX presentation.
 * This extension doesn't use other resources
 * This extension supports HTML and Textile markup languages.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0
 */
public class QuoteContentExtension extends AbstractContentExtension {
    private static final Logger LOGGER = Logger.getLogger(QuoteContentExtension.class.getName());

    private QuoteContentExtensionController controller;

    public QuoteContentExtension() {
        super("QUOTE", null,
                FontAwesomeIcon.QUOTE_LEFT,
                "Insert a quote",
                "Insert a quote");
    }

    @Override
    public Pane getUI() {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("/com/twasyl/slideshowfx/content/extension/quote/fxml/QuoteContentExtension.fxml"));
        Pane root = null;

        try {
            loader.setClassLoader(getClass().getClassLoader());
            root = loader.load();
            this.controller = loader.getController();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Can not load UI for QuoteContentExtension", e);
        }

        return root;
    }

    @Override
    public String buildContentString(IMarkup markup) {
        final StringBuilder builder = new StringBuilder();

        if(markup == null || "HTML".equals(markup.getCode())) {
            builder.append(this.buildDefaultContentString());
        } else if("TEXTILE".equals(markup.getCode())) {
            builder.append("bq.. ")
                    .append(this.controller.getQuote())
                    .append("\np{text-align: right; font-weight: bold; font-style: italic;}. ")
                    .append(this.controller.getAuthor());
        } else {
            builder.append(this.buildDefaultContentString());
        }

        return builder.toString();
    }

    @Override
    public String buildDefaultContentString() {

        final StringBuilder builder = new StringBuilder();
        builder.append("<blockquote><p>")
                .append(this.controller.getQuote())
                .append("</p></blockquote>\n<p style=\"text-align: right; font-weight: bold; font-style: italic;\">")
                .append(this.controller.getAuthor())
                .append("</p>");

        return builder.toString();
    }
}
