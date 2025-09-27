package com.example.demo.controllers;

import com.example.demo.entity.Part;
import com.example.demo.entity.Product;
import com.example.demo.service.PartService;
import com.example.demo.service.ProductService;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class MainScreenControllerr {

    private final PartService partService;
    private final ProductService productService;

    public MainScreenControllerr(PartService partService, ProductService productService) {
        this.partService = partService;
        this.productService = productService;
    }

    // Redirect root path to /mainscreen
    @GetMapping("/")
    public String redirectToMainScreen() {
        return "redirect:/mainscreen";
    }

    // Download CSV report
    @GetMapping("/report")
    public void downloadCSV(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=report.csv");

        PrintWriter writer = response.getWriter();
        writer.println("Name,Price,Inventory,Date");

        List<Product> products = productService.findAll();
        for (Product p : products) {
            writer.printf("%s,%.2f,%d,%s%n", p.getName(), p.getPrice(), p.getInv(), LocalDateTime.now());
        }

        writer.flush();
        writer.close();
    }

    // Show main screen with parts and products
    @GetMapping("/mainscreen")
    public String listPartsandProducts(Model theModel, @Param("partkeyword") String partkeyword, @Param("productkeyword") String productkeyword) {
        List<Part> partList = partService.listAll(partkeyword);
        List<Product> productList = productService.listAll(productkeyword);

        theModel.addAttribute("parts", partList);
        theModel.addAttribute("partkeyword", partkeyword);
        theModel.addAttribute("products", productList);
        theModel.addAttribute("productkeyword", productkeyword);

        return "mainscreen";
    }
}
