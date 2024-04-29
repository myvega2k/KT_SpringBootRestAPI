package com.kt.myrestapi.lectures;

import com.kt.myrestapi.lectures.dto.LectureReqDto;
import com.kt.myrestapi.lectures.validator.LectureValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping(value = "/api/lectures", produces = MediaTypes.HAL_JSON_VALUE)
@RequiredArgsConstructor
public class LectureController {
    private final LectureRepository lectureRepository;
    private final ModelMapper modelMapper;
    private final LectureValidator lectureValidator;

    //Constructor Injection
//    public LectureController(LectureRepository lectureRepository) {
//        this.lectureRepository = lectureRepository;
//    }

    @PostMapping
    public ResponseEntity<?> createLecture(@RequestBody @Valid LectureReqDto lectureReqDto,
                                           Errors errors) {
        //필수입력항목 체크  Java Bean Validator에 정의된 어노테이션 사용
        if(errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }
        //비지니스 로직에 따른 입력항목 체크 직접 정의한 Validator를 호출해 주어야 합니다.
        this.lectureValidator.validate(lectureReqDto, errors);
        if(errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        Lecture lecture = modelMapper.map(lectureReqDto, Lecture.class);

        Lecture addedLecture = lectureRepository.save(lecture);

        // http://localhost:8080/api/lectures/10
        WebMvcLinkBuilder selfLinkBuilder =
                WebMvcLinkBuilder.linkTo(LectureController.class).slash(addedLecture.getId());
        URI createUri = selfLinkBuilder.toUri();
        return ResponseEntity.created(createUri).body(addedLecture);
    }

}