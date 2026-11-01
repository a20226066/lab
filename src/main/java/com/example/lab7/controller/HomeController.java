package com.example.lab7.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// Controlador de la pagina de inicio
@Controller
public class HomeController {

    // GET / -> templates/index.html
    @GetMapping({"", "/"})
    public String inicio() {
        return "index";
    }
}
