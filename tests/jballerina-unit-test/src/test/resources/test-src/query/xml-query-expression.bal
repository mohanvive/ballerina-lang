
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