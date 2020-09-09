package rip.protocol.bridge.shared.disguise;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Disguise {

    private String realName, disguisedName;
    private Skin skin;

    public String toString() {
        return new Gson().toJson(this);
    }

}
