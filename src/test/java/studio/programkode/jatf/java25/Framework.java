// SPDX-FileCopyrightText: 2025 Marcus Alexander Dahl (programkode)
// SPDX-License-Identifier: MPL-2.0
package studio.programkode.jatf.java25;

import org.opentest4j.AssertionFailedError;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static java.lang.ScopedValue.where;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.ScopedValue;
import java.lang.reflect.AccessFlag;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


/**
 * JATF - Java Assignment Testing Framework (Java 25)
 *  A class that strictly contains static helper methods to test:
 *  Classes, fields, methods, constructors (class instantiation) and general assertations.
 *  <p>
 *  Scoped testing is performed via `test*(...)`-methods,
 *  and allows you to easily switch testing context by nesting such test-calls.
 *
 */
public class Framework
{
    ///-----------------------------------------------------------------------------------------------------------------
    ///# Section: Standard I/O
    ///-----------------------------------------------------------------------------------------------------------------
    static private final PrintStream stdout = System.out;
    static private final ByteArrayOutputStream output = new ByteArrayOutputStream();


    /**
     * Returns the current standard system output as String-value.
     * <p>
     * Replaces all ASCII control characters (0x00-0x1F and 0x7F (0-31 and 127)) with empty String,
     * while also keeping newlines and tabs.
     *
     * @return String
     */
    static public String getStandardOutput() {
        return Framework.output.toString().replaceAll("[\\p{Cntrl}&&[^\\n\\r\\t]]", "");
    }

    /**
     * Sets the standard output to our local ByteArrayOutputStream used to capture stdout
     */
    static public void setStandardOutput() {
        Framework.setStandardOutput(new PrintStream(Framework.output));
    }

    static public void setStandardOutput(PrintStream stream) {
        System.setOut(stream);
    }

    static public void resetStandardOutput() {
        System.setOut(Framework.stdout);
    }



    ///-----------------------------------------------------------------------------------------------------------------
    ///# Section: Scoped values
    ///
    ///
    /// TODO: class constructor scoping
    /// TODO: class instance scoping
    ///-----------------------------------------------------------------------------------------------------------------
    static private final ScopedValue<Class<?>> CLASS = ScopedValue.newInstance();
    static private final ScopedValue<Method> METHOD = ScopedValue.newInstance();
    static private final ScopedValue<Field> FIELD = ScopedValue.newInstance();

    static public Class<?> getScopedClass() {
        return CLASS.get();
    }

    static public Method getScopedMethod() {
        return METHOD.get();
    }

    static public Field getScopedField() {
        return FIELD.get();
    }



    ///-----------------------------------------------------------------------------------------------------------------
    ///# Section: Classes
    ///-----------------------------------------------------------------------------------------------------------------
    static public Optional<Class<?>> findClass(String pkg, String className) {
        return Framework.findClass(Framework.FQCN(pkg, className));
    }

    static public Optional<Class<?>> findClass(String fullyQualifiedClassName) {
        try {
            return Optional.of(Class.forName(fullyQualifiedClassName));
        } catch (ClassNotFoundException _) {
            return Optional.empty();
        }
    }


    static public void testClass(String pkg, String className, Runnable fn) {
        Framework.testClass(Framework.FQCN(pkg, className), fn);
    }

    static public void testClass(String fullyQualifiedClassName, Runnable fn) {
        var classObject = Framework.findClass(fullyQualifiedClassName);

        if (classObject.isEmpty()) {
            Framework.throwClassNotFound(fullyQualifiedClassName);
        }
        else {
            Framework.testClass(classObject.get(), fn);
        }
    }

    static public void testClass(Class<?> classObject, Runnable fn) {
        where(Framework.CLASS, classObject).run(fn);
    }


    static public boolean classExists(String pkg, String className) {
        return Framework.classExists(Framework.FQCN(pkg, className));
    }

    static public boolean classExists(String fullyQualifiedClassName) {
        return Framework.findClass(fullyQualifiedClassName).isPresent();
    }


    static public boolean classIsInnerClass(Class<?> classObject) {
        return classObject.isMemberClass();
    }

    /** Scoped CLASS */
    static public boolean classIsInnerClass() {
        return Framework.classIsInnerClass(CLASS.get());
    }


    /** Scoped CLASS */
    static public boolean classInheritsFrom(Class<?> classObject) {
        var superClass = CLASS.get().getSuperclass();

        do {
            if (superClass.equals(classObject)) {
                return true;
            }

            superClass = superClass.getSuperclass();
        } while (superClass != null);

        return false;
    }


    /** Scoped CLASS */
    static public Object classCreateInstance(Object... parameterValues) {
        return Framework.classCreateInstance(CLASS.get(), parameterValues);
    }

    static public Object classCreateInstance(Class<?> classObject, Object... parameterValues) {
        var signature = Arrays.stream(parameterValues).map(Object::getClass).toArray(Class<?>[]::new);

        try {
            var constructor = signature.length == 0
                ? classObject.getConstructor()
                : classObject.getConstructor(signature);
            ;

            return constructor.newInstance(parameterValues);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            // TODO: better feedback per exception
            throw new RuntimeException(e);
        }
    }


    /** Scoped CLASS */
    static public void classInstanceInvokeMethod(Object instance, String methodName, Object... parameterValues) {
        var signature = Arrays.stream(parameterValues).map(Object::getClass).toArray(Class<?>[]::new);

        try {
            var method = parameterValues.length == 0
                ? CLASS.get().getMethod(methodName)
                : CLASS.get().getMethod(methodName, signature)
            ;

            method.invoke(instance, parameterValues);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            // TODO: better feedback per exception
            throw new RuntimeException(e);
        }
    }


    ///-----------------------------------------------------------------------------------------------------------------
    ///# Section: Methods
    ///
    ///
    /// TODO: support FQCN#methodName(...) syntax as method specifier
    ///-----------------------------------------------------------------------------------------------------------------
    static public Optional<Method> findMethod(
            String pkg, String className,
            String methodName, Class<?>... parameterTypes
    ) {
        return Framework.findMethod(Framework.FQCN(pkg, className), methodName, parameterTypes);
    }

    static public Optional<Method> findMethod(
            String fullyQualifiedClassName,
            String methodName, Class<?>... parameterTypes
    ) {
        var classObject = Framework.findClass(fullyQualifiedClassName);

        if (classObject.isPresent()) {
            return Framework.findMethod(classObject.get(), methodName, parameterTypes);
        }

        return Optional.empty();
    }

    static public Optional<Method> findMethod(Class<?> classObject, String methodName, List<Class<?>> parameterTypes) {
        return Framework.findMethod(classObject, methodName, parameterTypes.toArray(new Class[0]));
    }

    static public Optional<Method> findMethod(Class<?> classObject, String methodName, Class<?>... parameterTypes) {
        try {
            return parameterTypes.length == 0
                ? Optional.of(classObject.getMethod(methodName))
                : Optional.of(classObject.getMethod(methodName, parameterTypes))
            ;
        } catch (NoSuchMethodException _) {
            return Optional.empty();
        }
    }


    static public Optional<Method> findDeclaredMethod(
            String pkg, String className,
            String methodName, Class<?>... parameterTypes
    ) {
        return Framework.findDeclaredMethod(Framework.FQCN(pkg, className), methodName, parameterTypes);
    }

    static public Optional<Method> findDeclaredMethod(
            String fullyQualifiedClassName,
            String methodName, Class<?>... parameterTypes
    ) {
        var classObject = Framework.findClass(fullyQualifiedClassName);

        if (classObject.isPresent()) {
            return Framework.findDeclaredMethod(classObject.get(), methodName, parameterTypes);
        }

        return Optional.empty();
    }

    static public Optional<Method> findDeclaredMethod(
            Class<?> classObject,
            String methodName, List<Class<?>> parameterTypes
    ) {
        return Framework.findDeclaredMethod(classObject, methodName, parameterTypes.toArray(new Class[0]));
    }

    static public Optional<Method> findDeclaredMethod(
            Class<?> classObject,
            String methodName, Class<?>... parameterTypes
    ) {
        try {
            return parameterTypes.length == 0
                ? Optional.of(classObject.getDeclaredMethod(methodName))
                : Optional.of(classObject.getDeclaredMethod(methodName, parameterTypes))
            ;
        } catch (NoSuchMethodException _) {
            return Optional.empty();
        }
    }


    static public void testMethod(
            String pkg, String className,
            String methodName, List<Class<?>> parameterTypes,
            Runnable fn
    ) {
        Framework.testMethod(FQCN(pkg, className), methodName, parameterTypes, fn);
    }

    static public void testMethod(
            String fullyQualifiedClassName,
            String methodName, List<Class<?>> parameterTypes,
            Runnable fn
    ) {
        var classObject = findClass(fullyQualifiedClassName);

        if (classObject.isEmpty()) {
            Framework.throwClassNotFound(fullyQualifiedClassName);
        }

        Framework.testMethod(classObject.get(), methodName, parameterTypes, fn);
    }

    static public void testMethod(Class<?> classObject, String methodName, List<Class<?>> parameterTypes, Runnable fn) {
        Framework.findMethod(classObject, methodName, parameterTypes.toArray(new Class[0])).ifPresent(
            method -> Framework.testMethod(method, fn)
        );
    }

    /** Scoped CLASS */
    static public void testMethod(String methodName, Runnable fn) {
        Framework.findMethod(CLASS.get(), methodName, new Class[0]).ifPresent(
                method -> Framework.testMethod(method, fn)
        );
    }

    /** Scoped CLASS */
    static public void testMethod(String methodName, List<Class<?>> parameterTypes, Runnable fn) {
        Framework.findMethod(CLASS.get(), methodName, parameterTypes.toArray(new Class[0])).ifPresent(
            method -> Framework.testMethod(method, fn)
        );
    }

    static public void testMethod(Method methodObject, Runnable fn) {
        where(Framework.METHOD, methodObject).run(fn);
    }


    static public void testDeclaredMethod(
            String pkg, String className,
            String methodName, List<Class<?>> parameterTypes,
            Runnable fn
    ) {
        Framework.testDeclaredMethod(FQCN(pkg, className), methodName, parameterTypes, fn);
    }

    static public void testDeclaredMethod(
            String fullyQualifiedClassName,
            String methodName, List<Class<?>> parameterTypes,
            Runnable fn
    ) {
        var classObject = findClass(fullyQualifiedClassName);

        if (classObject.isEmpty()) {
            Framework.throwClassNotFound(fullyQualifiedClassName);
        }

        Framework.testDeclaredMethod(classObject.get(), methodName, parameterTypes, fn);
    }

    static public void testDeclaredMethod(
            Class<?> classObject,
            String methodName, List<Class<?>> parameters,
            Runnable fn
    ) {
        var parameterTypes = parameters.toArray(new Class[0]);
        var methodObject = Framework.findMethod(classObject, methodName, parameterTypes);

        if (methodObject.isEmpty()) {
            Framework.throwClassMethodNotFound(classObject.getName(), methodName, parameterTypes);
        }

        Framework.testMethod(methodObject.get(), fn);
    }

    /** Scoped CLASS */
    static public void testDeclaredMethod(String methodName, List<Class<?>> parameterTypes, Runnable fn) {
        Framework.findDeclaredMethod(CLASS.get(), methodName, parameterTypes.toArray(new Class[0])).ifPresent(
            method -> Framework.testMethod(method, fn)
        );
    }


    static public void testClassMethod(
            String pkg, String className,
            String methodName,
            Runnable fn
    ) {
        Framework.testClassMethod(pkg, className, methodName, List.of(), fn);
    }

    static public void testClassMethod(
            String pkg, String className,
            String methodName, List<Class<?>> parameterTypes,
            Runnable fn
    ) {
        Framework.testClassMethod(Framework.FQCN(pkg, className), methodName, parameterTypes, fn);
    }

    static public void testClassMethod(
            String fullyQualifiedClassName,
            String methodName, List<Class<?>> parameters,
            Runnable fn
    ) {
        var classOptional = Framework.findClass(fullyQualifiedClassName);

        if (classOptional.isEmpty()) {
            Framework.throwClassNotFound(fullyQualifiedClassName);
        }

        where(Framework.CLASS, classOptional.get()).run(() -> {
            var parameterTypes = parameters.toArray(new Class[0]);
            var methodOptional = Framework.findMethod(CLASS.get(), methodName, parameterTypes);

            if (methodOptional.isEmpty()) {
                Framework.throwClassMethodNotFound(fullyQualifiedClassName, methodName, parameterTypes);
            }

            where(Framework.METHOD, methodOptional.get()).run(fn);
        });
    }


    static public boolean methodExists(String pkg, String className, String methodName, Class<?>... parameterTypes) {
        return Framework.methodExists(Framework.FQCN(pkg, className), methodName, parameterTypes);
    }

    static public boolean methodExists(String fullyQualifiedClassName, String methodName, Class<?>... parameterTypes) {
        return Framework.findMethod(fullyQualifiedClassName, methodName, parameterTypes).isPresent();
    }

    static public boolean methodExists(Class<?> classObject, String methodName, Class<?>... parameterTypes) {
        return Framework.findMethod(classObject, methodName, parameterTypes).isPresent();
    }

    /** Scoped CLASS */
    static public boolean methodExists(String method, Class<?>... parameterTypes) {
        return findMethod(CLASS.get(), method, parameterTypes).isPresent();
    }


    static public boolean methodExistsDeclared(String pkg, String className, String methodName, Class<?>... parameterTypes) {
        return Framework.methodExistsDeclared(Framework.FQCN(pkg, className), methodName, parameterTypes);
    }

    static public boolean methodExistsDeclared(String fullyQualifiedClassName, String methodName, Class<?>... parameterTypes) {
        return Framework.findDeclaredMethod(fullyQualifiedClassName, methodName, parameterTypes).isPresent();
    }

    static public boolean methodExistsDeclared(Class<?> classObject, String methodName, Class<?>... parameterTypes) {
        return Framework.findDeclaredMethod(classObject, methodName, parameterTypes).isPresent();
    }

    /** Scoped CLASS */
    static public boolean methodExistsDeclared(String method, Class<?>... parameterTypes) {
        return findDeclaredMethod(CLASS.get(), method, parameterTypes).isPresent();
    }


    static public boolean methodReturns(
            String fullyQualifiedClassName,
            String methodName,
            List<Class<?>> parameterTypes,
            Class<?> returnType
    ) {
        var methodObject = Framework.findMethod(fullyQualifiedClassName, methodName, parameterTypes.toArray(new Class[0]));

        return methodObject.map(method -> method.getReturnType().equals(returnType)).orElse(false);
    }

    static public boolean methodReturns(Method methodObject, Class<?> returnType) {
        return methodObject.getReturnType().equals(returnType);
    }

    /** Scoped CLASS+METHOD */
    static public boolean methodReturns(Class<?> returnType) {
        return Framework.methodReturns(METHOD.get(), returnType);
    }


    static public String methodReturnType(
            String pkg, String className,
            String methodName, Class<?>... parameterTypes
    ) {
        return Framework.methodReturnType(FQCN(pkg, className), methodName, parameterTypes);
    }

    static public String methodReturnType(
            String fullyQualifiedClassName,
            String methodName, Class<?>... parameterTypes
    ) {
        var classObject = Framework.findClass(fullyQualifiedClassName);

        if (classObject.isEmpty()) {
            Framework.throwClassNotFound(fullyQualifiedClassName);
        }

        return Framework.methodReturnType(classObject.get(), methodName, parameterTypes);
    }

    static public String methodReturnType(
            Class<?> classObject, String methodName, Class<?>... parameterTypes
    ) {
        var methodObject = Framework.findMethod(classObject, methodName, parameterTypes);

        if (methodObject.isEmpty()) {
            Framework.throwClassMethodNotFound(classObject.getName(), methodName, parameterTypes);
        }

        return Framework.methodReturnType(methodObject.get());
    }

    static public String methodReturnType(Method methodObject) {
        return Framework.getTypeName(methodObject.getGenericReturnType());
    }


    /** Scoped METHOD */
    static public boolean methodIsPublic() {
        return (METHOD.get().getModifiers() & Modifier.PUBLIC) != 0;
    }

    /** Scoped METHOD */
    static public boolean methodIsProtected() {
        return (METHOD.get().getModifiers() & Modifier.PROTECTED) != 0;
    }

    /** Scoped METHOD */
    static public boolean methodIsPrivate() {
        return (METHOD.get().getModifiers() & Modifier.PRIVATE) != 0;
    }

    /** Scoped METHOD */
    static public boolean methodIsAbstract() {
        return (METHOD.get().getModifiers() & Modifier.ABSTRACT) != 0;
    }

    /** Scoped METHOD */
    static public boolean methodIsStatic() {
        return (METHOD.get().getModifiers() & Modifier.STATIC) != 0;
    }

    /** Scoped METHOD */
    static public boolean methodIsFinal() {
        return (METHOD.get().getModifiers() & Modifier.FINAL) != 0;
    }

    /** Scoped CLASS+METHOD */
    static public boolean methodIsInherited() {
        return (METHOD.get().getDeclaringClass() != CLASS.get());
    }


    /** Scoped METHOD */
    static public boolean methodHasModifiers(AccessFlag... flags) {
        return Framework.methodHasModifiers(METHOD.get(), flags);
    }

    static public boolean methodHasModifiers(Method methodObject, AccessFlag... flags) {
        var modifiers = methodObject.getModifiers();

        for (var flag : flags) {
            if ((modifiers&flag.mask()) == 0) {
                return false;
            }
        }

        return true;
    }


    // Check if a method overrides an inherited method
    static public boolean methodOverrides() {
        return Framework.methodOverrides(METHOD.get());
    }

    static public boolean methodOverrides(Method methodObject) {
        var superClass = methodObject.getDeclaringClass().getSuperclass();

        while (superClass != null) {
            try {
                if (!superClass.getMethod(methodObject.getName(), methodObject.getParameterTypes()).equals(methodObject)) {
                    return true;
                }
            } catch (NoSuchMethodException e) {}

            superClass = superClass.getSuperclass();
        }

        return false;
    }


    static public boolean mainMethodExists(String pkg, String className) {
        return Framework.mainMethodExists(Framework.FQCN(pkg, className));
    }

    static public boolean mainMethodExists(String fullyQualifiedClassName) {
        var classObject = findClass(fullyQualifiedClassName);

        return classObject.filter(Framework::mainMethodExists).isPresent();

    }

    static public boolean mainMethodExists(Class<?> classObject) {
        for (var method : classObject.getDeclaredMethods()) {
            if (method.getName().equals("main")) {
                return true;
            }
        }

        return false;
    }

    /** SCOPED CLASS */
    static public boolean mainMethodExists() {
        return Framework.mainMethodExists(CLASS.get());
    }



    ///-----------------------------------------------------------------------------------------------------------------
    ///# Section: Fields
    ///
    ///
    /// TODO: support FQCN#fieldName syntax as field specifier
    ///-----------------------------------------------------------------------------------------------------------------
    static public Optional<Field> findField(String pkg, String className, String fieldName) {
        return Framework.findField(FQCN(pkg, className), fieldName);
    }

    static public Optional<Field> findField(String fullyQualifiedClassName, String fieldName) {
        var classObject = Framework.findClass(fullyQualifiedClassName);

        if (classObject.isPresent()) {
            return Framework.findField(classObject.get(), fieldName);
        }

        return Optional.empty();
    }

    static public Optional<Field> findField(Class<?> classObject, String fieldName) {
        try {
            return Optional.of(classObject.getField(fieldName));
        } catch (NoSuchFieldException _) {
            return Optional.empty();
        }
    }


    static public Optional<Field> findDeclaredField(String pkg, String className, String fieldName) {
        return Framework.findDeclaredField(FQCN(pkg, className), fieldName);
    }

    static public Optional<Field> findDeclaredField(String fullyQualifiedClassName, String fieldName) {
        var classObject = Framework.findClass(fullyQualifiedClassName);

        if (classObject.isPresent()) {
            return Framework.findDeclaredField(classObject.get(), fieldName);
        }

        return Optional.empty();
    }

    static public Optional<Field> findDeclaredField(Class<?> classObject, String fieldName) {
        try {
            return Optional.of(classObject.getDeclaredField(fieldName));
        } catch (NoSuchFieldException _) {
            return Optional.empty();
        }
    }

    static public Optional<Field> findAnyField(Class<?> classObject, String fieldName) {
        var field = Framework.findDeclaredField(classObject, fieldName);

        if (field.isEmpty()) {
            field = Framework.findField(classObject, fieldName);

            if (field.isEmpty()) {
                return Optional.empty();
            }
        }

        return field;
    }


    static public void testField(
            String pkg,
            String className,
            String fieldName,
            Runnable fn
    ) {
        Framework.testField(FQCN(pkg, className), fieldName, fn);
    }

    static public void testField(
            String fullyQualifiedClassName,
            String fieldName,
            Runnable fn
    ) {
        Framework.findClass(fullyQualifiedClassName).ifPresent(classObject -> {
            Framework.testField(classObject, fieldName, fn);
        });
    }

    static public void testField(String fieldName, Runnable fn) {
        Framework.testField(CLASS.get(), fieldName, fn);
    }

    static public void testField(Class<?> classObject, String fieldName, Runnable fn) {
        Framework.findAnyField(classObject, fieldName).ifPresent(field -> Framework.testField(field, fn));
    }

    static public void testField(Field fieldObject, Runnable fn) {
        where(Framework.FIELD, fieldObject).run(fn);
    }


    static public void testClassField(String pkg, String className, String fieldName, Runnable fn) {
        Framework.testClassField(FQCN(pkg, className), fieldName, fn);
    }

    static public void testClassField(String fullyQualifiedClassName, String fieldName, Runnable fn) {
        var classObject = Framework.findClass(fullyQualifiedClassName);

        if (classObject.isEmpty()) {
            Framework.throwClassNotFound(fullyQualifiedClassName);
        }

        where(Framework.CLASS, classObject.get()).run(() -> {
            var fieldObject = Framework.findField(CLASS.get(), fieldName);

            if (fieldObject.isEmpty()) {
                Framework.throwClassFieldNotFound(fullyQualifiedClassName, fieldName);
            }

            where(Framework.FIELD, fieldObject.get()).run(fn);
        });
    }


    static public boolean fieldExists(String pkg, String className, String fieldName) {
        return Framework.fieldExists(FQCN(pkg, className), fieldName);
    }

    static public boolean fieldExists(String fullyQualifiedClassName, String fieldName) {
        return Framework.findField(fullyQualifiedClassName, fieldName).isPresent();
    }

    /** Scoped CLASS */
    static public boolean fieldExists(String fieldName) {
        return Framework.findAnyField(CLASS.get(), fieldName).isPresent();
    }

    static public boolean fieldExistsDeclared(String pkg, String className, String fieldName) {
        return Framework.fieldExistsDeclared(FQCN(pkg, className), fieldName);
    }

    static public boolean fieldExistsDeclared(String fullyQualifiedClassName, String fieldName) {
        return Framework.findDeclaredField(fullyQualifiedClassName, fieldName).isPresent();
    }

    /** Scoped CLASS */
    static public boolean fieldExistsDeclared(String fieldName) {
        return Framework.findDeclaredField(CLASS.get(), fieldName).isPresent();
    }

    /** Scoped FIELD */
    static public boolean fieldIsPublic() {
        return (FIELD.get().getModifiers() & Modifier.PUBLIC) != 0;
    }

    /** Scoped FIELD */
    static public boolean fieldIsProtected() {
        return (FIELD.get().getModifiers() & Modifier.PROTECTED) != 0;
    }

    /** Scoped FIELD */
    static public boolean fieldIsPrivate() {
        return (FIELD.get().getModifiers() & Modifier.PRIVATE) != 0;
    }

    /** Scoped FIELD */
    static public boolean fieldIsStatic() {
        return (FIELD.get().getModifiers() & Modifier.STATIC) != 0;
    }

    /** Scoped FIELD */
    static public boolean fieldIsFinal() {
        return (FIELD.get().getModifiers() & Modifier.FINAL) != 0;
    }


    /** Scoped FIELD */
    static public boolean fieldHasModifiers(AccessFlag... flags) {
        return Framework.fieldHasModifiers(FIELD.get(), flags);
    }

    static public boolean fieldHasModifiers(Field fieldObject, AccessFlag... flags) {
        var modifiers = fieldObject.getModifiers();

        for (var flag : flags) {
            if ((modifiers&flag.mask()) == 0) {
                return false;
            }
        }

        return true;
    }


    /** Scoped FÌELD */
    static public String fieldType() {
        return Framework.fieldType(FIELD.get().getGenericType());
    }

    static public String fieldType(Type type) {
        if (type instanceof ParameterizedType parameterizedType) {
            return Framework.fieldParameterizedType(parameterizedType);
        }
        else {
            return Framework.stripPackageFromClassName(type.getTypeName());
        }
    }


    static public String fieldParameterizedType(ParameterizedType type) {
        return Framework.getParameterizedTypeName(type);
    }



    ///-----------------------------------------------------------------------------------------------------------------
    ///# Section: Helper-methods
    ///-----------------------------------------------------------------------------------------------------------------
    static public String getTypeName(Type type) {
        if (type instanceof ParameterizedType parameterizedType) {
            return Framework.getParameterizedTypeName(parameterizedType);
        }
        else {
            return Framework.stripPackageFromClassName(type.getTypeName());
        }
    }


    static public String getParameterizedTypeName(ParameterizedType type) {
        StringBuilder output = new StringBuilder();

        var typeArguments = type.getActualTypeArguments();
        var types = new ArrayList<String>();

        output.append(stripPackageFromClassName(
            ((Class<?>) type.getRawType()).getName()
        ));

        for (var typeArgument : typeArguments) {
            if (typeArgument instanceof Class) {
                types.add(Framework.stripPackageFromClassName(((Class<?>) typeArgument).getName()));
            }
            else if (typeArgument instanceof ParameterizedType parameterizedType) {
                types.add(Framework.getParameterizedTypeName(parameterizedType));
            }
            else {
                types.add(Framework.stripPackageFromClassName(typeArgument.toString()));
            }
        }

        output.append("<");
        output.append(String.join(", ", types));
        output.append(">");

        return output.toString();
    }


    static public String stripPackageFromClassName(String fullyQualifiedClassName) {
        return List.of(fullyQualifiedClassName.split("\\.")).getLast();
    }


    static private String FQCN(String pkg, String className) {
        return String.format("%s.%s", pkg, className);
    }


    //## Assertions
    static public void assertStandardOutputEquals(String input) {
        assertEquals("\"%s\"".formatted(input), "\"%s\"".formatted(Framework.getStandardOutput()));
    }


    // TODO: improve later, goal is to preserve indentation when removing leading whitespace from multiline string given
    // TODO: handle when first line does not set the proper (intended) indentation level
    static public void provideHintIfAssertionFails(String hint, Runnable fn) {
        try {
            fn.run();
        } catch (AssertionFailedError e) {
            Framework.resetStandardOutput();

            IO.println();

            if (hint.lines().count() == 1) {
                IO.println(hint);
            }
            else {
                var firstLine = hint.lines().findFirst();

                if (firstLine.isPresent()) {
                    var indentation = firstLine.get().length() - firstLine.get().stripLeading().length();

                    hint.lines().forEach(line -> {
                        if (line.length() > indentation) IO.println(line.substring(indentation));
                        else IO.println(line.stripLeading());
                    });
                }
            }

            Framework.setStandardOutput();

            throw e;
        }
    }


    static private void throwClassNotFound(String fullyQualifiedClassName) {
        throw new AssertionFailedError(
            "Class not found: %s".formatted(fullyQualifiedClassName),
            "Class located at src/main/java/%s.java".formatted(fullyQualifiedClassName.replaceAll("\\.", "/")),
            "Class not found"
        );
    }


    static private void throwClassMethodNotFound(
            String fullyQualifiedClassName,
            String methodName, Class<?>... parameterTypes
    ) {
        throw new AssertionFailedError(
            "Class-method not found: %s.%s(%s)".formatted(
                fullyQualifiedClassName, methodName, parameterTypes.length == 0 ? "" : Arrays.toString(parameterTypes)
            ),
            "Method to exist within class",
            "Method within class does not exist"
        );
    }


    static private void throwClassFieldNotFound(String fullyQualifiedClassName, String fieldName) {
        throw new AssertionFailedError(
            "Class-field not found: %s#%s".formatted(
                fullyQualifiedClassName, fieldName
            ),
            "Field to exist within class",
            "Field within class does not exist"
        );
    }



    ///-----------------------------------------------------------------------------------------------------------------
    ///# Note: Private constructor; prevent instantiation of this class as it strictly contains static helper methods
    private Framework() {}
}
