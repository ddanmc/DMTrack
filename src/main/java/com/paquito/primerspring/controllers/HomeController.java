package com.paquito.primerspring.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

    // Página de inicio (bienvenida)
    @GetMapping("/")
    public String home() {
        return "home";
    }

    // Página de login personalizada
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // Ruta de prueba para Railway
    @GetMapping("/test")
    @ResponseBody
    public String test() {
        return "DMTrack funcionando correctamente";
    }
}
