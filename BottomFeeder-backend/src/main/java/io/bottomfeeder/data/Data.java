package io.bottomfeeder.data;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * Top-level container for imported or exported data.
 */
record Data<T>(@JsonAlias({"users", "digests"}) Collection<T> items) {
}
