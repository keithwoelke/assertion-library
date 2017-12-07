package com.github.keithwoelke.assertion;

import io.restassured.specification.ResponseSpecification;
import org.springframework.stereotype.Service;

/**
 * A builder for RestAssured related objects. This class was introduced to allow for unit testing of code where object
 * are created within methods.
 *
 * @author wkwoelke
 */
@Service
public class RestAssuredObjectBuilder {

    public ResponseSpecificationDecorator getResponseSpecificationDecorator(ResponseSpecification responseSpecification) {
        return new ResponseSpecificationDecorator(responseSpecification);
    }

}
