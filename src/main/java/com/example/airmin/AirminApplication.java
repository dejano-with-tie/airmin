package com.example.airmin;

import com.cosium.spring.data.jpa.entity.graph.repository.support.EntityGraphJpaRepositoryFactoryBean;
import com.example.airmin.model.Airport;
import com.example.airmin.model.Comment;
import com.example.airmin.model.Route;
import com.example.airmin.rest.dto.AirportDto;
import com.example.airmin.rest.dto.CityCommentDto;
import com.example.airmin.rest.dto.CityDto;
import com.example.airmin.rest.dto.RouteDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.csv.CSVFormat;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
@ConfigurationPropertiesScan("com.example.airmin")
@EnableJpaRepositories(repositoryFactoryBeanClass = EntityGraphJpaRepositoryFactoryBean.class)
@EnableJpaAuditing
@EnableCaching
public class AirminApplication {

    public static void main(String[] args) {
        SpringApplication.run(AirminApplication.class, args);
    }

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Bean
    public ModelMapper modelMapper() {
        final var modelMapper = new ModelMapper();
        modelMapper.typeMap(Comment.class, CityCommentDto.class)
                .addMapping(source -> source.getAuthor().getUsername(), CityCommentDto::setAuthor);
        modelMapper.addConverter(new AbstractConverter<Route, RouteDto>() {
            protected RouteDto convert(Route route) {
                final Airport sourceAirport = route.getSource();
                final CityDto sourceCity = new CityDto(sourceAirport.getCity().getName(),
                        sourceAirport.getCity().getCountry(),
                        sourceAirport.getCity().getDescription());
                sourceCity.setId(sourceAirport.getCity().getId());

                final Airport destAirport = route.getDestination();
                final CityDto destCity = new CityDto(destAirport.getCity().getName(),
                        destAirport.getCity().getCountry(),
                        destAirport.getCity().getDescription());
                destCity.setId(destAirport.getCity().getId());

                return new RouteDto(
                        new AirportDto(sourceAirport.getName(), sourceCity),
                        new AirportDto(destAirport.getName(), destCity),
                        route.getPrice());
            }
        });


        return modelMapper;
    }

    @Bean
    public CSVFormat csvFormat() {
        return CSVFormat.DEFAULT.withEscape('\\').withNullString("\\N");
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
