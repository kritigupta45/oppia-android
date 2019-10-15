package org.oppia.app.activity

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.model.InteractionObject
import org.oppia.app.player.exploration.ExplorationActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matchers.instanceOf
import org.oppia.app.customview.inputInteractionView.NumberInputInteractionView

/** Tests for [NumericInputInteractionViewTestActivity]. */
@RunWith(AndroidJUnit4::class)
class NumericInputInteractionViewTestActivityTest{
  private lateinit var launchedActivity: Activity
  @get:Rule
  var activityTestRule: ActivityTestRule<NumericInputInteractionViewTestActivity> = ActivityTestRule(
    NumericInputInteractionViewTestActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )



  @Test
  fun testInputInteractionView_withInputtedText_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(NumericInputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_number_input_interaction_view)).perform(ViewActions.clearText(), ViewActions.typeText("9"))
    activityScenario.onActivity { activity ->
      val textAnswerRetriever = activity.findViewById(R.id.test_number_input_interaction_view) as NumberInputInteractionView
      assertThat(textAnswerRetriever.getPendingAnswer(), instanceOf(InteractionObject::class.java))
      assertThat(textAnswerRetriever.getPendingAnswer().normalizedString,`is`("9"))
    }
  }
  @Test
  fun testInputInteractionView_withInputtedText_onConfigurationChange_hasCorrectPendingAnswer() {
    Intents.init()
    val intent = Intent(Intent.ACTION_PICK)
    launchedActivity = activityTestRule.launchActivity(intent)
    onView(withId(R.id.test_number_input_interaction_view)).perform(ViewActions.clearText(), ViewActions.typeText("9"))
    activityTestRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    activityTestRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    onView(withId(R.id.test_number_input_interaction_view)).check(matches(isDisplayed())).check(matches(withText("9")))
    Intents.release()

  }
}