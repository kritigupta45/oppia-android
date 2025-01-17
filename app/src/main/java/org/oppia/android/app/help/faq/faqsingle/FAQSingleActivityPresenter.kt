package org.oppia.android.app.help.faq.faqsingle

import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.databinding.FaqSingleActivityBinding
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.parser.html.HtmlParser
import javax.inject.Inject

/** The presenter for [FAQSingleActivity]. */
@ActivityScope
class FAQSingleActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val htmlParserFactory: HtmlParser.Factory,
  @DefaultResourceBucketName private val resourceBucketName: String,
  private val resourceHandler: AppLanguageResourceHandler
) {

  private lateinit var faqSingleActivityToolbar: Toolbar

  fun handleOnCreate(question: String, answer: String) {
    val binding = DataBindingUtil.setContentView<FaqSingleActivityBinding>(
      activity,
      R.layout.faq_single_activity
    )
    binding.apply {
      lifecycleOwner = activity
    }

    faqSingleActivityToolbar = binding.faqSingleActivityToolbar
    activity.setSupportActionBar(faqSingleActivityToolbar)
    activity.supportActionBar!!.title = resourceHandler.getStringInLocale(R.string.FAQs)
    activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
    activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    binding.faqSingleActivityToolbar.setNavigationOnClickListener {
      (activity as FAQSingleActivity).finish()
    }
    activity.findViewById<TextView>(R.id.faq_question_text_view).text = question

    // NOTE: Here entityType and entityId can be anything as it will actually not get used.
    // They are needed only for cases where rich-text contains images from server and in faq
    // we do not have images.
    val answerTextView = activity.findViewById<TextView>(R.id.faq_answer_text_view)
    answerTextView.text = htmlParserFactory.create(
      resourceBucketName,
      entityType = "faq",
      entityId = "oppia",
      imageCenterAlign = false
    ).parseOppiaHtml(
      answer,
      answerTextView
    )
  }
}
