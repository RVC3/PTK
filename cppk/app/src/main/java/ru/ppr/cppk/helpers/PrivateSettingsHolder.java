package ru.ppr.cppk.helpers;

import ru.ppr.cppk.Holder;
import ru.ppr.cppk.entity.settings.PrivateSettings;

/**
 * @author Aleksandr Brazhkin
 */
public class PrivateSettingsHolder implements Holder<PrivateSettings> {

    private final Holder<PrivateSettings> privateSettingsHolder;

    public PrivateSettingsHolder(Holder<PrivateSettings> privateSettingsHolder) {
        this.privateSettingsHolder = privateSettingsHolder;
    }

    @Override
    public PrivateSettings get() {
        return privateSettingsHolder.get();
    }

    @Override
    public void set(PrivateSettings privateSettings) {
        privateSettingsHolder.set(privateSettings);
    }
}
