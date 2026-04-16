package org.valeneisa.core;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class WebController {

    @GetMapping("/")
    public String index() {
        return "index"; // return index view
    }

    @GetMapping("/game")
    public String game() {
        return "game"; // return game view
    }
}