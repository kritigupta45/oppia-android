package org.oppia.android.app.player.state.itemviewmodel

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.ListOfSetsOfHtmlStrings
import org.oppia.android.app.model.StringList
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerErrorOrAvailabilityCheckReceiver
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.recyclerview.OnDragEndedListener
import org.oppia.android.app.recyclerview.OnItemDragListener

/** [StateItemViewModel] for drag drop & sort choice list. */
class DragAndDropSortInteractionViewModel(
  private val gcsResourceName: String,
  private val gcsEntityType: String,
  val entityId: String,
  val hasConversationView: Boolean,
  interaction: Interaction,
  private val interactionAnswerErrorOrAvailabilityCheckReceiver: InteractionAnswerErrorOrAvailabilityCheckReceiver, // ktlint-disable max-line-length
  val isSplitView: Boolean
) : StateItemViewModel(ViewType.DRAG_DROP_SORT_INTERACTION),
  InteractionAnswerHandler,
  OnItemDragListener,
  OnDragEndedListener {
  private val allowMultipleItemsInSamePosition: Boolean by lazy {
    interaction.customizationArgsMap["allowMultipleItemsInSamePosition"]?.boolValue ?: false
  }
  private val choiceStrings: List<String> by lazy {
    interaction.customizationArgsMap["choices"]
      ?.schemaObjectList
      ?.schemaObjectList
      ?.map { schemaObject -> schemaObject.subtitledHtml.html }
      ?: listOf()
  }

  val choiceItems: ArrayList<DragDropInteractionContentViewModel> =
    computeChoiceItems(choiceStrings, this, gcsResourceName, gcsEntityType, entityId)

  var isAnswerAvailable = ObservableField<Boolean>(false)

  init {
    val callback: Observable.OnPropertyChangedCallback =
      object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable, propertyId: Int) {
          interactionAnswerErrorOrAvailabilityCheckReceiver.onPendingAnswerErrorOrAvailabilityCheck(
            /* pendingAnswerError= */null,
            /* inputAnswerAvailable= */true
          )
        }
      }
    isAnswerAvailable.addOnPropertyChangedCallback(callback)
    isAnswerAvailable.set(true) // For drag drop submit button will be enabled by default.
  }

  override fun onItemDragged(
    indexFrom: Int,
    indexTo: Int,
    adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
  ) {
    val item = choiceItems[indexFrom]
    choiceItems.removeAt(indexFrom)
    choiceItems.add(indexTo, item)

    // Update the data of item moved for every drag if merge icons are displayed.
    if (allowMultipleItemsInSamePosition) {
      choiceItems[indexFrom].itemIndex = indexFrom
      choiceItems[indexTo].itemIndex = indexTo
    }
    adapter.notifyItemMoved(indexFrom, indexTo)
  }

  override fun onDragEnded(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
    // Update the data list if once drag is complete and merge icons are displayed.
    if (allowMultipleItemsInSamePosition) {
      (adapter as BindableAdapter<*>).setDataUnchecked(choiceItems)
    }
  }

  fun onItemMoved(
    indexFrom: Int,
    indexTo: Int,
    adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
  ) {

    val item = choiceItems[indexFrom]
    choiceItems.removeAt(indexFrom)
    choiceItems.add(indexTo, item)

    choiceItems[indexFrom].itemIndex = indexFrom
    choiceItems[indexTo].itemIndex = indexTo

    (adapter as BindableAdapter<*>).setDataUnchecked(choiceItems)
  }

  override fun getPendingAnswer(): UserAnswer {
    val userAnswerBuilder = UserAnswer.newBuilder()
    val listItems = choiceItems.map { it.htmlContent }
    userAnswerBuilder.listOfHtmlAnswers = convertItemsToAnswer(listItems)
    userAnswerBuilder.answer =
      InteractionObject.newBuilder().setListOfSetsOfHtmlString(
        ListOfSetsOfHtmlStrings.newBuilder().addAllSetOfHtmlStrings(listItems).build()
      ).build()
    return userAnswerBuilder.build()
  }

  /** Returns an HTML list containing all of the HTML string elements as items in the list. */
  private fun convertItemsToAnswer(htmlItems: List<StringList>): ListOfSetsOfHtmlStrings {
    return ListOfSetsOfHtmlStrings.newBuilder()
      .addAllSetOfHtmlStrings(htmlItems)
      .build()
  }

  /** Returns whether the grouping is allowed or not for [DragAndDropSortInteractionViewModel]. */
  fun getGroupingStatus(): Boolean {
    return allowMultipleItemsInSamePosition
  }

  fun updateList(
    itemIndex: Int,
    adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
  ) {
    val item = choiceItems[itemIndex]
    val nextItem = choiceItems[itemIndex + 1]
    nextItem.htmlContent = StringList.newBuilder().addAllHtml(nextItem.htmlContent.htmlList)
      .addAllHtml(item.htmlContent.htmlList)
      .build()
    choiceItems[itemIndex + 1] = nextItem

    choiceItems.removeAt(itemIndex)

    choiceItems.forEachIndexed { index, dragDropInteractionContentViewModel ->
      dragDropInteractionContentViewModel.itemIndex = index
      dragDropInteractionContentViewModel.listSize = choiceItems.size
    }
    // to update the content of grouped item
    (adapter as BindableAdapter<*>).setDataUnchecked(choiceItems)
  }

  fun unlinkElement(itemIndex: Int, adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
    val item = choiceItems[itemIndex]
    choiceItems.removeAt(itemIndex)
    item.htmlContent.htmlList.forEach {
      choiceItems.add(
        itemIndex,
        DragDropInteractionContentViewModel(
          StringList.newBuilder()
            .addHtml(it)
            .build(),
          gcsResourceName,
          gcsEntityType,
          entityId,
          /* itemIndex= */ 0,
          /* listSize= */ 0,
          /* dragAndDropSortInteractionViewModel= */this
        )
      )
    }

    choiceItems.forEachIndexed { index, dragDropInteractionContentViewModel ->
      dragDropInteractionContentViewModel.itemIndex = index
      dragDropInteractionContentViewModel.listSize = choiceItems.size
    }
    // to update the list
    (adapter as BindableAdapter<*>).setDataUnchecked(choiceItems)
  }

  companion object {
    private fun computeChoiceItems(
      choiceStrings: List<String>,
      dragAndDropSortInteractionViewModel: DragAndDropSortInteractionViewModel,
      gcsResourceName: String,
      gcsEntityType: String,
      gcsEntityId: String
    ): ArrayList<DragDropInteractionContentViewModel> {
      val itemList = ArrayList<DragDropInteractionContentViewModel>()
      itemList += choiceStrings.mapIndexed { index, choiceString ->
        DragDropInteractionContentViewModel(
          htmlContent = StringList.newBuilder().addHtml(choiceString).build(),
          gcsResourceName = gcsResourceName,
          gcsEntityType = gcsEntityType,
          gcsEntityId = gcsEntityId,
          itemIndex = index,
          listSize = choiceStrings.size,
          dragAndDropSortInteractionViewModel = dragAndDropSortInteractionViewModel
        )
      }
      return itemList
    }
  }
}