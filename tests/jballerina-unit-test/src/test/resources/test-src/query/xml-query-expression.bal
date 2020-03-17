
function testSimpleQueryExprForXML() returns xml{
    xml book1 = xml `<book>
                            <name>Sherlock Holmes</name>
                            <author>Sir Arthur Conan Doyle</author>
                        </book>`;

    xml book2 = xml `<book>
                            <name>The Da Vinci Code</name>
                            <author>Dan Brown</author>
                    </book>`;

    xml book = book1 + book2;

    xml authors = from var x in book/<name>
                    select <xml> x;

    return  authors;
}

function testSimpleQueryExprForXML2() returns xml{

    xml theXml = xml `<book>the book</book>`;
    xml bitOfText = xml `bit of text\u2702\u2705`;
    xml compositeXml = theXml + bitOfText;

    xml finalOutput = from var elem in compositeXml
                            select <xml> elem;

    return  finalOutput;
}

function testSimpleQueryExprForXML3() returns xml{

    xml bookstore = xml `<bookstore>
                        <book category="cooking">
                            <title lang="en">Everyday Italian</title>
                            <author>Giada De Laurentiis</author>
                            <year>2005</year>
                            <price>30.00</price>
                        </book>
                        <book category="children">
                            <title lang="en">Harry Potter</title>
                            <author>J. K. Rowling</author>
                            <year>2005</year>
                            <price>29.99</price>
                        </book>
                        <book category="web">
                            <title lang="en">XQuery Kick Start</title>
                            <author>James McGovern</author>
                            <author>Per Bothner</author>
                            <author>Kurt Cagle</author>
                            <author>James Linn</author>
                            <author>Vaidyanathan Nagarajan</author>
                            <year>2003</year>
                            <price>49.99</price>
                        </book>
                        <book category="web" cover="paperback">
                            <title lang="en">Learning XML</title>
                            <author>Erik T. Ray</author>
                            <year>2003</year>
                            <price>39.95</price>
                        </book>
                    </bookstore>`;

    xml finalOutput = from var elem in bookstore["book"]["title"]
                            select <xml> elem;

    return  finalOutput;
}
