type Person record {|
   string firstName;
   string lastName;
   int age;
|};

type Teacher record {|
   string firstName;
   string lastName;
   int age;
   string teacherId;
|};

function testSimpleQueryExprForString() returns string{
    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    string nameList =
                from var person in personList
                select (person.firstName).concat(" ");

    return nameList;
}

function testQueryExprWithWhere() returns string{
    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    string nameList =
                from var person in personList
                where person.age >= 30
                select (person.firstName).concat(" ");

    return nameList;
}

