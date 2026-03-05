package com.example.theatre.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Главный контроллер
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/plays";
    }
}

