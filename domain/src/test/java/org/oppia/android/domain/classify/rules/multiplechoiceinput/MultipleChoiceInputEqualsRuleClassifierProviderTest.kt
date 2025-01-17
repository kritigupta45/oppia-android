package org.oppia.android.domain.classify.rules.multiplechoiceinput

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.domain.classify.InteractionObjectTestBuilder
import org.oppia.android.testing.assertThrows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [MultipleChoiceInputEqualsRuleClassifierProvider]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class MultipleChoiceInputEqualsRuleClassifierProviderTest {

  private val NON_NEGATIVE_VALUE_TEST_0 =
    InteractionObjectTestBuilder.createNonNegativeInt(value = 0)
  private val NON_NEGATIVE_VALUE_TEST_1 =
    InteractionObjectTestBuilder.createNonNegativeInt(value = 1)
  private val STRING_VALUE_TEST = InteractionObjectTestBuilder.createString(value = "test")

  @Inject
  internal lateinit var multipleChoiceInputEqualsRuleClassifierProvider:
    MultipleChoiceInputEqualsRuleClassifierProvider

  private val inputEqualsRuleClassifier by lazy {
    multipleChoiceInputEqualsRuleClassifierProvider.createRuleClassifier()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testNonNegativeAnswer_nonNegativeInput_sameValue_bothValuesMatch() {
    val inputs = mapOf("x" to NON_NEGATIVE_VALUE_TEST_0)

    val matches = inputEqualsRuleClassifier.matches(
      answer = NON_NEGATIVE_VALUE_TEST_0,
      inputs = inputs,
      writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testNonNegativeAnswer_nonNegativeInput_differentValue_bothValuesDoNotMatch() {
    val inputs = mapOf("x" to NON_NEGATIVE_VALUE_TEST_0)

    val matches = inputEqualsRuleClassifier.matches(
      answer = NON_NEGATIVE_VALUE_TEST_1,
      inputs = inputs,
      writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testNonNegativeAnswer_missingInput_throwsException() {
    val inputs = mapOf("y" to NON_NEGATIVE_VALUE_TEST_0)

    val exception = assertThrows(IllegalStateException::class) {
      inputEqualsRuleClassifier.matches(
        answer = NON_NEGATIVE_VALUE_TEST_0,
        inputs = inputs,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'x'")
  }

  @Test
  fun testUnexpectedStringAnswer_nonNegativeIntInput_throwsException() {
    val inputs = mapOf("x" to NON_NEGATIVE_VALUE_TEST_0)

    val exception = assertThrows(IllegalStateException::class) {
      inputEqualsRuleClassifier.matches(
        answer = STRING_VALUE_TEST,
        inputs = inputs,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected answer to be of type NON_NEGATIVE_INT")
  }

  @Test
  fun testNonNegativeAnswer_stringInput_throwsException() {
    val inputs = mapOf("x" to STRING_VALUE_TEST)

    val exception = assertThrows(IllegalStateException::class) {
      inputEqualsRuleClassifier.matches(
        answer = NON_NEGATIVE_VALUE_TEST_0,
        inputs = inputs,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected input value to be of type NON_NEGATIVE_INT")
  }

  private fun setUpTestApplicationComponent() {
    DaggerMultipleChoiceInputEqualsRuleClassifierProviderTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(modules = [])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: MultipleChoiceInputEqualsRuleClassifierProviderTest)
  }
}
