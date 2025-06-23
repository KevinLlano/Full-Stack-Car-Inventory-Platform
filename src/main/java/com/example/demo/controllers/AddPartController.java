package com.example.demo.controllers;

import com.example.demo.entity.InhousePart;
import com.example.demo.entity.OutsourcedPart;
import com.example.demo.entity.Part;
import com.example.demo.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 *
 *
 *
 *
 */
@Controller
public class AddPartController {

    @Autowired
    private ApplicationContext context;  // Spring Context for bean retrieval

    // Show update form for a Part
    @GetMapping("/showPartFormForUpdate")
    public String showPartFormForUpdate(@RequestParam("partID") int theId, Model theModel) {
        // Inheritance & Polymorphism: Use PartService to work with different Part subtypes (InhousePart, OutsourcedPart)
        PartService repo = context.getBean(PartServiceImpl.class);
        OutsourcedPartService outsourcedrepo = context.getBean(OutsourcedPartServiceImpl.class);
        InhousePartService inhouserepo = context.getBean(InhousePartServiceImpl.class);

        // Decide if the part is Inhouse or Outsourced (Polymorphism)
        boolean inhouse = true;
        List<OutsourcedPart> outsourcedParts = outsourcedrepo.findAll();
        for (OutsourcedPart outsourcedPart : outsourcedParts) {
            if (outsourcedPart.getId() == theId) inhouse = false;
        }

        // Encapsulation: Using getters/setters to handle part data
        if (inhouse) {
            InhousePart inhousePart = inhouserepo.findById(theId);
            theModel.addAttribute("inhousepart", inhousePart);
            return "InhousePartForm";  // Show InhousePart form
        } else {
            OutsourcedPart outsourcedPart = outsourcedrepo.findById(theId);
            theModel.addAttribute("outsourcedpart", outsourcedPart);
            return "OutsourcedPartForm";  // Show OutsourcedPart form
        }
    }

    // Delete a part only if not linked to products
    @GetMapping("/deletepart")
    public String deletePart(@Valid @RequestParam("partID") int theId, Model theModel) {
        PartService repo = context.getBean(PartServiceImpl.class);
        Part part = repo.findById(theId);

        // Encapsulation: Access part’s associated products via getter
        if (part.getProducts().isEmpty()) {
            repo.deleteById(theId);
            return "confirmationdeletepart";  // Successful deletion
        } else {
            return "negativeerror";  // Can't delete part linked to products
        }
    }
}
