import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import util.ConfigUtil

class TokenSimulation extends Simulation {

  private val baseUrl: String = ConfigUtil.getFromConfig("baseUri")

  private val httpProtocol: HttpProtocolBuilder = http
    .baseUrl(baseUrl)
  private val request = http("Token request")
    .get("/chat/token")
  private val scn: ScenarioBuilder = scenario("Token Simulation")
    .exec(request)

  setUp(
    scn.inject(atOnceUsers(100))
  )
    .protocols(httpProtocol)
    .assertions(
      global.responseTime.mean.lt(500),
      global.failedRequests.count.is(0)
    )
}