package com.ishan.user_service.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class RandomUserClientService {

    private final RestTemplate restTemplate = new RestTemplate();

    // RestTemplate is a synchronous (blocking) HTTP client.
    // Kept here (commented) only for learning and comparison purposes.
    // private final RestTemplate restTemplate = new RestTemplate();

    // WebClient is Spring's modern HTTP client.
    // It supports non-blocking and reactive communication by default.
    private final WebClient webClient;

    public RandomUserClientService() {
        // Build WebClient with a fixed base URL so we don't repeat it
        // in every request.
        this.webClient = WebClient.builder()
                        .baseUrl("https://randomuser.me") // Base URL of the third-party API
                        .build();
    }

    public String fetchRandomUsersRaw(){
        //String url = "https://randomuser.me/api/";
        //String restTemplateResponse = restTemplate.getForObject(url, String.class);
        /*
         * Flow of this method:
         * 1. Build an HTTP GET request
         * 2. Call the third-party API
         * 3. Read the response body as raw JSON (String)
         * 4. Block the thread to get the response synchronously
         *    (blocking is used ONLY for learning and simplicity)
         */
        return webClient
                .get() // Specify HTTP method (GET)
                .uri("/api/") // Specify the endpoint (relative to baseUrl)
                .retrieve()// Trigger the HTTP call and prepare to read response
                .bodyToMono(String.class)// Convert response body into Mono<String> Mono means "eventually one value"
                .block(); // BLOCKING call: Waits for the response and converts Mono<String> → String (THIS STEP IS ONLY FOR LEARNING) TO MAKE IT SYNCHRONOUS
        /*“WebClient returns a Mono<T> because the call is asynchronous.
        When we call .block(), it waits for the value and returns the actual object, which is why the method can return String.”*/
    }
}
