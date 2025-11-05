package at.fhtw.webenprjbackend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// TODO: Remove this TestController before deploying to production.

/**
 * This controller is primarily for testing and should not be part of the production code.
 * @author Wii
 * @version 1.0
 */

@RestController
public class TestController {

    /**
     * Health check endpoint.
     * 
     * @return A simple string indicating the application is healthy.
     */
    @GetMapping("/status")
    public String status() {
        return "Motivise Backend is running! ✅";
    }
}