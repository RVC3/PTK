package ru.ppr.database.greendao;

/**
 * @author Aleksandr Brazhkin
 */
public interface GreenDaoDatabase extends ru.ppr.database.Database, org.greenrobot.greendao.database.Database {
    @Override
    GreenDaoDatabaseStatement compileStatement(String sql);
}
