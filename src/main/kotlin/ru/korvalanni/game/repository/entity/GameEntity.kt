package ru.korvalanni.game.repository.entity


import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table("games")
data class GameEntity(
    @Id
    val id: UUID = UUID.randomUUID(),
    val width: Int,
    val height: Int,
    val minesCount: Int,
    val completed: Boolean = false,
    val field: String,
    val hiddenField: String,
    @Version
    val version: Long? = null,
)
