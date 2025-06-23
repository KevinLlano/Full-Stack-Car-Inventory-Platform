package com.example.demo.MainScreenControllerr.java;

import com.example.demo.controllers.MainScreenControllerr;
import com.example.demo.service.PartService;
import com.example.demo.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class MainScreenControllerrTest {

    @Test
    void testListPartsAndProducts() {
        // Create mock services and model
        PartService mockPartService = mock(PartService.class);
        ProductService mockProductService = mock(ProductService.class);
        Model mockModel = mock(Model.class);

        // Mock the return values of listAll for given keywords
        when(mockPartService.listAll("key1")).thenReturn(Collections.emptyList());
        when(mockProductService.listAll("key2")).thenReturn(Collections.emptyList());

        // Instantiate controller with mock services
        MainScreenControllerr controller = new MainScreenControllerr(mockPartService, mockProductService);

        // Call the method with mocks
        String result = controller.listPartsandProducts(mockModel, "key1", "key2");

        // Verify model attributes were added correctly
        verify(mockModel).addAttribute("parts", Collections.emptyList());
        verify(mockModel).addAttribute("partkeyword", "key1");
        verify(mockModel).addAttribute("products", Collections.emptyList());
        verify(mockModel).addAttribute("productkeyword", "key2");

        // Assert the view name is correct
        assertEquals("mainscreen", result);
    }
}
