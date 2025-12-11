# Java Assignment Testing Frame

The goal of this testing framework is to make it easier to write automated tests for checking course work where Java 25 is used.

---

## Technology

> Java 25

> JUnit 6

---

# How to use:

> Meant to be used within IntelliJ IDEA, recommended version: 2025.3
> 
> Tests can also be run via `mvn test` (For use with GitHub Actions)

## `src/main/java/assignment/`

Code goes here

### `src/main/java/assignment/README.md`

Assignment text goes here

## `src/test/java/assignment/`

### `src/test/java/assignment/TestAssignment.java`

Code that tests the assignment goes here

---

## GitHub Action

> `.github/workflows/test-assignment.yml`

```yml
name: Maven CI with Java 25

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v5.0.1

      - name: Set up Java 25
        uses: actions/setup-java@v5.1.0
        with:
          distribution: 'oracle'
          java-version: '25'
          cache: 'maven'

      - name: Build with Maven and run tests
        run: mvn test
```

---

## Examples: Testing assignment code

> Examples below are just a subset of what is available, docs are work-in-progress

```java
testClass("assignment.Person", () -> {
    assertTrue(fieldExists("name"));
    assertTrue(methodExists("getName"));
    assertTrue(methodExists("setName", String.class));
    assertTrue(methodExists("printName"));
    
    var Person = getScopedClass();

    testClass("assignment.Student", () -> {
        assertTrue(classInheritsFrom(Person));
    });
});

testClass("assignment.Student", () -> {
    var name = "Alice";
    var student = classCreateInstance(name);
    
    classInstanceInvokeMethod(instance, "printName");
    
    assertStandardOutputEquals(name);
});

testClassMethod("assignment.Person", "getName", () -> {
    assertFalse(methodIsStatic());
    
    assertTrue(methodHasModifiers(AccessFlag.PUBLIC));
    
    assertTrue(methodReturns(String.class));
});

testClassMethod("assignment.Student", "toString", () -> {
    testMethod("toString", () -> {
        provideHintIfAssertionFails(
            String.format(
                "Currently Student#toString() is inherited from '%s', declare your own to override it",
                getScopedMethod().getDeclaringClass().getName()
            ),
            () -> {
                assertFalse(methodIsInherited());
            }
        );
    });
});
```


### Tips:

- Use `message` parameter in `assert*()`-methods to provide more detailed feedback
- If you are writing tests locally and from time to time are pulling changes from origin or upstream (in a fork), use `src/main/java/[local|dev]/**` and `src/test/java/[local|dev]/**` as these are included in the gitignore 


# Plans

- File I/O
- Database I/O (JDBC)
- Snapshot testing
- Relevant unit testing as part of the framework
- Testing the testing framework + solve "Quid revera probationes probabunt?"
- Deeper testing via bytecode analysis
- Testing of algorithmic complexity via bytecode analysis
