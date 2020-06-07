import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.assertj.core.api.Assertions.assertThat;

@Test(groups = {"all"})
public class PlanetApiTest {
    private final RequestSpecification baseSpecification = new RequestSpecBuilder()
            .setBaseUri("https://swapi.dev/api/")
            .setBasePath("planets")
            .addFilter(new AllureRestAssured())
            .log(LogDetail.ALL)
            .build();

    @Test
    public void searchByExactNameShouldReturnOneObject() {
        Planet expectedPlanet = Planet.builder()
                .name("Alderaan")
                .rotationPeriod("24")
                .orbitalPeriod("364")
                .diameter("12500")
                .climate("temperate")
                .gravity("1 standard")
                .terrain("grasslands, mountains")
                .surfaceWater("40")
                .population("2000000000")
                .residents(new ArrayList<String>() {{
                    add("http://swapi.dev/api/people/5/");
                    add("http://swapi.dev/api/people/68/");
                    add("http://swapi.dev/api/people/81/");
                }})
                .films(new ArrayList<String>() {{
                    add("http://swapi.dev/api/films/1/");
                    add("http://swapi.dev/api/films/6/");
                }})
                .created("2014-12-10T11:35:48.479000Z")
                .edited("2014-12-20T20:58:18.420000Z")
                .url("http://swapi.dev/api/planets/2/")
                .build();

        Response response = getPlanetByName("Alderaan");
        assertResponseWithStatusCode(response, HTTP_OK);
        List<Planet> planets = response
                .then()
                .extract()
                .jsonPath()
                .getList("results", Planet.class);

        assertThat(planets).hasSize(1);
        Planet actualPlanet = planets.get(0);
        assertThat(actualPlanet).isEqualTo(expectedPlanet);
    }

    @Test(groups = {"smoke"})
    public void requestPlanetByIdShouldReturnInfoAboutPlanet() {
        Planet expectedPlanet = Planet.builder()
                .name("Yavin IV")
                .rotationPeriod("24")
                .orbitalPeriod("4818")
                .diameter("10200")
                .climate("temperate, tropical")
                .gravity("1 standard")
                .terrain("jungle, rainforests")
                .surfaceWater("8")
                .population("1000")
                .created("2014-12-10T11:37:19.144000Z")
                .edited("2014-12-20T20:58:18.421000Z")
                .build();
        Response response = getPlanetById(3);
        assertResponseWithStatusCode(response, HTTP_OK);
        Planet actualPlanet = response
                .then()
                .extract()
                .jsonPath()
                .getObject("", Planet.class);

        assertThat(actualPlanet).isEqualToIgnoringGivenFields(expectedPlanet,
                "url", "residents", "films");
    }

    @Test(groups = {"smoke"})
    public void requestNonExistingPlanetShouldReturn404StatusCode() {
        assertResponseWithStatusCode(getPlanetById(0), HTTP_NOT_FOUND);
    }

    @Step("Check response has status code {statusCode}")
    private void assertResponseWithStatusCode(Response response, int statusCode) {
        response
                .then()
                .assertThat()
                .statusCode(statusCode);
    }

    @Step("Get planet by planet name \"{planetName}\".")
    private Response getPlanetByName(String planetName) {
        return given()
                .spec(baseSpecification)
                .queryParam("search", planetName)
                .when()
                .get();
    }

    @Step("Get planet by id={id}")
    private Response getPlanetById(int id) {
        return given()
                .spec(baseSpecification)
                .pathParam("id", id)
                .when()
                .get("{id}");
    }
}