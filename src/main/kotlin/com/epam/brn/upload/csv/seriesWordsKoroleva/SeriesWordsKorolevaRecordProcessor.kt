package com.epam.brn.upload.csv.seriesWordsKoroleva

import com.epam.brn.enums.Locale
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Exercise
import com.epam.brn.model.Resource
import com.epam.brn.model.SubGroup
import com.epam.brn.model.Task
import com.epam.brn.model.WordType
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.ResourceRepository
import com.epam.brn.repo.SubGroupRepository
import com.epam.brn.service.AudioFileMetaData
import com.epam.brn.service.WordsService
import com.epam.brn.upload.csv.RecordProcessor
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class SeriesWordsKorolevaRecordProcessor(
    private val subGroupRepository: SubGroupRepository,
    private val resourceRepository: ResourceRepository,
    private val exerciseRepository: ExerciseRepository,
    private val wordsService: WordsService
) : RecordProcessor<SeriesWordsKorolevaRecord, Exercise> {

    @Value(value = "\${brn.picture.file.default.path}")
    private lateinit var pictureDefaultPath: String

    override fun isApplicable(record: Any): Boolean = record is SeriesWordsKorolevaRecord

    @Transactional
    override fun process(records: List<SeriesWordsKorolevaRecord>, locale: Locale): List<Exercise> {
        val exercises = mutableSetOf<Exercise>()
        records.forEach { record ->
            val subGroup = subGroupRepository.findByCodeAndLocale(record.code, locale.locale)
                ?: throw EntityNotFoundException("No subGroup was found for code=${record.code} and locale={${locale.locale}}")
            val existExercise = exerciseRepository.findExerciseByNameAndLevel(record.exerciseName, record.level)
            if (!existExercise.isPresent) {
                val answerOptions = extractAnswerOptions(record, locale)
                wordsService.addWordsToDictionary(locale, answerOptions.map { resource -> resource.word })
                resourceRepository.saveAll(answerOptions)

                val newExercise = generateExercise(record, subGroup)
                newExercise.addTask(generateOneTask(newExercise, answerOptions))
                exerciseRepository.save(newExercise)
                exercises.add(newExercise)
            }
        }
        return exercises.toMutableList()
    }

    private fun extractAnswerOptions(record: SeriesWordsKorolevaRecord, locale: Locale): MutableSet<Resource> =
        record.words
            .asSequence()
            .map { toStringWithoutBraces(it) }
            .map { toResource(it, locale) }
            .toMutableSet()

    private fun toResource(word: String, locale: Locale): Resource {
        val audioPath = wordsService.getSubFilePathForWord(
            AudioFileMetaData(
                word,
                locale.locale,
                wordsService.getDefaultWomanVoiceForLocale(locale.locale)
            )
        )
        val resource =
            resourceRepository.findFirstByWordAndLocaleAndWordType(word, locale.locale, WordType.OBJECT.toString())
                .orElse(
                    Resource(
                        word = word,
                        pictureFileUrl = pictureDefaultPath.format(word),
                        locale = locale.locale,
                    )
                )
        resource.audioFileUrl = audioPath
        resource.wordType = WordType.OBJECT.toString()
        return resource
    }

    private fun toStringWithoutBraces(it: String) = it.replace("[()]".toRegex(), StringUtils.EMPTY)

    private fun generateExercise(record: SeriesWordsKorolevaRecord, subGroup: SubGroup) =
        Exercise(
            subGroup = subGroup,
            name = record.exerciseName,
            level = record.level,
            playWordsCount = record.playWordsCount,
            wordsColumns = record.wordsColumns,
        )

    private fun generateOneTask(exercise: Exercise, answerOptions: MutableSet<Resource>) =
        Task(exercise = exercise, answerOptions = answerOptions)
}
