# Log parsing made simple

Log parsing can be a pain, you have to really understand regex (not only write them...), grok can be really useful
if patterns exist. I recently found VerbalExpressions ( https://github.com/VerbalExpressions ) which are really nice
to write regex that are easy to understand and modify (if you are careful...).

*This library uses regex, grok and VerbalExpression to create a log/text parser that should be simple to work with and
you can mix regex(string), grok patterns & VerbalExpressions to combine a single pattern.*


## Sample

Using sample from VerbalExpressions homepage https://github.com/VerbalExpressions/JavaVerbalExpressions/wiki/Parse-long-strings-example

```java
// Line to parse

String logLine = "3\t4\t1\thttp://localhost:20001\t1\t63528800\t0\t63528800\t1000000000\t0\t63528800\tSTR1";

// Setup expressions

VerbalExpression.Builder digits = regex().capt().digit().oneOrMore().endCapt();
VerbalExpression.Builder tab = regex().capt().tab();
VerbalExpression.Builder range = regex().capt().range("0", "1").count(1).endCapt();

// Create a grok instance IF you want to use any predefined patterns
Grok grok = Grok.create("src/main/resources/patterns/patterns");

// Build parser
Parser parser = Parser.builder(grok)
     .extract("((?:\\d)+)").into("digit1")  // Regular regex
     .tab()
     .extract(digits).into("digit2")        // VerbalExpression
     .tab()
     .skip(range)                           // Does not save value
     .tab()
     .grok("%{URI:host}")                   // Use predefined grok pattern
     .build();
     
// Parse into json     
String json = parser.parseAsJson(logLine);

```
Result

```json
{
    "digit1": 3,
    "digit2": 4,
    "host": "http://localhost:20001",
}
```
    
Auto tab separation

```java
Parser parser = Parser.builder()
    .withTabSeparation()
    .extract("((?:\\d)+)").into("digit1") 
    .extract(digits).into("digit2")       
    .skip(range)                          
    .grok("%{URI:host}")
    .build();
```

# TODO

Dist
