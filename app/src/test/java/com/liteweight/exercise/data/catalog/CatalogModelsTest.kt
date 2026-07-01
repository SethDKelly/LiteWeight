package com.liteweight.exercise.data.catalog

import org.junit.Assert.assertEquals
import org.junit.Test

class CatalogModelsTest {
    @Test
    fun decodesMinimalCatalogEntry() {
        val json =
            """
            {
              "catalogVersion": 1,
              "sourceAttribution": "test",
              "entries": [
                {
                  "catalogId": "lw:test",
                  "displayName": "Test Press",
                  "namingMode": "structured",
                  "equipment": "barbell",
                  "unitType": "weight"
                }
              ]
            }
            """.trimIndent()
        val bundle = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }.decodeFromString<ExerciseCatalogBundle>(json)
        assertEquals(1, bundle.catalogVersion)
        assertEquals("Test Press", bundle.entries.first().displayName)
    }
}
