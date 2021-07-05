package com.epam.esm.controller;

import com.epam.esm.dto.TagDto;
import com.epam.esm.response.CustomCode;
import com.epam.esm.response.CustomResponse;
import com.epam.esm.exception.EntityAlreadyExistsException;
import com.epam.esm.exception.EntityNotExistException;
import com.epam.esm.exception.EntityNotFoundException;
import com.epam.esm.exception.NotValidFieldsException;
import com.epam.esm.model.TagModel;
import com.epam.esm.model.assembler.TagModelAssembler;
import com.epam.esm.service.LocaleService;
import com.epam.esm.service.TagService;
import com.epam.esm.validator.TagValidator;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;


import javax.validation.Valid;

/**
 * Rest controller that processes requests with the tags.
 *
 * @author Shuleiko Yulia
 */
@RestController
@RequestMapping("/tags")
public class TagController {

    private static final String ENTITY_NOT_FOUND_ERROR = "entity_not_found";
    private static final String ENTITY_NOT_EXIST_ERROR = "entity_not_exist";
    private static final String TAG_WAS_CREATED_MESSAGE = "tag_was_created";
    private static final String TAG_WAS_DELETED_MESSAGE = "tag_was_deleted";
    private TagService tagService;
    private TagValidator tagValidator;
    private LocaleService localeService;
    private TagModelAssembler tagModelAssembler;

    /**
     * Construct controller with all necessary dependencies.
     */
    public TagController(TagService tagService,
                         TagValidator tagValidator,
                         LocaleService localeService,
                         TagModelAssembler tagModelAssembler) {
        this.tagService = tagService;
        this.tagValidator = tagValidator;
        this.localeService = localeService;
        this.tagModelAssembler = tagModelAssembler;
    }

    /**
     * Set validator to binder.
     *
     * @param binder the {@code WebDataBinder} object
     */
    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(tagValidator);
    }

    /**
     * Delete tag.
     *
     * @param id id of the tag
     */
    @DeleteMapping(value = "/{id}")
    public CustomResponse deleteTag(@PathVariable long id) {
        tagService.deleteTag(id);
        return new CustomResponse(CustomCode.TAG_WAS_DELETED.code,
                localeService.getLocaleMessage(TAG_WAS_DELETED_MESSAGE, id));
    }

    /**
     * Find tag by id.
     *
     * @param id id of the tag
     * @return the {@code TagDto} object
     */
    @GetMapping(value = "/{id}")
    public TagModel findTagById(@PathVariable long id) {
        return tagModelAssembler.toModel(tagService.findTagById(id));
    }

    /**
     * Create new tag.
     *
     * @param tagDto        the {@code TagDto} object
     * @param bindingResult the {@code BindingResult} object
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public CustomResponse createTag(@RequestBody @Valid TagDto tagDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new NotValidFieldsException(bindingResult);
        }
        long id = tagService.createTag(tagDto);
        return new CustomResponse(CustomCode.TAG_WAS_CREATED.code,
                localeService.getLocaleMessage(TAG_WAS_CREATED_MESSAGE, id));
    }

    /**
     * Handle the {@code EntityNotExistException}.
     *
     * @param ex the {@code EntityNotExistException} object
     * @return the {@code CustomError} object
     */
    @ExceptionHandler(EntityNotExistException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CustomResponse handleTagNotExists(EntityNotExistException ex) {
        return new CustomResponse(CustomCode.TAG_NOT_EXIST.code,
                localeService.getLocaleMessage(ENTITY_NOT_EXIST_ERROR, ex.getId()));
    }

    /**
     * Handle the {@code EntityNotFoundException}.
     *
     * @param ex the {@code EntityNotFoundException} object
     * @return the {@code CustomError} object
     */
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public CustomResponse handleTagNotFound(EntityNotFoundException ex) {
        return new CustomResponse(CustomCode.TAG_NOT_FOUND.code,
                localeService.getLocaleMessage(ENTITY_NOT_FOUND_ERROR, ex.getId()));
    }

    /**
     * Handle the {@code EntityAlreadyExistsException}.
     *
     * @param ex the {@code EntityAlreadyExistsException} object
     * @return the {@code CustomError} object
     */
    @ExceptionHandler(EntityAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CustomResponse handleTagAlreadyExists(EntityAlreadyExistsException ex) {
        return new CustomResponse(CustomCode.TAG_WAS_CREATED.code,
                localeService.getLocaleMessage(TAG_WAS_CREATED_MESSAGE, ex.getId()));
    }

    /**
     * Handle the {@code NotValidFieldsException}.
     *
     * @param ex the {@code NotValidFieldsException} object
     * @return the {@code CustomError} object
     */
    @ExceptionHandler(NotValidFieldsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CustomResponse handleTagFieldsNotValid(NotValidFieldsException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        StringBuilder errorMessage = new StringBuilder();
        for (Object object : bindingResult.getAllErrors()) {
            if (object instanceof FieldError) {
                FieldError fieldError = (FieldError) object;
                errorMessage.append(localeService.getLocaleMessage(fieldError.getCode()));
            }
        }
        return new CustomResponse(CustomCode.TAG_FIELDS_NOT_VALID.code, errorMessage.toString());
    }
}


