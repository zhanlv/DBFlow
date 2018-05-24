package com.dbflow5.rx2.query

import com.dbflow5.BaseUnitTest
import com.dbflow5.config.databaseForTable
import com.dbflow5.database.DatabaseStatement
import com.dbflow5.database.FlowCursor
import com.dbflow5.models.SimpleModel
import com.dbflow5.models.SimpleModel_Table.name
import com.dbflow5.query.insert
import com.dbflow5.query.property.Property
import com.dbflow5.query.select
import com.dbflow5.query.selectCountOf
import com.dbflow5.reactivestreams.transaction.asMaybe
import com.dbflow5.reactivestreams.transaction.asSingle
import com.dbflow5.structure.save
import org.junit.Assert.assertEquals
import org.junit.Test

class RXQueryTests : BaseUnitTest() {

    @Test
    fun testCanQuery() {
        SimpleModel("Name").save()

        var cursor: FlowCursor? = null
        databaseForTable<SimpleModel>()
            .beginTransactionAsync { (select from SimpleModel::class).cursor(it) }
            .asMaybe()
            .subscribe {
                cursor = it
            }

        assertEquals(1, cursor!!.count)
        cursor!!.close()
    }

    @Test
    fun testCanCompileStatement() {
        var databaseStatement: DatabaseStatement? = null
        databaseForTable<SimpleModel>()
            .beginTransactionAsync {
                insert<SimpleModel>().columnValues(name.`is`("name")).compileStatement(it)
            }.asSingle()
            .subscribe { statement ->
                databaseStatement = statement
            }
        databaseStatement!!.close()
    }

    @Test
    fun testCountMethod() {
        SimpleModel("name").save()
        SimpleModel("name2").save()
        var count = 0L
        databaseForTable<SimpleModel>()
            .beginTransactionAsync {
                (selectCountOf(Property.ALL_PROPERTY) from SimpleModel::class).longValue(it)
            }
            .asSingle()
            .subscribe { value ->
                count = value
            }

        assertEquals(2, count)
    }

    @Test
    fun testInsertMethod() {
        var count = 0L
        databaseForTable<SimpleModel>()
            .beginTransactionAsync {
                (insert<SimpleModel>().columnValues(name.eq("name"))).executeInsert(it)
            }.asSingle()
            .subscribe { c ->
                count = c
            }

        assertEquals(1, count)
    }

}