package io.bottomfeeder.base;

/**
 * A representational model for application entity that provides information necessary
 * to identify the entity.
 * 
 * @param <T> the type of entity
 */
public interface EntityModel<T> {

	/**
	 * The id of target entity, typically the database id (primary key).
	 */
	Long entityId();
	
	/**
	 * Target entity class.
	 */
	Class<T> entityClass();
	
}
