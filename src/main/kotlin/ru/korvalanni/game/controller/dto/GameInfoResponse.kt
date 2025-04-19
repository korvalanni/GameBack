import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

data class GameInfoResponse(
    @JsonProperty("game_id")
    val gameId: UUID,

    @JsonProperty("width")
    val width: Int,

    @JsonProperty("height")
    val height: Int,

    @JsonProperty("mines_count")
    val minesCount: Int,

    @JsonProperty("completed")
    val completed: Boolean,

    @JsonProperty("field")
    val field: List<List<String>>
)