package com.geolocationserver.exercise.controller;

import com.geolocationserver.exercise.repository.DistanceRepository;
import com.geolocationserver.exercise.model.Distance;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;


@RestController
@RequestMapping("/")
public class Controller {

    @Autowired
    DistanceRepository repository;

    @Value("${server.port}")
    private String port;


    @GetMapping("/hello")
    public void hello() {
        // empty function to test application
    }

    @GetMapping("/health")
    public ResponseEntity health() throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        JSONParser parser = new JSONParser();
        String response = restTemplate.getForObject("http://localhost:" + port + "/actuator/health", String.class);
        JSONObject json = (JSONObject) parser.parse(response);
        if (json.get("status").equals("UP")) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>("Error connecting to DB", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/distance")
    public ResponseEntity addDistance(@RequestBody String body) throws Exception {
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(body);
        Distance distance = new Distance(((Number) json.get("distance")).doubleValue(), (String) json.get("source"), (String) json.get("destination"), 0);
        repository.save(distance);
        return new ResponseEntity<>(json, HttpStatus.CREATED);
    }

    @GetMapping("/popularsearch")
    public JSONObject maxHitsDistance() {
        JSONObject response = new JSONObject();
        long maxHits = Distance.getMaxHitsId();
        if (maxHits == -1)
            return response;
        if (maxHits == 0)
            // hibernate bug
            maxHits = 1;
        Distance distance = repository.getDistanceById(maxHits);
        System.out.println(distance.toString());
        response.put("source", distance.getSource());
        response.put("destination", distance.getDestination());
        response.put("hits", distance.getHits());
        return response;
    }

    @GetMapping("/distance")
    public JSONObject distance(@RequestParam String source,
                               @RequestParam String destination) {
        JSONObject response = new JSONObject();
        if (source.toLowerCase().equals(destination.toLowerCase())) {
            response.put("distance", 0);
            return response;
        }
        List<Distance> distances = repository.findBySourceAndDestination(source.toLowerCase(), destination.toLowerCase());
        if (distances.isEmpty()) {
            return saveExternalDistance(source.toLowerCase(), destination.toLowerCase());
        }
        distances.get(0).incrementHits();
        repository.save(distances.get(0));
        response.put("distance", distances.get(0).getDistance());
        return response;
    }

    private JSONObject saveExternalDistance(String source, String destination) {
        JSONObject response = new JSONObject();
        try {
            double externalDistance = getExternalDistance(source, destination);
            Distance distance = new Distance(externalDistance, source, destination, 1);
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