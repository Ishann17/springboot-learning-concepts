package com.ishan.user_service.service;

import com.github.javafaker.Faker;
import com.ishan.user_service.dto.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MockUserGeneratorService {
    private static final Logger log = LoggerFactory.getLogger(MockUserGeneratorService.class);
    private final Faker faker = new Faker();

    //private final Faker faker = new Faker(java.util.Locale.of("en", "IN"));
    public List<UserDto> generateUsers(int count) {
        long startTime = System.currentTimeMillis();
        log.info("Generating users. TotalUsers={}", count);
        List<UserDto> userDtoList = new ArrayList<>(count);
        for(int i=0; i<count; i++){
            UserDto userDto = new UserDto();
            userDto.setFirstName(faker.name().firstName());
            userDto.setLastName(faker.name().lastName());
            userDto.setAge(faker.number().numberBetween(16,99));
            userDto.setGender(faker.demographic().sex());
            userDto.setCity(faker.address().city());
            userDto.setState(faker.address().state());
            userDto.setEmail(faker.internet().emailAddress());
            userDto.setPhNum(faker.phoneNumber().cellPhone());
            userDtoList.add(userDto);
            //System.out.println( "FAKE USER GENERATED :: " + userDto);
        }
        long endTime = System.currentTimeMillis();
        double executionTimeInSeconds = (endTime - startTime) / 1000.0;

        log.info("Generated Users. TotalUsers={} TotalTime={}s",
                count, String.format("%.2f", executionTimeInSeconds));

        return userDtoList;
    }
}
