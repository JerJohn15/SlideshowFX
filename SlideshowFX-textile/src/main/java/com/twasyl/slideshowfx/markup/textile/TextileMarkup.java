/*
 * Copyright 2014 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twasyl.slideshowfx.markup.textile;

import com.twasyl.slideshowfx.markup.IMarkup;
import org.eclipse.mylyn.internal.wikitext.textile.core.TextileContentState;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder;
import org.eclipse.mylyn.wikitext.core.parser.IdGenerator;
import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder;
import org.eclipse.mylyn.wikitext.core.parser.markup.ContentState;
import org.eclipse.mylyn.wikitext.core.parser.markup.IdGenerationStrategy;
import org.eclipse.mylyn.wikitext.core.parser.markup.MarkupLanguage;
import org.eclipse.mylyn.wikitext.textile.core.TextileLanguage;

import java.io.IOException;
import java.io.StringWriter;

/**
 * @author Thierry Wasylczenko
 */
public class TextileMarkup implements IMarkup {
    // The generation strategy generates IDs using the current timestamp
    final IdGenerationStrategy idGenerationStrategy = new IdGenerationStrategy() {
        @Override
        public String generateId(String s) {
            return System.currentTimeMillis() + "";
        }
    };

    final IdGenerator idGenerator = new IdGenerator();

    final TextileContentState contentState = new TextileContentState() {
        @Override
        public IdGenerator getIdGenerator() {
            return idGenerator;
        }
    };

    // Override the language to return the created contentState used for the ID generation
    final MarkupLanguage language = new TextileLanguage() {
        @Override
        protected ContentState createState() {
            return contentState;
        }
    };

    @Override
    public String getCode() {
        return "TEXTILE";
    }

    @Override
    public String getName() {
        return "Textile";
    }

    @Override
    public String convertAsHtml(String markupString) throws IllegalArgumentException {
        if(markupString == null) throw new IllegalArgumentException("Can not convert " + getName() + " to HTML : the String is null");
        final StringWriter writer = new StringWriter();

        idGenerator.setGenerationStrategy(idGenerationStrategy);

        final DocumentBuilder builder = new HtmlDocumentBuilder(writer);

        final MarkupParser parser = new MarkupParser(language, builder);

        parser.parse(markupString, false);

        writer.flush();
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return writer.toString();
    }
}