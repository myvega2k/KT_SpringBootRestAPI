package com.kt.myrestapi.lectures;

import com.kt.myrestapi.common.errors.ErrorsResource;
import com.kt.myrestapi.common.exception.BusinessException;
import com.kt.myrestapi.lectures.dto.LectureReqDto;
import com.kt.myrestapi.lectures.dto.LectureResDto;
import com.kt.myrestapi.lectures.dto.LectureResource;
import com.kt.myrestapi.lectures.validator.LectureValidator;
import com.kt.myrestapi.security.userinfo.UserInfo;
import com.kt.myrestapi.security.userinfo.annot.CurrentUser;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;

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

    @PutMapping("/{id}")
    public ResponseEntity updateLecture(@PathVariable Integer id,
                                        @RequestBody @Valid LectureReqDto lectureReqDto,
                                        Errors errors,
                                        @CurrentUser UserInfo currentUser) {
        Lecture existingLecture = getExistingLecture(id);

        if (errors.hasErrors()) {
            return getErrors(errors);
        }

        lectureValidator.validate(lectureReqDto, errors);
        if (errors.hasErrors()) {
            return getErrors(errors);
        }

        //Lecture가 참조하는 UserInfo 객체와 인증한 UserInfo 객체가 다르면 401 인증 오류
        if((existingLecture.getUserInfo() != null) && (!existingLecture.getUserInfo().equals(currentUser))) {
            throw new BadCredentialsException("등록한 User와 수정을 요청한 User가 다릅니다.");
            //return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

        this.modelMapper.map(lectureReqDto, existingLecture);
        existingLecture.update();
        Lecture savedLecture = this.lectureRepository.save(existingLecture);
        LectureResDto lectureResDto = modelMapper.map(savedLecture, LectureResDto.class);

        //Lecture 객체와 연관된 UserInfo 객체가 있다면 LectureResDto에 email을 set
        if(savedLecture.getUserInfo() != null)
            lectureResDto.setEmail(savedLecture.getUserInfo().getEmail());

        LectureResource lectureResource = new LectureResource(lectureResDto);
        return ResponseEntity.ok(lectureResource);
    }

    private Lecture getExistingLecture(Integer id) {
        Lecture existingLecture = lectureRepository.findById(id)  //Optional<Lecture>
                .orElseThrow(() -> {
                    String errMsg = String.format("Id = %d Lecture Not Found", id);
                    return new BusinessException(errMsg, HttpStatus.NOT_FOUND);
                });
        return existingLecture;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity getLecture(@PathVariable Integer id,
                                     @CurrentUser UserInfo currentUser) {
//        Optional<Lecture> optionalLecture = this.lectureRepository.findById(id);
//        if(optionalLecture.isEmpty()) {
//            return ResponseEntity.notFound().build();
//        }
//        Lecture lecture = optionalLecture.get();

        Lecture lecture = getExistingLecture(id);

        LectureResDto lectureResDto = modelMapper.map(lecture, LectureResDto.class);
        //Lecture 객체와 연관된 UserInfo 객체가 있다면 LectureResDto 에 UserInfo 객체의 email set
        if (lecture.getUserInfo() != null)
            lectureResDto.setEmail(lecture.getUserInfo().getEmail());

        LectureResource lectureResource = new LectureResource(lectureResDto);
        //Lecture가 참조하는 email과 인증토큰의 email이 같으면 update 링크를 생성하기
        if ((lecture.getUserInfo() != null) && (lecture.getUserInfo().equals(currentUser))) {
            lectureResource.add(linkTo(LectureController.class)
                    .slash(lecture.getId()).withRel("update-lecture"));
        }
        return ResponseEntity.ok(lectureResource);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
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