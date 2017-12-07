package com.github.keithwoelke.assertion;

import com.google.common.collect.Lists;
import io.restassured.assertion.BodyMatcherGroup;
import io.restassured.assertion.CookieMatcher;
import io.restassured.assertion.HeaderMatcher;
import io.restassured.config.RestAssuredConfig;
import io.restassured.internal.ResponseParserRegistrar;
import io.restassured.internal.ResponseSpecificationImpl;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import org.hamcrest.Matcher;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * The sole purpose of this class is to expose the inner workings of the RestAssured ResponseSpecification class. It
 * provides no extended functionality.
 *
 * @author wkwoelke
 * @see ResponseSpecification
 */
@SuppressWarnings({"unchecked", "unused"})
public class ResponseSpecificationWrapper {

    private final static String accessError = "An error occurred when parsing the Response. This can happen when making assertions when there is not a proper Response (such as Connection Refused). It can also be caused by use of an unsupported version of RestAssured, Hamcrest, or the interference of a JVM security manager. Make sure a dependency is not bringing in its own version of Hamcrest before your Hamcrest import. Some examples of packages which do this are: junit and mockito-all.";
    private final ResponseSpecification responseSpecification;
    public HamcrestAssertionClosureWrapper assertionClosure;
    public List<HeaderMatcher> headerAssertions = Lists.newArrayList();
    public List<CookieMatcher> cookieAssertions = Lists.newArrayList();
    public BodyMatcherGroup bodyMatchers = new BodyMatcherGroup();
    public ResponseParserRegistrar rpr;
    public Object contentParser;
    public RestAssuredConfig config;
    public Matcher<Integer> expectedStatusCode;
    public Matcher<String> expectedStatusLine;

    public ResponseSpecificationWrapper(ResponseSpecification responseSpecification) {
        this.responseSpecification = responseSpecification;

        try {
            Field assertionClosureField = responseSpecification.getClass().getDeclaredField("assertionClosure");
            assertionClosureField.setAccessible(true);
            ResponseSpecificationImpl.HamcrestAssertionClosure assertionClosure = (ResponseSpecificationImpl.HamcrestAssertionClosure) assertionClosureField.get(responseSpecification);
            this.assertionClosure = new HamcrestAssertionClosureWrapper(assertionClosure);

            Field headerAssertionsField = responseSpecification.getClass().getDeclaredField("headerAssertions");
            headerAssertionsField.setAccessible(true);
            this.headerAssertions = (List<HeaderMatcher>) headerAssertionsField.get(responseSpecification);

            Field cookieAssertionsField = responseSpecification.getClass().getDeclaredField("cookieAssertions");
            cookieAssertionsField.setAccessible(true);
            this.cookieAssertions = (List<CookieMatcher>) cookieAssertionsField.get(responseSpecification);

            Field bodyMatchersField = responseSpecification.getClass().getDeclaredField("bodyMatchers");
            bodyMatchersField.setAccessible(true);
            this.bodyMatchers = (BodyMatcherGroup) bodyMatchersField.get(responseSpecification);

            Field rprField = responseSpecification.getClass().getDeclaredField("rpr");
            rprField.setAccessible(true);
            this.rpr = (ResponseParserRegistrar) rprField.get(responseSpecification);

            Field contentParserField = responseSpecification.getClass().getDeclaredField("contentParser");
            contentParserField.setAccessible(true);
            this.contentParser = contentParserField.get(responseSpecification);

            Field configField = responseSpecification.getClass().getDeclaredField("config");
            configField.setAccessible(true);
            this.config = (RestAssuredConfig) configField.get(responseSpecification);

            Field expectedStatusCodeField = responseSpecification.getClass().getDeclaredField("expectedStatusCode");
            expectedStatusCodeField.setAccessible(true);
            this.expectedStatusCode = (Matcher<Integer>) expectedStatusCodeField.get(responseSpecification);

            Field expectedStatusLineField = responseSpecification.getClass().getDeclaredField("expectedStatusLine");
            expectedStatusLineField.setAccessible(true);
            this.expectedStatusLine = (Matcher<String>) expectedStatusLineField.get(responseSpecification);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(accessError, e);
        }
    }

    public Response validate(Response response) {
        assertionClosure.validate(response);
        return response;
    }

    public boolean hasAssertionsDefined() {
        boolean hasAssertionsDefined;

        try {
            Method method = responseSpecification.getClass().getDeclaredMethod("hasAssertionsDefined");
            method.setAccessible(true);
            hasAssertionsDefined = (boolean) method.invoke(responseSpecification);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(accessError, e);
        }

        return hasAssertionsDefined;
    }

    public boolean hasBodyAssertionsDefined() {
        boolean hasBodyAssertionsDefined = false;

        try {
            Method method = responseSpecification.getClass().getDeclaredMethod("hasBodyAssertionsDefined");
            method.setAccessible(true);
            hasBodyAssertionsDefined = (boolean) method.invoke(responseSpecification);
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof NullPointerException) {
                Throwable cause = new Throwable(e.getMessage(), e.getCause());
                cause.setStackTrace(ValidationReportGenerator.getStacktrace());
                throw new RuntimeException("Response body was null", cause);
            }
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(accessError, e);
        }

        return hasBodyAssertionsDefined;
    }

    public boolean isEagerAssert() {
        boolean isEagerAssert;

        try {
            Method method = responseSpecification.getClass().getDeclaredMethod("isEagerAssert");
            method.setAccessible(true);
            isEagerAssert = (boolean) method.invoke(responseSpecification);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(accessError, e);
        }

        return isEagerAssert;
    }

    @SuppressWarnings({"unchecked", "unused"})
    public class HamcrestAssertionClosureWrapper {

        private final ResponseSpecificationImpl.HamcrestAssertionClosure assertionClosure;

        public HamcrestAssertionClosureWrapper(ResponseSpecificationImpl.HamcrestAssertionClosure assertionClosure) {
            this.assertionClosure = assertionClosure;
        }

        public List<Map<String, Object>> validateStatusCodeAndStatusLine(Response response) {
            @SuppressWarnings("UnusedAssignment") List<Map<String, Object>> failures = Lists.newArrayList();

            try {
                Method method = assertionClosure.getClass().getDeclaredMethod("validateStatusCodeAndStatusLine", Response.class);
                method.setAccessible(true);
                failures = (List<Map<String, Object>>) method.invoke(assertionClosure, response);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(accessError, e);
            }

            return failures;
        }

        public void validate(Response response) {
            assertionClosure.validate(response);
        }

        public void logRequestAndResponseIfEnabled() {
            try {
                Method method = assertionClosure.getClass().getDeclaredMethod("logRequestAndResponseIfEnabled");
                method.setAccessible(true);
                method.invoke(assertionClosure);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(accessError, e);
            }
        }

        public List<Map<String, Object>> validateHeadersAndCookies(Response response) {
            @SuppressWarnings("UnusedAssignment") List<Map<String, Object>> failures = Lists.newArrayList();

            try {
                Method method = assertionClosure.getClass().getDeclaredMethod("validateHeadersAndCookies", Response.class);
                method.setAccessible(true);
                failures = (List<Map<String, Object>>) method.invoke(assertionClosure, response);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(accessError, e);
            }

            return failures;
        }

        public List<Map<String, Object>> validateContentType(Response response) {
            @SuppressWarnings("UnusedAssignment") List<Map<String, Object>> failures = Lists.newArrayList();

            try {
                Method method = assertionClosure.getClass().getDeclaredMethod("validateContentType", Response.class);
                method.setAccessible(true);
                failures = (List<Map<String, Object>>) method.invoke(assertionClosure, response);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(accessError, e);
            }

            return failures;
        }

        public List<Map<String, Object>> validateResponseTime(Response response) {
            @SuppressWarnings("UnusedAssignment") List<Map<String, Object>> failures = Lists.newArrayList();

            try {
                Method method = assertionClosure.getClass().getDeclaredMethod("validateResponseTime", Response.class);
                method.setAccessible(true);
                failures = (List<Map<String, Object>>) method.invoke(assertionClosure, response);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(accessError, e);
            }

            return failures;
        }

        public boolean requiresPathParsing() {
            boolean requiresPathParsing;

            try {
                Method method = assertionClosure.getClass().getDeclaredMethod("requiresPathParsing");
                method.setAccessible(true);
                requiresPathParsing = (boolean) method.invoke(assertionClosure);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(accessError, e);
            }

            return requiresPathParsing;
        }
    }
}
