# Uses VerbalExpressions together with Grok

Demonstrates how these can be used to parse and extract values from log files into a json structure


## Sample

Using sample from VerbalExpressions homepage https://github.com/VerbalExpressions/JavaVerbalExpressions/wiki/Parse-long-strings-example


```java

    String logLine = "3\t4\t1\thttp://localhost:20001\t1\t63528800\t0\t63528800\t1000000000\t0\t63528800\tSTR1";

    // Setup expressions
    
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
    
    
    // Parse
    
    Parser parser = Parser.builder()
                    .withTabSeparation()
                    .extract(digits).skip()
                    .extract(digits).into("digit2")
                    .extract(range).skip()
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

```

```json

    {
    "digit2":4,
    "digit4":63528800,
    "digit5":63528800,
    "digit6":1000000000,
    "host":"http://localhost:20001",
    "range2":1,
    "range3":0,
    "range4":0,
    "str":"STR1"
    }

```