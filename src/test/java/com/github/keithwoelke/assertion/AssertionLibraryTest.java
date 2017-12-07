package com.github.keithwoelke.assertion;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.exparity.hamcrest.date.LocalDateTimeMatchers.within;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsEqual.equalTo;

@SuppressWarnings("WeakerAccess")
public class AssertionLibraryTest {

    @Mock
    RestAssuredObjectBuilder restAssuredObjectBuilderMock;

    BaseValidationLibrary baseValidationLibrary;

    @Before
    public void init() {
        baseValidationLibrary = new BaseValidationLibrary(restAssuredObjectBuilderMock);
    }

    @Test
    public void expectThatStandard_singleFailure_shouldReturnFailure() {
        ValidationResult validationResult = baseValidationLibrary.expectThat(true, equalTo(false));

        assertThat(validationResult.getType(), equalTo(ValidationType.EXPECTATION));
        assertThat(validationResult.isSuccess(), is(false));
        assertThat(validationResult.getMessage(), allOf(notNullValue(), not(isEmptyString())));
        assertThat(validationResult.getEventTime(), within(100, ChronoUnit.MILLIS, LocalDateTime.now()));
    }

    @Test
    public void expectThatStandard_singleSuccess_shouldReturnSuccess() {
        ValidationResult validationResult = baseValidationLibrary.expectThat(true, equalTo(true));

        assertThat(validationResult.getType(), equalTo(ValidationType.EXPECTATION));
        assertThat(validationResult.isSuccess(), is(true));
        assertThat(validationResult.getMessage(), isEmptyString());
        assertThat(validationResult.getEventTime(), within(100, ChronoUnit.MILLIS, LocalDateTime.now()));
    }

    @Test
    public void assertThatStandard_singleFailure_shouldReturnFailure() {
        ValidationResult validationResult = baseValidationLibrary.assertThat(true, equalTo(false));

        assertThat(validationResult.getType(), equalTo(ValidationType.ASSERTION));
        assertThat(validationResult.isSuccess(), is(false));
        assertThat(validationResult.getMessage(), allOf(notNullValue(), not(isEmptyString())));
        assertThat(validationResult.getEventTime(), within(250, ChronoUnit.MILLIS, LocalDateTime.now()));
    }

    @Test
    public void assertThatStandard_singleSuccess_shouldReturnSuccess() {
        ValidationResult validationResult = baseValidationLibrary.assertThat(true, equalTo(true));

        assertThat(validationResult.getType(), equalTo(ValidationType.ASSERTION));
        assertThat(validationResult.isSuccess(), is(true));
        assertThat(validationResult.getMessage(), isEmptyString());
        assertThat(validationResult.getEventTime(), within(100, ChronoUnit.MILLIS, LocalDateTime.now()));
    }
}
