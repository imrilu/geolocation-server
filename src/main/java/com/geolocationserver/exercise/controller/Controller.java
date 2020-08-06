package com.geolocationserver.exercise.controller;

import com.geolocationserver.exercise.repository.DistanceRepository;
import com.geolocationserver.exercise.model.Distance;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;


@RestController
@RequestMapping("/")
public class Controller {

    @Autowired
    DistanceRepository repository;

    @GetMapping("/hello")
    public void hello() {
        // empty function to test application
    }

    @GetMapping("/distance")
    public JSONObject distance(@RequestParam String source,
                               @RequestParam String destination) {
        List<Distance> distances = repository.findBySourceAndDestination(source.toLowerCase(), destination.toLowerCase());
        if (distances.isEmpty()) {
            return saveExternalDistance(source.toLowerCase(), destination.toLowerCase());
        }
        JSONObject response = new JSONObject();
        response.put("distance", distances.get(0).getDistance());
        return response;
    }

    private JSONObject saveExternalDistance(String source, String destination) {
        JSONObject response = new JSONObject();
        try {
            double externalDistance = getExternalDistance(source, destination);
            Distance distance = new Distance(externalDistance, source, destination);
            repository.save(distance);
            response.put("distance", externalDistance);
            return response;
        } catch (Exception e) {
            response.put("distance", -1);
            return response;
        }
    }

    private double getExternalDistance(String source, String destination) throws Exception {
        if (source == null || destination == null)
            throw new Exception();
        String url = "http://dev.virtualearth.net/REST/V1/Routes/Driving?wp.0=" + source +
                "&wp.1=" + destination +
                "&avoid=minimizeTolls&key=AoHFws8MMBKOH_M9iNShgR-QG18XeFn50Sa0pJ4wFjhoiJcIOYnCCvExLilUCEP8";
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(url, String.class);
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(response);

        if ((long) json.get("statusCode") != 200)
            throw new Exception();
        return (double) ((JSONObject) ((JSONArray) ((JSONObject) ((JSONArray) ((JSONObject)
                parser.parse(response)).get("resourceSets")).get(0)).get("resources")).get(0)).get("travelDistance");
    }

}