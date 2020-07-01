package ru.ppr.cppk.ui.fragment;

/**
 * Интерфейс для передачи события нажатия кнопки Back во фрагмент
 *
 * Created by Артем on 12.01.2016.
 */
public interface FragmentOnBackPressed {

    /**
     * Обработка нажатия нажатия кнопки Back
     * @return true если событие обработано и дальнейшая обработка не нужна, иначе false
     */
    boolean onBackPress();
}
