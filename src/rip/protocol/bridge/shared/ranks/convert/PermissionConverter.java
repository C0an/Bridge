package rip.protocol.bridge.shared.ranks.convert;

import rip.protocol.bridge.shared.utils.Callback;

public abstract class PermissionConverter {

    public abstract void convert(Callback<Boolean> callback);
    public abstract void convertToRank(Callback<Boolean> callback);



}
