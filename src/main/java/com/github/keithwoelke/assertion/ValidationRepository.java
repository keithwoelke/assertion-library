package com.github.keithwoelke.assertion;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * This class is responsible for storing validation (assertion and expectation) results. It provides mechanisms for
 * differentiating "current" results versus "past" results. Specifically, a "current" set of results can be archived
 * for later analysis. All "get" methods will return only "current" results with the exception of the "getAll" methods
 * which will return both archived and non-archived results.
 * <p>
 * If all keys are expected to be unique, there would potentially be no need to archive validation results. However, if
 * there is no guarantee of uniqueness over time, archiving my be particularly useful. For example, using thread ID as
 * your key may lead to unique "current" results but which may not be unique over time.
 * <p>
 * In the previous example, a test runner could archive results by key at the end of each test to enable reporting
 * test-level metrics as well as metrics for the entire run. However, in such a scenario the archived results would
 * become nearly useless for post-run analysis at the test level if a unique key cannot be established during the test.
 *
 * @author wkwoelke
 */
@SuppressWarnings({"WeakerAccess", "MismatchedQueryAndUpdateOfCollection", "unused", "SuspiciousMethodCalls"})
@Service
public class ValidationRepository {

    private final String ASSERTION_SUMMARY_MESSAGE = "%d of %d assertion%s";
    private final String FAIL_ERROR_MESSAGE = "failed.%n%s";

    private final Map<Object, List<ValidationResult>> expectations = new ConcurrentHashMap<>();
    private final Map<Object, List<ValidationResult>> assertions = new ConcurrentHashMap<>();
    private final ListMultimap<Object, List<ValidationResult>> archivedExpectations = ArrayListMultimap.create();
    private final ListMultimap<Object, List<ValidationResult>> archivedAssertions = ArrayListMultimap.create();

    /**
     * Record assertion results (either passes or failures).
     *
     * @param key        the key to use for referencing the assertions
     * @param assertions a list of assertion results
     */
    void addAssertions(Object key, List<ValidationResult> assertions) {
        if (!this.assertions.containsKey(key)) {
            this.assertions.put(key, Lists.newArrayList());
        }

        List<ValidationResult> currentAssertionResults = this.assertions.get(key);

        currentAssertionResults.addAll(Lists.newArrayList(assertions));
    }

    /**
     * Record expectation results (either passes or failures).
     *
     * @param key          the key to use for referencing the expectations
     * @param expectations a list of assertion results
     */
    void addExpectations(Object key, List<ValidationResult> expectations) {
        if (!this.expectations.containsKey(key)) {
            this.expectations.put(key, Lists.newArrayList());
        }

        List<ValidationResult> currentExpectationResults = this.expectations.get(key);

        currentExpectationResults.addAll(expectations);
    }

    /**
     * Archives the validation results referenced by the provided key and moves them out of the current execution,
     * storing them in the archive using the same key.
     *
     * @param key the key use use for accessing the current results as well as the key to use for storing them in the
     *            archive
     */
    void archiveResults(Object key) {
        archiveResults(key, key);
    }

    /**
     * Archives the validation results for the specified key and archives them under the newKey which is provided.
     *
     * @param key    the key to use to reference the validation results
     * @param newKey the key to use for archiving the current validation results
     */
    void archiveResults(Object key, Object newKey) {
        if (expectations.containsKey(key)) {
            archivedExpectations.put(key, expectations.remove(key));
        }

        if (assertions.containsKey(key)) {
            archivedAssertions.put(key, assertions.remove(key));
        }
    }

    /**
     * Get all non-archived assertions stored under the provided key.
     *
     * @param key the key to use for accessing non-archived assertions
     * @return a list of a assertions matching the provided key
     */
    List<ValidationResult> getAssertions(Object key) {
        List<ValidationResult> assertions = Lists.newArrayList();

        if (this.assertions.containsKey(key)) {
            assertions.addAll(this.assertions.get(key));
        }

        return assertions;
    }

    List<ValidationResult> getArchivedAssertions(Object key) {
        List<ValidationResult> assertions = Lists.newArrayList();

        if (this.archivedAssertions.containsKey(key)) {
            List<List<ValidationResult>> archivedAssertions = this.archivedAssertions.get(key);
            List<ValidationResult> flattenedArchivedAssertions = archivedAssertions.stream().flatMap(List::stream).collect(Collectors.toList());

            assertions.addAll(flattenedArchivedAssertions);
        }

        return assertions;
    }

    List<ValidationResult> getAllAssertions(Object key) {
        List<ValidationResult> assertions = getAssertions(key);
        List<ValidationResult> archivedAssertions = getArchivedAssertions(key);
        assertions.addAll(archivedAssertions);

        return assertions;
    }

    /**
     * @return all non-archived assertions
     */
    List<ValidationResult> getAssertions() {
        List<ValidationResult> assertions = Lists.newArrayList();

        this.assertions.entrySet().forEach(entry -> assertions.addAll(entry.getValue()));

        return assertions;
    }

    /**
     * @return a combined list of all assertions both archived and non-archived
     */
    List<ValidationResult> getAllAssertions() {
        List<ValidationResult> assertions = getAssertions();
        List<ValidationResult> archivedAssertions = getArchivedAssertions();
        assertions.addAll(archivedAssertions);

        return assertions;
    }

    /**
     * @return a list of all archived assertions
     */
    List<ValidationResult> getArchivedAssertions() {
        List<ValidationResult> archivedAssertions = Lists.newArrayList();

        for (Map.Entry<Object, List<ValidationResult>> testResults : this.archivedAssertions.entries()) {
            archivedAssertions.addAll(testResults.getValue());
        }

        return archivedAssertions;
    }

    List<ValidationResult> getExpectations(Object key) {
        List<ValidationResult> expectations = Lists.newArrayList();

        if (this.expectations.containsKey(key)) {
            expectations.addAll(this.expectations.get(key));
        }

        return expectations;
    }


    List<ValidationResult> getArchivedExpectations(Object key) {
        List<ValidationResult> expectations = Lists.newArrayList();

        if (this.archivedExpectations.containsKey(key)) {
            List<List<ValidationResult>> archivedExpectations = this.archivedExpectations.get(key);
            List<ValidationResult> flattenedArchivedExpectations = archivedExpectations.stream().flatMap(List::stream).collect(Collectors.toList());

            expectations.addAll(flattenedArchivedExpectations);
        }

        return expectations;
    }

    List<ValidationResult> getAllExpectations(Object key) {
        List<ValidationResult> expectations = getExpectations(key);
        List<ValidationResult> archivedExpectations = getArchivedExpectations(key);
        expectations.addAll(archivedExpectations);

        return expectations;
    }

    /**
     * @return all non-archived expectations
     */
    List<ValidationResult> getExpectations() {
        List<ValidationResult> expectations = Lists.newArrayList();

        this.assertions.entrySet().forEach(entry -> expectations.addAll(entry.getValue()));

        return expectations;
    }

    /**
     * @return a combined list of all expectations both archived and non-archived
     */
    List<ValidationResult> getAllExpectations() {
        List<ValidationResult> expectations = getExpectations();
        List<ValidationResult> archivedExpectations = getArchivedExpectations();
        expectations.addAll(archivedExpectations);

        return expectations;
    }

    /**
     * @return a list of all archived expectations
     */
    List<ValidationResult> getArchivedExpectations() {
        List<ValidationResult> archivedExpectations = Lists.newArrayList();

        for (Map.Entry<Object, List<ValidationResult>> testResults : this.archivedExpectations.entries()) {
            archivedExpectations.addAll(testResults.getValue());
        }

        return archivedExpectations;
    }

    List<ValidationResult> getValidations(Object key) {
        List<ValidationResult> validations = Lists.newArrayList();
        validations.addAll(getAssertions(key));
        validations.addAll(getExpectations(key));

        return validations;
    }

    List<ValidationResult> getArchivedValidations(Object key) {
        List<ValidationResult> validations = Lists.newArrayList();
        validations.addAll(getArchivedAssertions(key));
        validations.addAll(getArchivedExpectations(key));

        return validations;
    }

    List<ValidationResult> getAllValidations(Object key) {
        List<ValidationResult> validations = Lists.newArrayList();
        validations.addAll(getValidations(key));
        validations.addAll(getArchivedValidations(key));

        return validations;
    }

    /**
     * @return all non-archived validationsResults
     */
    List<ValidationResult> getValidations() {
        List<ValidationResult> validations = Lists.newArrayList();
        validations.addAll(getAssertions());
        validations.addAll(getExpectations());

        return validations;
    }

    /**
     * @return a combined list of all validationsResults both archived and non-archived
     */
    List<ValidationResult> getAllValidations() {
        List<ValidationResult> validations = Lists.newArrayList();
        validations.addAll(getValidations());
        validations.addAll(getArchivedValidations());

        return validations;
    }

    /**
     * @return a list of all archived validationsResults
     */
    List<ValidationResult> getArchivedValidations() {
        List<ValidationResult> validations = Lists.newArrayList();
        validations.addAll(getArchivedAssertions());
        validations.addAll(getArchivedExpectations());

        return validations;
    }

    List<ValidationResult> getFailedExpectations(Object key) {
        List<ValidationResult> expectations = getExpectations(key);

        return ValidationListUtils.getFailed(expectations);
    }

    List<ValidationResult> getFailedAssertions(Object key) {
        List<ValidationResult> assertions = getAssertions(key);

        return ValidationListUtils.getFailed(assertions);
    }

    List<ValidationResult> getFailedValidations(Object key) {
        List<ValidationResult> validations = Lists.newArrayList();
        validations.addAll(getFailedExpectations(key));
        validations.addAll(getFailedAssertions(key));

        Collections.sort(validations);

        return validations;
    }
}
