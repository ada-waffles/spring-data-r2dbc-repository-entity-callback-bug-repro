package com.example.demo

import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.runBlocking
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.data.r2dbc.mapping.OutboundRow
import org.springframework.data.r2dbc.mapping.event.AfterConvertCallback
import org.springframework.data.r2dbc.mapping.event.AfterSaveCallback
import org.springframework.data.r2dbc.mapping.event.BeforeConvertCallback
import org.springframework.data.r2dbc.mapping.event.BeforeSaveCallback
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query.query
import org.springframework.data.relational.core.sql.SqlIdentifier
import org.springframework.stereotype.Component

@Component
class DemoCallbacks :
    BeforeConvertCallback<DemoEntity>,
    AfterConvertCallback<DemoEntity>,
    BeforeSaveCallback<DemoEntity>,
    AfterSaveCallback<DemoEntity>
{
    override fun onBeforeConvert(entity: DemoEntity, table: SqlIdentifier) = mono {
        println("onBeforeConvert called with $entity")
        entity
    }

    override fun onAfterConvert(entity: DemoEntity, table: SqlIdentifier) = mono {
        println("onAfterConvert called with $entity")
        entity
    }

    override fun onBeforeSave(entity: DemoEntity, row: OutboundRow, table: SqlIdentifier) = mono {
        println("onBeforeSave called with $entity")
        entity
    }

    override fun onAfterSave(entity: DemoEntity, outboundRow: OutboundRow, table: SqlIdentifier) = mono {
        println("onAfterSave called with $entity")
        entity
    }
}

//Separate class to avoid circular dependency with the R2dbcEntityOperations
@Component
class DemoStartupListener(
    private val entityOperations: R2dbcEntityOperations,
    private val demoRepository: DemoRepository
) {
    @EventListener
    fun onApplicationStarted(event: ApplicationStartedEvent) = runBlocking<Unit> {
        //Calls with the context R2dbcEntityOperations work
        println("Running context R2dbcEntityOperations queries")
        val opsEntityId = entityOperations.insert(DemoEntity(data = "context R2dbcEntityOperations")).awaitSingle().id!!
        //Select to trigger onAfterConvert
        entityOperations.selectOne(
            query(where(DemoEntity::id.name).`is`(opsEntityId)),
            DemoEntity::class.java
        ).awaitSingle()

        //But there won't be any log output for these
        println("Running repository queries")
        val repoEntityId = demoRepository.save(DemoEntity(data = "repository")).id!!
        demoRepository.findById(repoEntityId)

        println("Done")
    }
}
