package com.ishan.user_service.mapper;


import com.fasterxml.jackson.databind.JsonNode;
import com.ishan.user_service.dto.UserDto;

public class UserMapperFromRandomToDto {

    public static UserDto convertRandomUserToUserDto(JsonNode randomUser){
        UserDto userDto = new UserDto();
        String fName = randomUser.get("name").get("first").asText();
        String lName = randomUser.get("name").get("last").asText();
        String email = randomUser.get("email").asText();
        String city = randomUser.get("location").get("city").asText();
        String state = randomUser.get("location").get("state").asText();
        int age = randomUser.get("dob").get("age").asInt();
        String phNum = randomUser.get("cell").asText();
        String gender = randomUser.get("gender").asText();

        userDto.setFirstName(fName);
        userDto.setLastName(lName);
        userDto.setEmail(email);
        userDto.setCity(city);
        userDto.setAge(age);
        userDto.setState(state);
        userDto.setPhNum(phNum);
        userDto.setGender(gender);

       // System.out.println("USER DATA FROM API" + userDto);

        return userDto;
    }
}
