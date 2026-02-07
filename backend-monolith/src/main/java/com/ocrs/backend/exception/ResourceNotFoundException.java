package com.ocrs.backend.exception;

// custom exception for when a requested resource is not found
public class ResourceNotFoundException extends RuntimeException {

        /**
         * Creates a ResourceNotFoundException with the specified detail message.
         *
         * @param message the detail message describing the missing resource
         */
        public ResourceNotFoundException(String message) {
                super(message);
        }

        /**
         * Constructs an exception indicating that a resource of the given type with the specified id was not found.
         *
         * @param resourceType the type or name of the missing resource (for example, "User" or "Order")
         * @param id the numeric identifier of the missing resource
         */
        public ResourceNotFoundException(String resourceType, Long id) {
                super(resourceType + " not found with id: " + id);
        }

        /**
         * Constructs an exception indicating that a resource of the given type was not found using the provided identifier.
         *
         * @param resourceType the resource type or name (for example, "User" or "Order")
         * @param identifier   the string identifier used to locate the resource (for example, a username or UUID)
         */
        public ResourceNotFoundException(String resourceType, String identifier) {
                super(resourceType + " not found: " + identifier);
        }
}