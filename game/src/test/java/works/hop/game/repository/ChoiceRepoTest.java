package works.hop.game.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import works.hop.game.config.RepoTestConfig;
import works.hop.game.model.Choice;
import works.hop.game.model.Player;
import works.hop.game.model.Question;

import javax.sql.DataSource;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RepoTestConfig.class)
class ChoiceRepoTest {

    @Autowired
    DataSource embeddedDataSource;
    ChoiceRepo choiceRepo;
    PlayerRepo playerRepo;
    QuestionRepo questionRepo;

    @BeforeEach
    void setUp() {
        choiceRepo = new ChoiceRepo(embeddedDataSource);
        playerRepo = new PlayerRepo(embeddedDataSource);
        questionRepo = new QuestionRepo(embeddedDataSource);
    }

    @Test
    void createAndUpdateChoice() {
        Player crazyeyes = playerRepo.getByEmail("jimmy.crazyeyes@email.com");
        Choice newChoice = new Choice();
        newChoice.setOrdinal(1);
        newChoice.setQuestionRef(2L);
        newChoice.setExplanation("it's two");
        newChoice.setChoiceValue("2");
        newChoice = choiceRepo.createChoice(newChoice);

        Question que = questionRepo.getByValue(crazyeyes.getId(), "1 + 1");

        newChoice.setQuestionRef(que.getId());
        newChoice.setChoiceValue("7");
        newChoice = choiceRepo.updateChoice(newChoice);
        List<Choice> updated = choiceRepo.getByRef(newChoice.getQuestionRef());
        assertThat(updated).hasSize(4);
        assertThat(updated.stream().filter(v -> v.getChoiceValue().equals("7")).findFirst()).isPresent();
    }

    @Test
    void createAndRemoveChoice() {
        Choice newChoice = new Choice();
        newChoice.setOrdinal(2);
        newChoice.setQuestionRef(2L);
        newChoice.setExplanation("it's three");
        newChoice.setChoiceValue("3");
        newChoice = choiceRepo.createChoice(newChoice);

        List<Choice> choices = choiceRepo.getByRef(newChoice.getQuestionRef());
        assertThat(choices).isNotEmpty();

        choiceRepo.removeChoice(2, 2L);
        List<Choice> choices2 = choiceRepo.getByRef(2L);
        assertThat(choices2).hasSize(choices.size() - 1);
    }
}