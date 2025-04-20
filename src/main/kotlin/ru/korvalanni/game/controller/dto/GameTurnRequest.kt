import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import java.util.UUID

data class GameTurnRequest(
    @JsonProperty("game_id")
    val gameId: UUID,

    @JsonProperty("row")
    @field:Min(0)
    @field:Max(29)
    val row: Int,

    @JsonProperty("col")
    @field:Min(0)
    @field:Max(29)
    val col: Int
)