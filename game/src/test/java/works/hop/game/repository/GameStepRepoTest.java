package works.hop.game.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import works.hop.game.config.RepoTestConfig;
import works.hop.game.model.*;

import javax.sql.DataSource;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RepoTestConfig.class)
class GameStepRepoTest {

    @Autowired
    DataSource embeddedDataSource;
    GameStepRepo gameStepRepo;
    GameRepo gameRepo;
    QuestionRepo questionRepo;
    PlayerRepo playerRepo;

    @BeforeEach
    void setUp() {
        gameRepo = new GameRepo(embeddedDataSource);
        gameStepRepo = new GameStepRepo(embeddedDataSource);
        questionRepo = new QuestionRepo(embeddedDataSource);
        playerRepo = new PlayerRepo(embeddedDataSource);
    }

    @Test
    void createAndUpdateGameStep() {
        Player crazyeyes = playerRepo.getByEmail("jimmy.crazyeyes@email.com");
        Game game = gameRepo.getByOrganizer(crazyeyes.getId()).stream()
                .filter(g -> g.getTitle().equals("eye of a tiger")).findFirst().orElseThrow();
        Question que1 = questionRepo.getByAuthor(crazyeyes.getId()).stream()
                .filter(q -> q.getQuestion().equals("1 + 1")).findFirst().orElseThrow();

        GameStep newGameStep = new GameStep();
        newGameStep.setGroupNum(1);
        newGameStep.setQuestionNum(1);
        newGameStep.setGameRef(game.getId());
        newGameStep.setQuestionRef(que1.getId());
        newGameStep.setAutoProgression(true);
        newGameStep.setDelayAfterCountdown(1000L);
        newGameStep.setDelayBeforeCountdown(1000L);
        newGameStep.setCountdownDuration(10000L);
        newGameStep.setCountdownIntervals(500L);
        newGameStep.setStepStatus(StepStatus.COMING_UP);
        newGameStep = gameStepRepo.createGameStep(newGameStep);
        assertThat(newGameStep.getMaxPoints()).isZero();

        // now update a field
        newGameStep.setQuestionNum(2);
        newGameStep.setMaxPoints(100);
        GameStep updated1 = gameStepRepo.updateGameStep(newGameStep);
        assertThat(updated1.getQuestionNum()).isEqualTo(newGameStep.getQuestionNum());
        assertThat(updated1.getMaxPoints()).isEqualTo(newGameStep.getMaxPoints());
    }

    @Test
    void createAndRemoveGameStep() {
        Player crazyeyes = playerRepo.getByEmail("jimmy.crazyeyes@email.com");
        Game game = gameRepo.getByOrganizer(crazyeyes.getId()).stream()
                .filter(g -> g.getTitle().equals("eye of a tiger")).findFirst().orElseThrow();
        Question que1 = questionRepo.getByAuthor(crazyeyes.getId()).stream()
                .filter(q -> q.getQuestion().equals("1 + 1")).findFirst().orElseThrow();

        GameStep newGameStep = new GameStep();
        newGameStep.setGroupNum(1);
        newGameStep.setQuestionNum(1);
        newGameStep.setGameRef(game.getId());
        newGameStep.setQuestionRef(que1.getId());
        newGameStep.setAutoProgression(true);
        newGameStep.setDelayAfterCountdown(1000L);
        newGameStep.setDelayBeforeCountdown(1000L);
        newGameStep.setCountdownDuration(10000L);
        newGameStep.setCountdownIntervals(500L);
        newGameStep.setStepStatus(StepStatus.COMING_UP);
        newGameStep = gameStepRepo.createGameStep(newGameStep);
        assertThat(newGameStep.getMaxPoints()).isZero();

        List<GameStep> steps = gameStepRepo.getGameSteps(game.getId());
        assertThat(steps).hasSize(1);

        gameStepRepo.removeGameStep(newGameStep.getGroupNum(), newGameStep.getQuestionNum());
        steps = gameStepRepo.getGameSteps(game.getId());
        assertThat(steps).isEmpty();
    }
}