package jrask;

import oi.thekraken.grok.api.Grok;
import oi.thekraken.grok.api.Match;
import oi.thekraken.grok.api.exception.GrokException;
import org.junit.Test;
import ru.lanwen.verbalregex.VerbalExpression;

import static ru.lanwen.verbalregex.VerbalExpression.regex;


public class VerbalExpressionWithGrok {

    String logLine = "3\t4\t1\thttp://localhost:20001\t1\t63528800\t0\t63528800\t1000000000\t0\t63528800\tSTR1";

    VerbalExpression.Builder digits = regex().capt().digit().oneOrMore().endCapt();
    VerbalExpression.Builder tab = regex().capt().tab();
    VerbalExpression.Builder range = regex().capt().range("0", "1").count(1).endCapt();
    VerbalExpression.Builder str = regex().capt().find("STR").range("0", "2").count(1);
    VerbalExpression.Builder host = regex()
            .capt()
            .then("http")
            .maybe("s")
            .then("://")
            .maybe("www.")
            .anythingBut("\t")
            .endCapt();


    @Test
    public void testWithParser(){
        Parser parser = Parser.builder()
                .extract(digits).into("digit1")
                .tab()
                .extract(digits).into("digit2")
                .tab()
                .extract(range).into("range1")
                .tab()
                .extract(host).into("host")
                .build();

        System.out.println(parser.parse(logLine));
    }

    @Test
    public void testWithParserAutoTabSeparated(){
        Parser parser = Parser.builder()
                .withTabSeparation()
                .extract(digits).into("digit1")
                .extract(digits).into("digit2")
                .extract(range).into("range1")
                .extract(host).into("host")
                .extract(range).into("range2")
                .extract(digits).into("digit4")
                .extract(range).into("range3")
                .extract(digits).into("digit5")
                .extract(digits).into("digit6")
                .extract(range).into("range4")
                .extract(digits).skip()
                .extract(str).into("str")
                .build();

        System.out.println(parser.parse(logLine));
    }

    /**
     * How the same thing could be accomplished without Parser
     */
    @Test
    public void testVerbalExpressionWithGrok() throws GrokException {

        Grok grok = new Grok();

        grok.addPattern("DIGIT", digits.build().toString());
        grok.addPattern("RANGE", range.build().toString());
        grok.addPattern("HOST", host.build().toString());
        grok.addPattern("TAB", tab.build().toString());
        grok.addPattern("STR", str.build().toString());
        grok.compile("%{DIGIT:no1}%{TAB}%{DIGIT:no2}%{TAB}%{RANGE:range1}%{TAB}%{HOST:host}%{TAB}%{RANGE:range2}%{TAB}%" +
                "{DIGIT:no3}%{TAB}%{RANGE:range3}%{TAB}%{DIGIT:no4}%{TAB}%{DIGIT:no5}%{TAB}%{RANGE:range4}%{TAB}%{DIGIT:no6}%{TAB}%{STR:str}");
        Match match = grok.match(logLine);
        match.captures();
        System.out.println(match.toJson());

    }


}
