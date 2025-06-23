package com.example.demo.validators;

import com.example.demo.entity.InhousePart;
import com.example.demo.entity.Part;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DeletePartValidatorTest {

    @Test
    void testValidDeletePart() {
        // Create a new InhousePart instance
        Part part = new InhousePart();

        // Simulate a part with no associated products (empty set)
        part.setProducts(new HashSet<>());

        // Create validator instance
        DeletePartValidator validator = new DeletePartValidator();

        // Assert that the validator returns true for deletable part
        assertTrue(validator.isValid(part, null));
    }
}
