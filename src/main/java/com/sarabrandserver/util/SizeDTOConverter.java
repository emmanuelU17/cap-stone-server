package com.sarabrandserver.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarabrandserver.exception.InvalidFormat;
import com.sarabrandserver.product.util.SizeInventoryDTO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

/** Incase there is only one item instead of a string[] */
@Component
public class SizeDTOConverter implements Converter<String, SizeInventoryDTO[]> {

    private static final Logger log = Logger.getLogger(SizeDTOConverter.class.getName());

    private final ObjectMapper objectMapper;

    public SizeDTOConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public SizeInventoryDTO[] convert(String source) {
        try {
            return new SizeInventoryDTO[]{ objectMapper.readValue(source, SizeInventoryDTO.class) };
        } catch (JsonProcessingException e) {
            log.info("Incorrect format SizeInventoryDTOConverter. " + e.getMessage());
            throw new InvalidFormat("Invalid size and inventory");
        }
    }

}
