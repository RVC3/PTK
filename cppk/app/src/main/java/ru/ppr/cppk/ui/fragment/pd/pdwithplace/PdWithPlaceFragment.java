package ru.ppr.cppk.ui.fragment.pd.pdwithplace;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ru.ppr.cppk.FragmentParent;
import ru.ppr.cppk.R;
import ru.ppr.cppk.db.DateFormatOperations;
import ru.ppr.cppk.ui.fragment.pd.pdwithplace.model.PdWithPlaceViewModel;
import ru.ppr.logger.Logger;

/**
 * Фрагмент для отображения ПД с местом
 *
 * @author Dmitry Vinogradov
 */
public class PdWithPlaceFragment extends FragmentParent {

    private static final String TAG = Logger.makeLogTag(PdWithPlaceFragment.class);

    // region Views
    private TextView pdTitle;
    private TextView depStationName;
    private TextView destStationName;
    private ImageView directionImage;
    private TextView pdDateTimeValue;
    private TextView passengerValue;
    private TextView documentValue;
    private TextView trainValue;
    private TextView wagonValue;
    private TextView placeValue;
    //endregion

    //region Other
    private PdWithPlaceViewModel pdViewModel;
    //endregion

    public static PdWithPlaceFragment newInstance() {
        return new PdWithPlaceFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_pd_with_place, container, false);

        pdTitle = (TextView) view.findViewById(R.id.pdTitle);
        depStationName = (TextView) view.findViewById(R.id.depStationName);
        destStationName = (TextView) view.findViewById(R.id.destStationName);
        directionImage = (ImageView) view.findViewById(R.id.directionImage);
        pdDateTimeValue = (TextView) view.findViewById(R.id.pdDateTimeValue);
        passengerValue = (TextView) view.findViewById(R.id.passengerValue);
        documentValue = (TextView) view.findViewById(R.id.documentValue);
        trainValue = (TextView) view.findViewById(R.id.trainValue);
        wagonValue = (TextView) view.findViewById(R.id.wagonValue);
        placeValue = (TextView) view.findViewById(R.id.placeValue);

        // Устанавливаем заголовок
        pdTitle.setText(pdViewModel.getTitle());
        // Устанавливаем станции отправления и назначения
        depStationName.setText(pdViewModel.getDepStationName());
        destStationName.setText(pdViewModel.getDestStationName());
        // Устанавливаем направление
        directionImage.setImageResource(R.drawable.ic_direction_there);
        // Устанавливаем дату отправления
        String dateText = DateFormatOperations.getDateForOut(pdViewModel.getDepartureDate());
        pdDateTimeValue.setText(dateText);
        // Устанавливаем имя пассажира
        passengerValue.setText(pdViewModel.getPassengerName());
        // Устанавливаем номер документа
        documentValue.setText(pdViewModel.getDocumentNumber());
        // Устанавливаем номер поезда
        trainValue.setText(pdViewModel.getTrainNumber());
        // Устанавливаем номер вагона
        wagonValue.setText(pdViewModel.getWagonNumber());
        // Устанавливаем номер места
        placeValue.setText(pdViewModel.getPlaceNumber());

        return view;
    }

    public void setPdViewModel(PdWithPlaceViewModel pdViewModel) {
        this.pdViewModel = pdViewModel;
    }
}
