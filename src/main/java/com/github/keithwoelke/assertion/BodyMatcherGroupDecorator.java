package com.github.keithwoelke.assertion;

import com.google.common.collect.Maps;
import io.restassured.assertion.BodyMatcherGroup;
import io.restassured.config.RestAssuredConfig;
import io.restassured.internal.support.Prettifier;
import io.restassured.module.jsv.JsonSchemaValidationException;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class is designed to extend the behavior of the RestAssured BodyMatcherGroup class. It duplicates only enough
 * code to achieve the necessary modifications. All other functionality is delegated to the BodyMatcherGroupWrapper
 * class. It primarily serves to return the validation results in a collection containing both failed and successful
 * results.
 * <p>
 * At this point in time, the successful validations contain an empty message. This could be changed if the need arises
 * for more detailed reporting.
 *
 * @author wkwoelke
 * @see BodyMatcherGroupWrapper
 * @see BodyMatcherGroup
 */
@SuppressWarnings({"WeakerAccess", "unused", "unchecked"})
public class BodyMatcherGroupDecorator {

    private final BodyMatcherGroupWrapper bodyMatcherGroupWrapper;

    public BodyMatcherGroupDecorator(BodyMatcherGroup bodyMatchers) {
        this.bodyMatcherGroupWrapper = new BodyMatcherGroupWrapper(bodyMatchers);
    }

    public Collection<ValidationResult> validate(Response response, Object contentParser, RestAssuredConfig cfg) {
        final int totalNumberOfValidations = bodyMatcherGroupWrapper.bodyAssertions.size();

        List<Map<String, Object>> failures = bodyMatcherGroupWrapper.bodyAssertions.stream().map(it -> {
                    Map<String, Object> validation;

                    try {
                        validation = (Map<String, Object>) it.validate(response, contentParser, cfg);
                    } catch (JsonSchemaValidationException e) {
                        validation = Maps.newHashMap();
                        validation.put("success", false);

                        String responseString = new Prettifier().getPrettifiedBodyIfPossible(response, response);

                        if (StringUtils.isBlank(responseString)) {
                            responseString = response.asString();
                        }

                        validation.put("errorMessage", String.format("Something went wrong during JSON schema validation. Either the response was not (valid) JSON or there was an error parsing the schema definition.%n%nError: %s%nResponse body: %n%s", e.getMessage(), responseString));
                    }

                    return validation;
                }
        ).
                collect(Collectors.toList());

        ValidationResult.ValidationResultListBuilder validationResultListBuilder = ValidationResult.listBuilder();
        validationResultListBuilder.hamcrestResults(failures);
        validationResultListBuilder.count(totalNumberOfValidations);

        return validationResultListBuilder.build();
    }
}
