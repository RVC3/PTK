package ru.ppr.core.ui.mvp;

import ru.ppr.core.ui.mvp.core.MvpProcessor;
import ru.ppr.core.ui.mvp.view.MvpView;

/**
 * Класс-маркер для поддрежки старой реализации MVP.
 * Удалить, когда параметры во все фрагменты не станут передаваться стандартным для ОС путем.
 *
 * @author Aleksandr Brazhkin
 * @deprecated Use {@link MvpDelegate} instead
 */
@Deprecated
public class LegacyMvpDelegate extends MvpDelegate {
    public LegacyMvpDelegate(MvpProcessor mvpProcessor, MvpView view) {
        super(mvpProcessor, view);
    }
}
