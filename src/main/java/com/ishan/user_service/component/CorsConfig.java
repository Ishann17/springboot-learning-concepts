package com.ishan.user_service.component;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*
    WHY THIS CLASS EXISTS?

    When our frontend (HTML/JS UI) calls our backend APIs, the browser may BLOCK the request due to CORS.

    Example:
      Frontend running on  -> http://127.0.0.1:5500   (Live Server / VS Code)
      Backend running on   -> http://localhost:8080

    Browser considers them DIFFERENT "origins" (different host/port),
    so it blocks the request unless backend explicitly ALLOWS it.

    CORS = Cross-Origin Resource Sharing
    Means: backend tells the browser -> "Yes, this frontend is allowed to call me"

    Why not use @CrossOrigin on every controller?
    - If we have 10-12 controllers, repeating @CrossOrigin is messy
    - Hardcoding origin in every controller is not scalable
    - Global config in ONE place is clean and professional

    So we create this CorsConfig class which applies CORS rules to all matching APIs.
    ____________________________________
    Why we implemented WebMvcConfigurer?
    Because Spring Boot gives you Spring MVC by default, and WebMvcConfigurer is the official
    “hook” that lets you customize Spring MVC behavior without breaking it.
    Meaning - “Spring, keep your defaults, but also apply my custom rules.”
*/
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /*
        addCorsMappings()

        This method lets us define GLOBAL CORS rules for our backend.

        Spring will call this method automatically at application startup
        and store these CORS rules inside Spring MVC configuration.
    */
    @Override
    public void addCorsMappings(CorsRegistry registry) {

        /*
            registry.addMapping("/api/**")

            This means:
            "Apply these CORS rules to ALL endpoints that start with /api/"

            Example:
              /api/v1/users/256
              /api/v1/users
              /api/import/random-users
              /api/export/stream

            Note:
            - "/api/**" means "any path under /api/"
            - If our endpoint does NOT start with /api, these rules won’t apply there.
        */
        registry.addMapping("/api/**")

                /*
                    ✅ allowedOrigins(...)

                    This tells backend:
                    "Allow ONLY these frontend websites to call our APIs"

                    We added both:
                    - http://127.0.0.1:5500
                    - http://localhost:5500

                    Reason:
                    Sometimes VS Code Live Server runs as 127.0.0.1
                    and sometimes as localhost.

                    ✅ In real production:
                    - We will NOT hardcode these values
                    - We will read them from application.properties / env variables
                    - Example: allowedOrigins("https://myfrontend.com")
                */
                .allowedOrigins("http://127.0.0.1:5500", "http://localhost:5500")

                /*
                    ✅ allowedMethods(...)

                    This tells backend:
                    "Frontend is allowed to use these HTTP methods"

                    We allowed:
                    - GET    -> fetch user(s)
                    - POST   -> create new user
                    - PUT    -> update user
                    - DELETE -> delete user
                    - OPTIONS -> browser preflight request

                    ✅ OPTIONS is very important for CORS!

                    Browser often sends an OPTIONS request first (preflight)
                    to ask: "Hey backend, are you allowing this request?"
                    If we don’t allow OPTIONS, frontend calls may fail.
                */
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")

                /*
                    ✅ allowedHeaders("*")

                    This tells backend:
                    "Frontend can send any headers"

                    Example headers frontend might send:
                    - Content-Type: application/json
                    - Authorization: Bearer <token>   (in future for JWT)
                    - Accept: application/json

                    ✅ For learning/dev this is fine.
                    ✅ For production, you may restrict allowed headers if needed.
                */
                .allowedHeaders("*");
    }
}
