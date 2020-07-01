package ru.ppr.cppk.entity.event.model34;

/**
 * Created by Кашка Григорий on 13.12.2015.
 */
public class TrainInfo {

    //локальный идентификатор в БД
    private long id;

    /// Категория поезда
    /// («О» – пригородные пассажирские поезда, «С» - скорые пригородные поезда типа «Спутник» и т.д.)
    private String trainCategory;

    /// Номер поезда
    // ПТК не знает номера поезда
    //private String TrainNumber;

    /// Это не категория поезда, а непонятное поле, Николай Лившиц сказал выгружать Null
    private String carClass;

    /// Код RDS версии
    private int trainCategoryCode;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTrainCategory() {
        return trainCategory;
    }

    public void setTrainCategory(String trainCategory) {
        this.trainCategory = trainCategory;
    }

    public String getCarClass() {
        return carClass;
    }

    public void setCarClass(String carClass) {
        this.carClass = carClass;
    }

    public int getTrainCategoryCode() {
        return trainCategoryCode;
    }

    public void setTrainCategoryCode(int trainCategoryCode) {
        this.trainCategoryCode = trainCategoryCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TrainInfo trainInfo = (TrainInfo) o;

        if (trainCategoryCode != trainInfo.getTrainCategoryCode()) return false;
        if (!trainCategory.equals(trainInfo.getTrainCategory())) return false;
        if (carClass == null) return true;
        return carClass.equals(trainInfo.getCarClass());

    }

    public ru.ppr.nsi.entity.TrainCategory.TrainCategoryCategory getTrainCategoryCategory() {
        return ru.ppr.nsi.entity.TrainCategory.TrainCategoryCategory.getType(trainCategory);
    }

    @Override
    public int hashCode() {
        int result = trainCategory.hashCode();
        result = 31 * result + (carClass == null ? 0 : carClass.hashCode());
        result = 31 * result + trainCategoryCode;
        return result;
    }
}
