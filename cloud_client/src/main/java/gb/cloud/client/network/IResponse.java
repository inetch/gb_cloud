package gb.cloud.client.network;

import gb.cloud.common.network.CommandMessage;
import gb.cloud.common.network.ICommandMessage;

public interface IResponse {
    void gotOk(ICommandMessage message);
    void gotError(ICommandMessage message);
    void networkError();
}
