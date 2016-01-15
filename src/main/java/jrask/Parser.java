package jrask;

import oi.thekraken.grok.api.Grok;
import oi.thekraken.grok.api.Match;
import oi.thekraken.grok.api.exception.GrokException;
import ru.lanwen.verbalregex.VerbalExpression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.String.format;

import static ru.lanwen.verbalregex.VerbalExpression.regex;

/**
 *
 * Wraps construction of GROK patterns in an easy to use builder.
 *
 * Use when you cannot find the GROK patterns you are looking for and/or you suck
 * at regex and/or you want your regex + grok patterns to understandable.
 *
 * Combines VerbalExpressions, String regex and GROK patterns so you can mix and match
 * in the way that works for you.
 *
 * @author Johan Rask
 *
 */
public class Parser {

    private final Grok grok;

    private Parser(Grok grok) {
        this.grok = grok;
    }

    public static Builder builder() {
        return new Builder(new Grok());
    }

    public static Builder builder(Grok grok) {
        return new Builder(grok);
    }

    // Consider using jackson
    public String parseAsJson(String text) {
        Match match = grok.match(text);
        match.captures();
        return match.toJson();
    }

    public Map<String, Object> parseAsMap(String text) {
        Match match = grok.match(text);
        match.captures();
        return match.toMap();
    }

    public static class Builder {

        private final List<Field> fields = new ArrayList<>();
        private final Grok grok;
        private boolean tabSeparated = false;
        private boolean spaceSeparated = false;

        public Builder(Grok grok) {
            this.grok = grok;
        }

        public Field extract(VerbalExpression.Builder regex) {
            Field field = new Field(regex.build().toString());
            fields.add(field);
            return field;
        }

        public Builder skip(VerbalExpression.Builder regex) {
            Field field = new Field(regex.build().toString());
            fields.add(field);
            return field.skip();
        }

        public Field extract(String regex) {
            Field field = new Field(regex);
            fields.add(field);
            return field;
        }

        public Builder grok(String s) {
            return extract(s).into(null);
        }

        public Builder skip(String regex) {
            Field field = new Field(regex);
            fields.add(field);
            return field.skip();
        }

        public Builder tab() {
            return skip(regex().tab());
        }

        public Builder space() {
            return skip(regex().space());
        }


        public Builder withTabSeparation() {
            this.tabSeparated = true;
            return this;
        }

        public Builder withSpaceSeparation() {
            this.spaceSeparated = true;
            return this;
        }

        public Parser build() {
            return build(false);
        }

        public Parser build(boolean uglyDebug) {
            StringBuilder grokPatternBuilder = new StringBuilder();
            final AtomicBoolean isFirst = new AtomicBoolean(true);
            fields.stream().forEachOrdered(field -> {
                try {

                    if (isFirst.get() == true) {
                        isFirst.set(false);
                    } else {
                        perhapsAppend(grokPatternBuilder);
                    }

                    if (field.regex != null && field.name != null) {
                        grok.addPattern(field.name.toUpperCase(), field.regex);
                        grokPatternBuilder.append("%" + format("{%s:%s}", field.name.toUpperCase(),
                                (field.garbage ? "UNWANTED" : field.name)));
                    } else {
                        grokPatternBuilder.append(field.regex);
                    }
                } catch (GrokException e) {
                    throw new RuntimeException(e);
                }
            });
            try {
                if (uglyDebug) {
                    System.out.println("Grok pattern:" + grokPatternBuilder);
                }
                grok.compile(grokPatternBuilder.toString());
            } catch (GrokException e) {
                throw new RuntimeException(e);
            }
            return new Parser(grok);
        }

        private void perhapsAppend(StringBuilder grokPatternBuilder) throws GrokException {
            if (tabSeparated) {
                grok.addPattern("TAB", regex().tab().build().toString());
                grokPatternBuilder.append("%" + format("{%s:%s}", "TAB", "UNWANTED"));
            }

            if (spaceSeparated) {
                grok.addPattern("SPACE", regex().space().build().toString());
                grokPatternBuilder.append("%" + format("{%s:%s}", "SPACE", "UNWANTED"));
            }
        }


        class Field {

            private final String regex;
            private String name;
            private boolean garbage = false;

            Field(String regex) {
                this.regex = regex;
            }

            public Parser.Builder into(String name) {
                this.name = name;
                return Builder.this;
            }

            private Parser.Builder skip() {
                this.name = Long.toHexString(Double.doubleToLongBits(Math.random()));;
                this.garbage = true;
                return Builder.this;
            }
        }
    }


}
