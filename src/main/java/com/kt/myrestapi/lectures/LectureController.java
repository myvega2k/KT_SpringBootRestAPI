package com.kt.myrestapi.lectures;

import com.kt.myrestapi.common.errors.ErrorsResource;
import com.kt.myrestapi.lectures.dto.LectureReqDto;
import com.kt.myrestapi.lectures.dto.LectureResDto;
import com.kt.myrestapi.lectures.dto.LectureResource;
import com.kt.myrestapi.lectures.validator.LectureValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

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

    @GetMapping
    public ResponseEntity queryLectures(Pageable pageable, PagedResourcesAssembler<LectureResDto> assembler) {
        Page<Lecture> lecturePage = this.lectureRepository.findAll(pageable);
        //Lecture => LectureResDto 변환
        Page<LectureResDto> lectureResDtoPage =
                //Page<T>  map(Function) Function 추상메서드 R apply(T t)
                lecturePage.map(lecture -> modelMapper.map(lecture, LectureResDto.class));
        
        //Page<LectureResDto> => PagedModel<EntityModel<LectureResDto>> 변환
        //PagedModel<EntityModel<LectureResDto>> pagedModel = assembler.toModel(lectureResDtoPage);

        //Page<LectureResDto> => PagedModel<LectureResource> 변환
        //RepresentationalModelAssembler의 추상메서드 D toModel(T)
        //assembler.toModel(lectureResDtoPage, resDto -> new LectureResource(resDto));
        PagedModel<LectureResource> pagedModel = assembler.toModel(lectureResDtoPage, LectureResource::new);
        return ResponseEntity.ok(pagedModel);
    }

    @PostMapping
    public ResponseEntity<?> createLecture(@RequestBody @Valid LectureReqDto lectureReqDto,
                                           Errors errors) {
        //필수입력항목 체크  Java Bean Validator에 정의된 어노테이션 사용
        if(errors.hasErrors()) {
            return getErrors(errors);
        }
        //비지니스 로직에 따른 입력항목 체크 직접 정의한 Validator를 호출해 주어야 합니다.
        this.lectureValidator.validate(lectureReqDto, errors);
        if(errors.hasErrors()) {
            return getErrors(errors);
        }
        //ReqDTO => Entity
        Lecture lecture = modelMapper.map(lectureReqDto, Lecture.class);

        //offline,free 값을 체크해서 저장
        lecture.update();
        Lecture addedLecture = lectureRepository.save(lecture);
        LectureResDto lectureResDto = modelMapper.map(addedLecture, LectureResDto.class);

        // http://localhost:8080/api/lectures/10
        WebMvcLinkBuilder selfLinkBuilder =
                linkTo(LectureController.class).slash(lectureResDto.getId());
        URI createUri = selfLinkBuilder.toUri();

        //생성된 Link를 LectureResource에 저장하기
        LectureResource lectureResource = new LectureResource(lectureResDto);
        lectureResource.add(linkTo(LectureController.class).withRel("query-lectures"));
        lectureResource.add(selfLinkBuilder.withRel("update-lecture"));

        return ResponseEntity.created(createUri).body(lectureResource);
    }

    private static ResponseEntity<?> getErrors(Errors errors) {

        return ResponseEntity.badRequest().body(new ErrorsResource(errors));
    }

}