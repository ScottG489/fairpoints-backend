import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import java.util.UUID
import util.ConfigUtil

class ChannelSimulation extends Simulation {

  private val baseUrl: String = ConfigUtil.getFromConfig("baseUri")
  private val maxMeanResponseTimeMs = 20000

  private val channelFeeder = Iterator.from(0).map { id =>
    Map(
      "topic" -> "topicA",
      "viewpoint" -> (if (id % 2 == 0) "agree" else "disagree"),
      "identity" -> s"perf-$id-${UUID.randomUUID()}"
    )
  }

  private val httpProtocol: HttpProtocolBuilder = http
    .baseUrl(baseUrl)
  private val request = http("Channel request")
    .get("/chat/channel")
    .queryParam("topicId", "#{topic}")
    .queryParam("viewpoint", "#{viewpoint}")
    .queryParam("identity", "#{identity}")
  private val scn: ScenarioBuilder = scenario("Channel Simulation")
    .feed(channelFeeder)
    .exec(request)

  setUp(
    scn.inject(atOnceUsers(100))
  )
    .protocols(httpProtocol)
    .assertions(
      global.responseTime.mean.lt(maxMeanResponseTimeMs),
      global.failedRequests.count.is(0)
    )
}
