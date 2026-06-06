package com.paquito.primerspring.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // Página de inicio (bienvenida)
    @GetMapping("/")
    public String home() {
        return "home"; // Muestra home.html
    }

    // Página de login personalizada
    @GetMapping("/login")
    public String login() {
        return "login"; // Muestra login.html
    }
}
