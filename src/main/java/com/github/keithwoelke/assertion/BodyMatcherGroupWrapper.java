package com.github.keithwoelke.assertion;

import io.restassured.assertion.BodyMatcher;
import io.restassured.assertion.BodyMatcherGroup;
import io.restassured.config.RestAssuredConfig;
import io.restassured.response.Response;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * The sole purpose of this class is to expose the inner workings of the RestAssured BodyMatcherGroup class. It provides
 * no extended functionality.
 *
 * @author wkwoelke
 * @see BodyMatcherGroup
 */
@SuppressWarnings({"unchecked", "WeakerAccess", "unused"})
public class BodyMatcherGroupWrapper {

    @SuppressWarnings("FieldCanBeLocal")
    private final static String accessError = "Reflection of RestAssured objects did not behave as expected. This is most likely caused by use of an unsupported version of RestAssured or the interference of a JVM security manager";
    public final List<BodyMatcher> bodyAssertions;
    private final BodyMatcherGroup bodyMatcherGroup;

    public BodyMatcherGroupWrapper(BodyMatcherGroup bodyMatcherGroup) {
        this.bodyMatcherGroup = bodyMatcherGroup;

        try {
            Field bodyAssertionsField = bodyMatcherGroup.getClass().getDeclaredField("bodyAssertions");
            bodyAssertionsField.setAccessible(true);
            this.bodyAssertions = (List<BodyMatcher>) bodyAssertionsField.get(bodyMatcherGroup);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(accessError, e);
        }
    }

    public List<Map<String, Object>> validate(Response response, Object contentParser, RestAssuredConfig cfg) {
        return this.bodyMatcherGroup.validate(response, contentParser, cfg);
    }
}
