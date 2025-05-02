package Database.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface IMapper<T> {
    T mapper(final ResultSet rs) throws SQLException;
}
