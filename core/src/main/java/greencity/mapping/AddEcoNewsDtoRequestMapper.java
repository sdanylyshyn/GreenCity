package greencity.mapping;

import greencity.constant.ErrorMessage;
import greencity.dto.econews.AddEcoNewsDtoRequest;
import greencity.entity.EcoNews;
import greencity.entity.localization.EcoNewsTranslation;
import greencity.exception.exceptions.BadIdException;
import greencity.exception.exceptions.LanguageNotFoundException;
import greencity.exception.exceptions.TagNotFoundException;
import greencity.repository.LanguageRepository;
import greencity.repository.TagRepo;
import greencity.repository.UserRepo;
import java.time.ZonedDateTime;
import java.util.stream.Collectors;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class that used by {@link ModelMapper} to map {@link AddEcoNewsDtoRequest} into
 * {@link EcoNews}.
 */
@Component
public class AddEcoNewsDtoRequestMapper extends AbstractConverter<AddEcoNewsDtoRequest, EcoNews> {
    private LanguageRepository languageRepository;
    private UserRepo userRepo;
    private TagRepo tagRepo;

    /**
     * All args constructor.
     *
     * @param languageRepository repository for getting language.
     * @param userRepo           repository for getting author.
     * @param tagRepo            repository for getting tags.
     */
    @Autowired
    public AddEcoNewsDtoRequestMapper(LanguageRepository languageRepository,
                                      UserRepo userRepo, TagRepo tagRepo) {
        this.languageRepository = languageRepository;
        this.userRepo = userRepo;
        this.tagRepo = tagRepo;
    }

    /**
     * Method for converting {@link AddEcoNewsDtoRequest} into {@link EcoNews}.
     *
     * @param addEcoNewsDtoRequest object to convert.
     * @return converted object.
     */
    @Override
    protected EcoNews convert(AddEcoNewsDtoRequest addEcoNewsDtoRequest) {
        EcoNews ecoNews = EcoNews.builder()
            .creationDate(ZonedDateTime.now())
            .author(userRepo.findById(addEcoNewsDtoRequest.getAuthor().getId()).orElseThrow(
                () -> new BadIdException(ErrorMessage.USER_NOT_FOUND_BY_ID + addEcoNewsDtoRequest.getAuthor().getId())
            ))
            .imagePath(addEcoNewsDtoRequest.getImagePath())
            .build();

        ecoNews.setTags(addEcoNewsDtoRequest.getTags()
            .stream()
            .map(tag -> tagRepo.findByName(tag.getName()).orElseThrow(() ->
                new TagNotFoundException(ErrorMessage.TAG_NOT_FOUND + tag.getName())))
            .collect(Collectors.toList())
        );

        ecoNews.setTranslations(addEcoNewsDtoRequest.getTranslations()
            .stream()
            .map(translation ->
                new EcoNewsTranslation(null,
                    languageRepository.findByCode(translation.getLanguage().getCode())
                        .orElseThrow(() -> new LanguageNotFoundException(ErrorMessage.INVALID_LANGUAGE_CODE)),
                    translation.getTitle(), translation.getText(), ecoNews))
            .collect(Collectors.toList()));

        return ecoNews;
    }
}
