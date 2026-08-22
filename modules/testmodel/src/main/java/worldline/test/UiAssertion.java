package worldline.test;

import worldline.api.GameUiQuery;

/** One retryable semantic UI assertion evaluated against a lazy locator. */
@FunctionalInterface
public interface UiAssertion {
    void verify(GameUiQuery query);
}
