package com.spyneai.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R
import com.spyneai.credits.holder.TransactionHistoryHolder
import com.spyneai.credits.holder.TransactionTypeHolder
import com.spyneai.onboardingv2.ui.intro.holder.ChooseCatHolderV2
import com.spyneai.onboardingv2.ui.intro.holder.ChooseCategoryHolder
import com.spyneai.onboardingv2.ui.intro.holder.ImageProcessingHolder
import com.spyneai.onboardingv2.ui.intro.holder.SampleImageHolder
import com.spyneai.registration.view.ui.activity.PreferenceListHolder
import com.spyneai.reshoot.*
import com.spyneai.shootapp.data.OnOverlaySelectionListener
import com.spyneai.shootapp.holders.*
import com.spyneai.singleimageprocessing.ui.holder.SingleImageOverlaysHolder
import com.spyneai.singleimageprocessing.ui.holder.SingleImageSubcategoryHolder
import com.spyneai.trybackground.ui.TryBackgroundHolder


object JavaViewHolderFactory {

    fun create(view: View, viewType: Int, listener: OnItemClickListener,
                overlaySelectionListener: OnOverlaySelectionListener? = null): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.item_app_subcategories -> SubcategoryHolder(view, listener)
            R.layout.item_shoot_config_subcategory -> ShootConfigSubCatHolder(view, listener)
            R.layout.item_angle -> AngleHolder(view, listener)
            R.layout.item_background_new -> BackgroundHolder(view, listener)
            R.layout.item_single_image_subcategories -> SingleImageSubcategoryHolder(view, listener)
            R.layout.item_overlays -> OverlaysHolder(view, listener,overlaySelectionListener)
            R.layout.item_single_image_overlays -> SingleImageOverlaysHolder(view, listener,overlaySelectionListener)
            R.layout.item_interior -> InteriorHolder(view, listener,overlaySelectionListener)
            R.layout.item_miscellanous -> MiscHolder(view, listener,overlaySelectionListener)
            R.layout.item_app_select_image -> SelectImageHolder(view, listener)
            R.layout.item_reshoot -> ReshootHolder(view,listener,overlaySelectionListener)
            R.layout.item_reshoot_ecom -> ReshootEcomHolder(view,listener,overlaySelectionListener)
            R.layout.item_clicked -> ClickedHolder(view,listener,overlaySelectionListener)
            R.layout.item_category -> ChooseCategoryHolder(view,listener)
            R.layout.item_process_image -> ImageProcessingHolder(view,listener)
            R.layout.item_sample_image -> SampleImageHolder(view,listener)
            R.layout.item_try_background -> TryBackgroundHolder(view,listener)
            R.layout.item_reshoot_request -> ReshootSkuHolder(view,listener)
            R.layout.item_reshoot_sku_image -> ReshootSkuImageHolder(view,listener)
            R.layout.preference_list_invite -> PreferenceListHolder(view,listener)
            R.layout.item_transaction -> TransactionHistoryHolder(view,listener)
            R.layout.item_transaction_date -> TransactionHistoryHolder(view,listener)
            R.layout.item_transaction_type -> TransactionTypeHolder(view,listener)
            R.layout.item_catgory_webview -> ChooseCatHolderV2(view,listener)
            R.layout.item_no_plate -> NumberPlateHolderApp(view,listener)
            R.layout.food_item -> FoodBgHolder(view,listener,overlaySelectionListener)
            else -> GenericViewHolder(view)
        }
    }
}