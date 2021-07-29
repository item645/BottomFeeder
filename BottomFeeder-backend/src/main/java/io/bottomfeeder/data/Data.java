package io.bottomfeeder.data;

import java.io.IOException;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Top-level container for imported or exported data.
 */
@JsonSerialize(using = Data.Serializer.class)
record Data<T>(
		
		@JsonAlias({TYPE_USERS, TYPE_DIGESTS, TYPE_SOURCE_FEEDS})
		Collection<T> items,
		
		@JsonIgnore
		String type) {

	static final String TYPE_USERS = "users";
	static final String TYPE_DIGESTS = "digests";
	static final String TYPE_SOURCE_FEEDS = "sourceFeeds";
	
	
	/**
	 * Custom serializer for {@code Data} instances that sets field name for data items array
	 * according to specified data type.
	 */
	static class Serializer extends JsonSerializer<Data<?>> {

		@Override
		public void serialize(Data<?> data, JsonGenerator jsonGenerator, SerializerProvider serializers) 
				throws IOException {
			jsonGenerator.writeStartObject();
			jsonGenerator.writeObjectField(data.type(), data.items());
			jsonGenerator.writeEndObject();
		}
		
	}
	
}
