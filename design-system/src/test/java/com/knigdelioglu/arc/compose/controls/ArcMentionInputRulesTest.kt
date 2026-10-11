package com.knigdelioglu.arc.compose.controls

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ArcMentionInputRulesTest {
    @Test fun findsPeopleAndChannelQueriesAtTheCaret() {
        assertEquals(ArcMentionQuery('@', "ja", 3, 6), ArcMentionInputRules.activeQuery("Hi @jasmine", 6))
        assertEquals(ArcMentionQuery('#', "des", 0, 4), ArcMentionInputRules.activeQuery("#design team", 4))
    }

    @Test fun doesNotTreatAnEmailAddressOrCompletedWordAsAMentionQuery() {
        assertNull(ArcMentionInputRules.activeQuery("name@example.com", 8))
        assertNull(ArcMentionInputRules.activeQuery("@one two", 8))
    }

    @Test fun resolvesTheQueryBeforeLaterMentionsWhenTheCaretMovesBack() {
        val text = "@first then @ja #channel"
        val cursor = text.indexOf("@ja") + 3
        assertEquals(ArcMentionQuery('@', "ja", text.indexOf("@ja"), cursor), ArcMentionInputRules.activeQuery(text, cursor))
    }

    @Test fun insertingAChoiceReplacesOnlyTheQueryAndKeepsTheSuffix() {
        val field = TextFieldValue("Hi @ja there", selection = TextRange(6))
        val query = ArcMentionInputRules.activeQuery(field.text, field.selection.end)!!
        val inserted = ArcMentionInputRules.insert(field, query, ArcMentionCandidate("jane-id", "Jane Smith", token = "jane"))
        assertEquals("Hi @jane there", inserted.value.text)
        assertEquals(8, inserted.value.selection.end)
        assertEquals(3, inserted.tokenStart)
        assertEquals(8, inserted.tokenEnd)
    }
}
