package co.unruly.control_dojos.rollercoasters;

import co.unruly.control.result.Result;
import co.unruly.control_dojos.Progress;
import co.unruly.control_dojos.rollercoasters.domain.Person;
import co.unruly.control_dojos.rollercoasters.domain.RidePhoto;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static co.unruly.control.result.Resolvers.successes;
import static co.unruly.control.result.Transformers.onFailureDo;
import static co.unruly.control_dojos.Chapter.*;
import static co.unruly.control_dojos.Progress.between;
import static co.unruly.control_dojos.rollercoasters.test_support.FamousPeople.*;
import static co.unruly.control_dojos.rollercoasters.test_support.ValidationMatchers.failure;
import static co.unruly.control_dojos.rollercoasters.test_support.ValidationMatchers.success;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class MondoLooperTest {

    @Mock
    private Consumer<List<String>> rejector;

    @After
    public void haventMissedAnything() {
        verifyNoMoreInteractions(rejector);
    }

    @Test
    public void acceptNormalSizedRiders() {
        between($09_COMPOSE_YOURSELF_DEAR, $16_THE_HEROS_JOURNEY);

        assertThat(RollercoasterValidators.forMondoLooper().apply(new Person("Brad Pitt", 185)), is(success()));
    }

    @Test
    public void rejectRidersUnder1m20Tall() {
        between($09_COMPOSE_YOURSELF_DEAR, $10_SAY_MY_NAME_GUESS_MY_HEIGHT);

        assertThat(
            RollercoasterValidators.forMondoLooper().apply(new Person("Warwick Davis", 116)),
            is(failure("You must be 1m20 tall to ride"))
        );
    }

    @Test
    public void rejectRidersOver2m10Tall() {
        assumeTrue(
            Progress.hasStarted($09_COMPOSE_YOURSELF_DEAR) &&
                !Progress.hasStarted($10_SAY_MY_NAME_GUESS_MY_HEIGHT)
        );

        assertThat(
            RollercoasterValidators.forMondoLooper().apply(new Person("Shaquille O'Neal", 224)),
            is(failure("You must be under 2m10 tall to ride"))
        );
    }

    @Test
    public void rejectDonaldTrumpBecauseOfTheBan() {
        between($09_COMPOSE_YOURSELF_DEAR, $16_THE_HEROS_JOURNEY);

        assertThat(
            RollercoasterValidators.forMondoLooper().apply(new Person("Donald Trump", 182)),
            is(failure("SEE YOU IN COURT, THE SECURITY OF OUR ROLLERCOASTER IS AT STAKE"))
        );
    }

    @Test
    public void onlyTakesPhotosOfPeopleAllowedOnRide() {
        between($11_SAY_CHEESE, $16_THE_HEROS_JOURNEY);

        Function<Person, Result<RidePhoto, List<String>>> rideAndGetPhoto = f -> Result.failure(singletonList("Not implemented yet"));

        List<RidePhoto> ridePhotos = Stream.of(HAPPY, SLEEPY, GRUMPY, SNEEZY, DOPEY, BASHFUL, DOC)
            .map(rideAndGetPhoto)
            .peek(onFailureDo(rejector))
            .flatMap(successes())
            .collect(toList());

        RidePhoto[] expectedPhotos = photosFor(HAPPY, SNEEZY, GRUMPY, DOC);

        assertThat(ridePhotos, hasItems(expectedPhotos));

        verify(rejector).accept(singletonList("Dopey must be 1m20 tall to ride"));
        verify(rejector).accept(singletonList("Sleepy must be 1m20 tall to ride"));
        verify(rejector).accept(singletonList("Bashful must be 1m20 tall to ride"));
    }

    @Test
    public void onlyTakesPhotosOfPeopleAllowedOnRideWhoCanBePhotographed() {
        between($12_FANGS_FOR_THE_MEMORIES, $16_THE_HEROS_JOURNEY);

        Function<Person, Result<RidePhoto, List<String>>> rideAndGetPhoto = f -> Result.failure(singletonList("Not implemented yet"));

        List<RidePhoto> ridePhotos = Stream.of(DRACULA, FRANKENSTEIN, WOLFMAN, CHUCKY, FREDDIE_KRUEGER, JASON_VOORHEES)
            .map(rideAndGetPhoto)
            .peek(onFailureDo(rejector))
            .flatMap(successes())
            .collect(toList());

        RidePhoto[] expectedPhotos = photosFor(WOLFMAN, FREDDIE_KRUEGER, JASON_VOORHEES);

        assertThat(ridePhotos, hasItems(expectedPhotos));

        verify(rejector).accept(singletonList("Chucky must be 1m20 tall to ride"));
        verify(rejector).accept(singletonList("The Monster must be under 2m10 tall to ride"));
        verify(rejector).accept(singletonList("Dracula could not be photographed"));
    }

    private static RidePhoto[] photosFor(Person ...people) {
        return Stream.of(people).map(RidePhoto::new).toArray(RidePhoto[]::new);
    }
}
