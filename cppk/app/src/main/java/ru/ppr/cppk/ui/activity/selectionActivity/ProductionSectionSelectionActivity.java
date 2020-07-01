package ru.ppr.cppk.ui.activity.selectionActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.List;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.Holder;
import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.ui.adapter.autoCompleteTextView.ProductionSectionAdapter;
import ru.ppr.cppk.ui.adapter.autoCompleteTextView.filters.ProductionSectionFilter;
import ru.ppr.cppk.ui.adapter.base.BaseListAdapter;
import ru.ppr.nsi.entity.ProductionSection;
import ru.ppr.nsi.repository.ProductionSectionRepository;

/**
 * Класс для выбора участка обслуживания.
 */
public class ProductionSectionSelectionActivity extends BaseSelectionActivity<ProductionSection> {

    // EXTRAS
    public static final String EXTRA_RESULT_CODE = "EXTRA_RESULT_CODE";
    public static final String EXTRA_RESULT_NAME = "EXTRA_RESULT_NAME";

    private volatile List<ProductionSection> items;

    private Holder<PrivateSettings> privateSettingsHolder;
    private NsiVersionManager nsiVersionManager;
    private ProductionSectionRepository productionSectionRepository;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        privateSettingsHolder = Globals.getInstance().getPrivateSettingsHolder();
        nsiVersionManager = Di.INSTANCE.nsiVersionManager();
        productionSectionRepository = Dagger.appComponent().productionSectionRepository();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected List<ProductionSection> getData() {

        List<ProductionSection> localList = items;

        if (localList == null) {
            synchronized (this) {
                if (items == null) {
                    items = Dagger.appComponent().productionSectionRepository().getAllProductionSections(Dagger.appComponent().nsiVersionManager().getCurrentNsiVersionId());
                }
            }
            return items;
        }

        return localList;
    }

    @Override
    protected Intent getIntent(ProductionSection selectedItem) {

        Intent intent = new Intent();
        intent.putExtra(EXTRA_RESULT_CODE, selectedItem.getCode());
        intent.putExtra(EXTRA_RESULT_NAME, selectedItem.getName());
        setResult(RESULT_OK, intent);
        return intent;

    }

    public static Intent getCallingIntent(Context context) {
        Intent intent = new Intent(context, ProductionSectionSelectionActivity.class);
        return intent;
    }

    @Override
    protected BaseListAdapter<ProductionSection> getAdapter() {
        BaseListAdapter<ProductionSection> adapter = new ProductionSectionAdapter(this);
        PrivateSettings privateSettings = privateSettingsHolder.get();
        ProductionSection currentProductionSection = productionSectionRepository.load(
                (long) privateSettings.getProductionSectionId(),
                nsiVersionManager.getCurrentNsiVersionId());
        adapter.setCurrentItem(currentProductionSection);
        adapter.setItems(getData());
        adapter.setFilter(new ProductionSectionFilter(adapter, getData()));
        return adapter;
    }

    @Override
    protected int getTitleId() {
        return R.string.ptk_area;
    }

    @Override
    protected int getHintId() {
        return R.string.enter_ptk_area;
    }

    @Override
    protected
    @Nullable
    ProductionSection getItemForName(String name) {

        ProductionSection productionSection = null;

        for (ProductionSection item : getData()) {
            if (item.getName().toLowerCase().equals(name.toLowerCase()))
                productionSection = item;
        }

        return productionSection;
    }

}
