package com.kt.myrestapi.common.errors;

import java.io.IOException;

import org.springframework.boot.jackson.JsonComponent;
import org.springframework.validation.Errors;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

@JsonComponent
public class ErrorsSerializer extends JsonSerializer<Errors>{
	@Override
	public void serialize(Errors errors, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		gen.writeStartArray();
        //Iterable.forEach(Consumer) Consumer의 추상메서드 void accept(T t)
        errors.getFieldErrors().forEach(e -> {
            try {
                gen.writeStartObject();
                //Field 의 이름
                gen.writeStringField("field", e.getField());
                //Field가 속한 DTO 객체 의 이름
                gen.writeStringField("objectName", e.getObjectName());
                //NotEmpty, NotBlank
                gen.writeStringField("code", e.getCode());
                //Error Message
                gen.writeStringField("defaultMessage", e.getDefaultMessage());
                //잘못 입력된 값
                Object rejectedValue = e.getRejectedValue();
                if (rejectedValue != null) {
                    gen.writeStringField("rejectedValue", rejectedValue.toString());
                }
                gen.writeEndObject();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });

        errors.getGlobalErrors().forEach(e -> {
            try {
                gen.writeStartObject();
                gen.writeStringField("objectName", e.getObjectName());
                gen.writeStringField("code", e.getCode());
                gen.writeStringField("defaultMessage", e.getDefaultMessage());
                gen.writeEndObject();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        gen.writeEndArray();
		
	}
}