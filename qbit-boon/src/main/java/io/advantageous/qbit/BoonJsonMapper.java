package io.advantageous.qbit;

import io.advantageous.qbit.json.JsonMapper;
import org.boon.Boon;

/**
 * Created by gcc on 10/15/14.
 * @author Rick Hightower
 */
public class BoonJsonMapper implements JsonMapper {
    @Override
    public Object fromJson(String json) {
        return Boon.fromJson(json);
    }

    @Override
    public String toJson(Object object) {
        return Boon.toJson(object);
    }
}
