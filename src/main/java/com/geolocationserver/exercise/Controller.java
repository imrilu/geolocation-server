package com.geolocationserver.exercise;

import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/")
public class Controller {

    @GetMapping("/hello")
    public void hello(){
        // empty function to test application
    }

}