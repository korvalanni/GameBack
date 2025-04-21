package ru.korvalanni.game.repository

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query.query
import org.springframework.data.relational.core.query.Update
import org.springframework.stereotype.Repository
import ru.korvalanni.game.repository.entity.GameEntity
import java.util.UUID

@Repository
class GameEntityRepository(
    private val template: R2dbcEntityTemplate,
) {
    suspend fun save(entity: GameEntity): GameEntity =
        template.insert(GameEntity::class.java).using(entity)
            .awaitSingle()

    suspend fun findById(id: UUID): GameEntity? =
        template.select(GameEntity::class.java)
            .matching(query(where(GameEntity::id.name).`is`(id)))
            .one()
            .awaitSingleOrNull()

    suspend fun update(entity: GameEntity, isFirstMove: Boolean): GameEntity {
        val currentVersion = entity.version
            ?: throw IllegalStateException("Version must not be null for update with optimistic locking")

        val newVersion = currentVersion + 1

        var updateSpec = Update
            .update(GameEntity::field.name, entity.field)
            .set(GameEntity::completed.name, entity.completed)
            .set(GameEntity::version.name, newVersion)

        if(isFirstMove) {
            updateSpec = updateSpec.set(GameEntity::hiddenField.name, entity.hiddenField)
        }

        val updatedCount = template.update(GameEntity::class.java)
            .matching(
                query(
                    where(GameEntity::id.name).`is`(entity.id)
                        .and(where(GameEntity::version.name).`is`(currentVersion))
                )
            )
            .apply(updateSpec)
            .awaitSingle()

        if (updatedCount != 1L) {
            throw IllegalStateException("Concurrent update error for game ${entity.id}")
        }

        return entity.copy(version = newVersion)
    }
}


