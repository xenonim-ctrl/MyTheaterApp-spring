package com.example.theatre.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Контроллер для страницы "О разработчике"
 */
@Controller
public class AboutController {

    @GetMapping("/about")
    public String about() {
        return "about";
    }
}

