package com.example.demo.controllers;

import com.example.demo.entity.Part;
import com.example.demo.entity.Product;
import com.example.demo.service.PartService;
import com.example.demo.service.PartServiceImpl;
import com.example.demo.service.ProductService;
import com.example.demo.service.ProductServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 *
 *
 */
@Controller
public class AddProductController {

    @Autowired
    private ApplicationContext context;

    private PartService partService;  // Encapsulation: service injected to access parts
    private static Product product1;
    private Product product;

    public AddProductController(PartService partService) {
        this.partService = partService;
    }

    // Show form to add a Product
    @GetMapping("/showFormAddProduct")
    public String showFormAddPart(Model theModel) {
        theModel.addAttribute("parts", partService.findAll());  // Encapsulation: get all parts
        product = new Product();  // Inheritance: Product class encapsulates product data
        product1 = product;
        theModel.addAttribute("product", product);

        // Prepare available parts not already associated
        List<Part> availParts = new ArrayList<>();
        for (Part p : partService.findAll()) {  // Polymorphism: treat all Part subtypes uniformly
            if (!product.getParts().contains(p)) availParts.add(p);
        }
        theModel.addAttribute("availparts", availParts);
        theModel.addAttribute("assparts", product.getParts());
        return "productForm";
    }

    // Submit the product form
    @PostMapping("/showFormAddProduct")
    public String submitForm(@Valid @ModelAttribute("product") Product product, BindingResult bindingResult, Model theModel) {
        theModel.addAttribute("product", product);

        if (bindingResult.hasErrors()) {
            // Show form with errors
            ProductService productService = context.getBean(ProductServiceImpl.class);
            Product product2 = new Product();
            try {
                product2 = productService.findById((int) product.getId());
            } catch (Exception e) {
                System.out.println("Error Message " + e.getMessage());
            }
            theModel.addAttribute("parts", partService.findAll());

            // Polymorphism: Handle parts as Part supertype
            List<Part> availParts = new ArrayList<>();
            for (Part p : partService.findAll()) {
                if (!product2.getParts().contains(p)) availParts.add(p);
            }
            theModel.addAttribute("availparts", availParts);
            theModel.addAttribute("assparts", product2.getParts());
            return "productForm";
        } else {
            // Save or update product and adjust inventory (Encapsulation)
            ProductService repo = context.getBean(ProductServiceImpl.class);
            if (product.getId() != 0) {
                Product product2 = repo.findById((int) product.getId());
                PartService partService1 = context.getBean(PartServiceImpl.class);
                if (product.getInv() - product2.getInv() > 0) {
                    for (Part p : product2.getParts()) {
                        int inv = p.getInv();
                        p.setInv(inv - (product.getInv() - product2.getInv()));  // Encapsulation: update part inventory via setter
                        partService1.save(p);
                    }
                }
            }
            repo.save(product);
            return "confirmationaddproduct";
        }
    }

    // Show form to update a product
    @GetMapping("/showProductFormForUpdate")
    public String showProductFormForUpdate(@RequestParam("productID") int theId, Model theModel) {
        theModel.addAttribute("parts", partService.findAll());  // Encapsulation: access parts list
        ProductService repo = context.getBean(ProductServiceImpl.class);
        Product theProduct = repo.findById(theId);
        product1 = theProduct;

        // Encapsulation: get associated parts via getter
        theModel.addAttribute("product", theProduct);
        theModel.addAttribute("assparts", theProduct.getParts());

        List<Part> availParts = new ArrayList<>();
        for (Part p : partService.findAll()) {  // Polymorphism: use Part type for all parts
            if (!theProduct.getParts().contains(p)) availParts.add(p);
        }
        theModel.addAttribute("availparts", availParts);
        return "productForm";
    }

    // Delete a product and update related parts
    @GetMapping("/deleteproduct")
    public String deleteProduct(@RequestParam("productID") int theId, Model theModel) {
        ProductService productService = context.getBean(ProductServiceImpl.class);
        Product product2 = productService.findById(theId);

        // Encapsulation: updating associations safely using getters/setters
        for (Part part : product2.getParts()) {
            part.getProducts().remove(product2);
            partService.save(part);
        }
        product2.getParts().clear();
        productService.save(product2);
        productService.deleteById(theId);

        return "confirmationdeleteproduct";
    }

    // Add part to product (associate)
    @GetMapping("/associatepart")
    public String associatePart(@Valid @RequestParam("partID") int theID, Model theModel) {
        if (product1.getName() == null) {
            return "saveproductscreen";
        } else {
            product1.getParts().add(partService.findById(theID));  // Encapsulation: manipulate parts set safely
            partService.findById(theID).getProducts().add(product1);
            ProductService productService = context.getBean(ProductServiceImpl.class);
            productService.save(product1);
            partService.save(partService.findById(theID));
            theModel.addAttribute("product", product1);
            theModel.addAttribute("assparts", product1.getParts());

            List<Part> availParts = new ArrayList<>();
            for (Part p : partService.findAll()) {
                if (!product1.getParts().contains(p)) availParts.add(p);  // Polymorphism with Part type
            }
            theModel.addAttribute("availparts", availParts);
            return "productForm";
        }
    }

    // Remove part from product
    @GetMapping("/removepart")
    public String removePart(@RequestParam("partID") int theID, Model theModel) {
        product1.getParts().remove(partService.findById(theID));
        partService.findById(theID).getProducts().remove(product1);
        ProductService productService = context.getBean(ProductServiceImpl.class);
        productService.save(product1);
        partService.save(partService.findById(theID));

        theModel.addAttribute("product", product1);
        theModel.addAttribute("assparts", product1.getParts());

        List<Part> availParts = new ArrayList<>();
        for (Part p : partService.findAll()) {
            if (!product1.getParts().contains(p)) availParts.add(p);  // Polymorphism
        }
        theModel.addAttribute("availparts", availParts);
        return "productForm";
    }
}
