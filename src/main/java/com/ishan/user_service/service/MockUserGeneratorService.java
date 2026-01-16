package com.ishan.user_service.service;

import com.github.javafaker.Faker;
import com.ishan.user_service.dto.UserDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MockUserGeneratorService {

    private final Faker faker = new Faker();
    public List<UserDto> generateUsers(int count) {
        List<UserDto> userDtoList =new ArrayList<>();
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

        return userDtoList;
    }
}
