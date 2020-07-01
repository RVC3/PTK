package ru.ppr.chit.ui.activity.passengerlist.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import javax.inject.Inject;

import ru.ppr.chit.R;
import ru.ppr.chit.ui.activity.passengerlist.model.PassengerInfo;
import ru.ppr.core.ui.adapter.BaseRecyclerAdapter;

/**
 * @author Aleksandr Brazhkin
 */
public class PassengerRecyclerAdapter extends BaseRecyclerAdapter<PassengerInfo, RecyclerView.ViewHolder> {

    public interface Loader {
        void uploadItems(int recordsOffset);
    }

    private Drawable drawableBoarding;
    private Drawable drawableBoarded;
    private Drawable drawableUnboarded;

    private Loader loader;
    private boolean hasMoreData = false;
    private boolean isLoading = false;


    @Inject
    PassengerRecyclerAdapter() {}

    public void setLoader(Loader loader){
        this.loader = loader;
    }

    public void uploadItems(List<PassengerInfo> passengers, int positionFrom, boolean hasMoreData) {
        this.hasMoreData = hasMoreData;

        if (positionFrom == 0 || items == null){
            setItems(passengers);
        } else {
            if (positionFrom < items.size()) {
                items.remove(positionFrom);
            }
            insertItems(positionFrom, passengers);
        }

        isLoading = false;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        if (drawableBoarded == null) {
            drawableBoarded = ContextCompat.getDrawable(context, R.drawable.ic_passenger_boarded);
            drawableBoarded.setColorFilter(ContextCompat.getColor(context, R.color.app_success), PorterDuff.Mode.SRC_IN);
        }
        if (drawableBoarding == null){
            drawableBoarding = ContextCompat.getDrawable(context, R.drawable.ic_passenger_boarding2);
            drawableBoarding.setColorFilter(ContextCompat.getColor(context, R.color.passengerListItemFio), PorterDuff.Mode.SRC_IN);
        }
        if (drawableUnboarded == null) {
            drawableUnboarded = ContextCompat.getDrawable(context, R.drawable.ic_passenger_unboarded);
            drawableUnboarded.setColorFilter(ContextCompat.getColor(context, R.color.passengerListItemDocNumber), PorterDuff.Mode.SRC_IN);
        }

        recyclerView.addOnScrollListener(recyclerViewOnScrollListener);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        recyclerView.removeOnScrollListener(recyclerViewOnScrollListener);
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mLayoutInflater.inflate(R.layout.item_passenger, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder vh = (ViewHolder) holder;

        PassengerInfo passengerInfo = getItem(position);

        vh.fio.setText(passengerInfo.getFio());
        vh.docNumber.setText(passengerInfo.getDocumentNumber());

        final Context context = holder.itemView.getContext();
        // Посадка уже совершена
        if (passengerInfo.getWasBoarded()) {
            holder.itemView.setBackgroundResource(R.color.passengerItemBackgroundBoarded);
            vh.fio.setCompoundDrawablesWithIntrinsicBounds(null, null, drawableBoarded, null);
        } else
            // Пассажир садится на текущей станции
            if (passengerInfo.getIsCurrentStationBoarding()){
                holder.itemView.setBackgroundResource(0);
                vh.fio.setCompoundDrawablesWithIntrinsicBounds(null, null, drawableBoarding, null);
            } else {
                // Пассажир должен сесть на другой станции
                holder.itemView.setBackgroundResource(R.color.passengerItemBackgroundUnboarded);
                vh.fio.setCompoundDrawablesWithIntrinsicBounds(null, null, drawableUnboarded, null);
            }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView fio;
        TextView docNumber;

        ViewHolder(View itemView) {
            super(itemView);

            fio = (TextView) itemView.findViewById(R.id.fio);
            docNumber = (TextView) itemView.findViewById(R.id.docNumber);
        }
    }

    private RecyclerView.OnScrollListener recyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            if (loader == null || items == null || items.isEmpty()){
                return;
            }

            final LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            int visibleItemCount = layoutManager.getChildCount();
            int totalItemCount = layoutManager.getItemCount();
            int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

            synchronized (this) {
                if (!isLoading && hasMoreData) {
                    if (firstVisibleItemPosition > 0 && firstVisibleItemPosition >= totalItemCount - visibleItemCount) {
                        isLoading = true;
                        loader.uploadItems(items.size());
                    }
                }
            }
        }

    };

}
